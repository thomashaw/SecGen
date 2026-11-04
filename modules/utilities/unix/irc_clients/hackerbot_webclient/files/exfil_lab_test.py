#!/usr/bin/env python3
"""DEV ONLY: automated end-to-end test of the exfiltration detection Hackerbot lab (response_and_investigation/6).

Copy this file to lab_dev/<lab>_test.py, fill in discover(), clean_start() and run_all(), and serve it
(together with hackerbot_irc.py from the skill's scripts/) from the hackerbot_server's web client.
Run on the student-facing VM (usually the desktop), as the lab's main user, on a FRESH build:

    curl -sS --noproxy '*' -O http://hackerbot:8080/<lab>_test.py -O http://hackerbot:8080/hackerbot_irc.py
    python3 <lab>_test.py --irc-check      # 10 s: can we reach the bot, do the prompts parse?
    python3 <lab>_test.py --yes            # the full run (minutes); writes ~/<lab>_report.txt

It walks the labsheet top to bottom running its commands, plays every Hackerbot challenge over IRC
(including deliberate student mistakes, to record each hint) and writes a compact report (SUMMARY
first, then DETAILS) plus a full log. It changes the VMs - only use disposable builds.
Keep it Python-3.7 compatible (old bases) and standard-library only.
"""
import argparse
import datetime
import os
import re
import shlex
import socket
import subprocess
import sys
import tempfile
import time

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from hackerbot_irc import Hackerbot  # noqa: E402  (served next to this file)

LAB = 'exfil'
VERSION = '2026-11-04.4'
HOME = os.path.expanduser('~')
REPORT = os.path.join(HOME, LAB + '_report.txt')
FULL_LOG = os.path.join(HOME, LAB + '_full_log.txt')
SUDOERS_DROPIN = '/etc/sudoers.d/zz-%s-labtest' % LAB

_log_fh = None
summary = []        # (id, status, text)
details = []        # lines for the DETAILS part of the report
flags_seen = []     # (where, flag)
bot = None          # Hackerbot connection


class C:
    """Values discovered from the bot's prompts / the VM (fill in discover())."""
    U = None        # the lab's main user (the one running this)
    IP = None       # the host remote() talks to (set per call by on())
    IDS = None      # ids_monitor IP (--ids-ip)
    WEB = None      # web_server IP (--web-ip)
    HB = None       # hackerbot_server IP (from getent)
    PW = None
    MSG = {}        # attack number -> alert message the prompt asks for


# --------------------------------------------------------------------------------------------- report
def log(text=''):
    _log_fh.write(text + '\n')
    _log_fh.flush()


def say(text):
    print(text, flush=True)
    log(text)


def section(title):
    say('\n=== ' + title)
    details.extend(['', '=' * 100, title, '=' * 100])


def note(text):
    details.append(text)
    log('NOTE: ' + text)


def result(rid, status, text):
    """status: OK (works as intended) | PROBLEM (unexpected) | INFO (observation) |
    CONFIRMED / NOT-REPRODUCED (when checking a suspected issue) | ERROR (the test itself broke)"""
    summary.append((rid, status, text))
    details.append('[%s] %s: %s' % (status, rid, text))
    say('  [%s] %s: %s' % (status, rid, text))


def trim(out, n=12, width=220):
    lines = [l[:width] for l in out.rstrip().splitlines()]
    if len(lines) > n:
        lines = lines[:n // 2] + ['  ... (%d lines omitted, see full log) ...' % (len(lines) - n)] + lines[-(n - n // 2):]
    return '\n'.join('    | ' + l for l in lines)


def write_report(header):
    with open(REPORT, 'w') as f:
        f.write('\n'.join(header) + '\n\n')
        f.write('SUMMARY  (OK = works as intended; PROBLEM = unexpected; INFO = observation)\n')
        for rid, st, text in summary:
            f.write('%-15s %-20s %s\n' % (st, rid, text))
        f.write('\n\nDETAILS\n' + '\n'.join(details) + '\n')


# --------------------------------------------------------------------------------------------- shell
def sh(cmd, timeout=1800, quiet=False, label=None):
    """Run cmd with bash -c as this user (so ~ and $HOME expand exactly as for a student). stdin is
    /dev/null so nothing can hang on a prompt. Returns (rc, combined output)."""
    start = time.time()
    log('\n$ ' + cmd)
    try:
        p = subprocess.run(['bash', '-c', cmd], stdin=subprocess.DEVNULL, stdout=subprocess.PIPE,
                           stderr=subprocess.STDOUT, timeout=timeout, env=dict(os.environ, LC_ALL='C', LANG='C'))
        out, rc = p.stdout.decode('utf-8', 'replace'), p.returncode
    except subprocess.TimeoutExpired as e:
        out, rc = (e.output or b'').decode('utf-8', 'replace') + '\n[TIMEOUT after %ds]' % timeout, 124
    dur = time.time() - start
    log(out.rstrip())
    log('[rc=%d %.1fs]' % (rc, dur))
    if not quiet:
        details.append('$ %s   -> rc=%d (%.0fs)' % (label or cmd, rc, dur))
        if out.strip():
            details.append(trim(out))
    return rc, out


def remote(cmd, **kw):
    """Run cmd on the second server as C.U (key auth from setup_ssh)."""
    return sh('ssh -o BatchMode=yes %s@%s %s' % (C.U, C.IP, shlex.quote(cmd)),
              label=kw.pop('label', None) or '[%s] %s' % (C.IP, cmd), **kw)


def warnings_in(out):
    return [l for l in out.splitlines() if re.search(r'warn|error|does not exist|denied|failed', l, re.I)]


# --------------------------------------------------------------------------------------------- bot
def bot_goto(n):
    r = bot.goto(n)
    if not r.ended:
        note('(goto %s: no end-of-reply seen; got %s)' % (n, r.lines))
    return r


def bot_ready(n, what):
    """Say ready on attack n (after a goto), record the reply in DETAILS. Returns the Reply
    (r.verdict, r.flags, r.fyi, r.lines)."""
    say('  ... ready on attack %s: %s' % (n, what))
    t0 = time.time()
    r = bot.ready()
    for f in r.flags:
        flags_seen.append(('attack %s: %s' % (n, what), f))
    details.append('BOT #%s ready (%s) [%.0fs]%s:' % (n, what, time.time() - t0, '' if r.ended else ' [NO END-OF-REPLY SEEN]'))
    if r.fyi:
        details.append('    > ' + r.fyi[:400])
    details.append('    > ' + re.sub(r'flag\{[^}]*\}', 'flag{..}', r.verdict)[:300])
    return r


def said(r, text):
    return text in ' '.join(r.lines)


# --------------------------------------------------------------------------------------------- setup
def install_sudo(password):
    """Passwordless sudo for the run (removed at the end).
    Not 'USER ALL=(ALL) NOPASSWD: ALL': sudo uses the last matching rule, and some bases add the
    account's own 'USER ALL=(ALL) ALL' after #includedir /etc/sudoers.d, overriding the drop-in.
    A per-user Defaults applies to any rule without an explicit PASSWD/NOPASSWD tag, in any order."""
    if sh('sudo -n true', quiet=True)[0] == 0:
        return
    line = 'Defaults:%s !authenticate' % C.U
    p = subprocess.run(['sudo', '-S', '-p', '', 'bash', '-c',
                        'echo %s > %s && chmod 440 %s && visudo -cf %s' % (shlex.quote(line), SUDOERS_DROPIN,
                                                                          SUDOERS_DROPIN, SUDOERS_DROPIN)],
                       input=(password + '\n').encode(), stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    log(p.stdout.decode())
    if subprocess.run(['sudo', '-n', 'true']).returncode != 0:
        sys.exit('Could not enable passwordless sudo - is --password right?')


def remove_sudo():
    sh('sudo rm -f ' + SUDOERS_DROPIN, quiet=True)


def setup_ssh(password, host):
    """Key-based SSH as C.U and as root@here -> C.U@C.IP, pushed once with the lab password via
    SSH_ASKPASS (OpenSSH >= 8.4), so labsheet commands with ssh/scp/rsync run unattended."""
    C.IP = host
    section('SETUP: passwordless SSH to %s (for automation only)' % C.IP)
    sh('mkdir -p ~/.ssh && chmod 700 ~/.ssh && { [ -f ~/.ssh/id_ed25519 ] || ssh-keygen -q -t ed25519 -N "" -f ~/.ssh/id_ed25519; }', quiet=True)
    sh('sudo bash -c \'mkdir -p /root/.ssh && { [ -f /root/.ssh/id_ed25519 ] || ssh-keygen -q -t ed25519 -N "" -f /root/.ssh/id_ed25519; }\'', quiet=True)
    sh('ssh-keyscan -H %s 2>/dev/null >> ~/.ssh/known_hosts; sudo bash -c "ssh-keyscan -H %s 2>/dev/null >> /root/.ssh/known_hosts"' % (C.IP, C.IP), quiet=True)
    probe = 'ssh -o BatchMode=yes {0}@{1} true && sudo ssh -o BatchMode=yes {0}@{1} true'.format(C.U, C.IP)
    if sh(probe, quiet=True)[0] != 0:
        fd, askpass = tempfile.mkstemp(prefix='askpass')
        with os.fdopen(fd, 'w') as f:
            f.write('#!/bin/sh\necho ' + shlex.quote(password) + '\n')
        os.chmod(askpass, 0o700)
        try:
            pubs = sh('cat ~/.ssh/id_ed25519.pub; sudo cat /root/.ssh/id_ed25519.pub', quiet=True)[1]
            p = subprocess.run(['ssh', '-o', 'PubkeyAuthentication=no', '-o', 'StrictHostKeyChecking=accept-new',
                                '%s@%s' % (C.U, C.IP), 'umask 077; mkdir -p ~/.ssh; cat >> ~/.ssh/authorized_keys'],
                               input=pubs.encode(), stdout=subprocess.PIPE, stderr=subprocess.STDOUT, timeout=60,
                               env=dict(os.environ, SSH_ASKPASS=askpass, SSH_ASKPASS_REQUIRE='force', DISPLAY=':0'))
            log(p.stdout.decode())
        finally:
            os.unlink(askpass)
    if sh(probe + ' && echo KEYS_OK', quiet=True)[1].find('KEYS_OK') < 0:
        sys.exit('Could not set up key-based SSH to %s (password auth off? wrong --password?). See %s' % (C.IP, FULL_LOG))


# --------------------------------------------------------------------------------------------- lab-specific
SNORT_RULES = '/etc/snort/rules/local.rules'
ALERTS = '/var/log/snort/alert'
rules = []          # every rule written so far (students keep their earlier rules)


def on(host, cmd, **kw):
    C.IP = host
    return remote(cmd, **kw)


def remote_sudo_setup(host):
    """Passwordless sudo for C.U on host (removed at the end); the password goes on stdin."""
    line = 'Defaults:%s !authenticate' % C.U
    cmd = "sudo -S -p '' sh -c %s" % shlex.quote('echo %s > %s && chmod 440 %s' % (shlex.quote(line), SUDOERS_DROPIN, SUDOERS_DROPIN))
    p = subprocess.run(['ssh', '-o', 'BatchMode=yes', '%s@%s' % (C.U, host), cmd], input=(C.PW + '\n').encode(),
                       stdout=subprocess.PIPE, stderr=subprocess.STDOUT, timeout=60)
    log(p.stdout.decode('utf-8', 'replace'))
    if on(host, 'sudo -n true', quiet=True)[0] != 0:
        sys.exit('Could not enable passwordless sudo on %s' % host)


def write_rules(extra=None, restart=True):
    """Replace local.rules on the IDS with every rule so far (plus extra), then stop/start Snort as the sheet says."""
    text = '\n'.join(rules + ([extra] if extra else [])) + '\n'
    rc, _ = on(C.IDS, 'printf %%s %s | sudo tee %s >/dev/null' % (shlex.quote(text), SNORT_RULES), label='[ids] write %s (%d rules)' % (SNORT_RULES, len(rules) + (1 if extra else 0)))
    if restart:
        return restart_snort()
    return rc == 0


def restart_snort():
    on(C.IDS, 'sudo service snort stop; sudo service snort start; sleep 8; pgrep -x snort', label='[ids] sudo service snort stop/start')
    return on(C.IDS, 'pgrep -x snort', quiet=True)[0] == 0


def rule(msg, body, sid):
    return 'alert tcp any any -> any any (msg:"%s"; %s sid:%d; rev:1;)' % (msg, body, sid)


def csv_lines():
    return on(C.WEB, 'cat ~/trade_secrets/clients.csv', quiet=True)[1].splitlines()


def expect(rid, r, phrase, what):
    ok = phrase in ' '.join(r.lines)
    result(rid, 'OK' if ok else 'PROBLEM', '%s: bot said %r' % (what, re.sub(r'flag\{[^}]*\}', 'flag{..}', r.verdict)[:160]))
    return ok


def expect_flag(rid, r, what):
    ok = bool(r.flags)
    result(rid, 'OK' if ok else 'PROBLEM', '%s: %s; bot said %r' % (what, 'flag received' if ok else 'NO FLAG', re.sub(r'flag\{[^}]*\}', 'flag{..}', r.verdict)[:160]))
    return ok


def bot_answer(n, text, what):
    say('  ... answer on attack %s: %s' % (n, text))
    r = bot.answer(text)
    for f in r.flags:
        flags_seen.append(('attack %s answer: %s' % (n, what), f))
    details.append('BOT #%s answer %r (%s):' % (n, text, what))
    details.append('    > ' + re.sub(r'flag\{[^}]*\}', 'flag{..}', ' / '.join(r.lines))[:400])
    return r


def discover():
    prompts = bot.list_attacks()
    if not prompts:
        sys.exit('No attacks listed - is the bot running?')
    for n in (1, 2, 3):
        m = re.search(r'message "([0-9a-f]{6})"', prompts.get(n, ''))
        if m:
            C.MSG[n] = m.group(1)
    if len(C.MSG) != 3:
        sys.exit('Could not read the alert messages from prompts 1-3: %r' % prompts)
    C.HB = C.HB or (sh('getent hosts hackerbot', quiet=True)[1].split() or [None])[0]
    return prompts


def clean_start():
    section('SETUP: check the VMs look fresh')
    rc, out = on(C.IDS, 'sudo cp -n %s %s.labtest-orig; cat %s' % (SNORT_RULES, SNORT_RULES, SNORT_RULES), quiet=True)
    fresh = not re.search(r'^\s*alert', out, re.M)
    result('FRESH', 'OK' if fresh else 'INFO', 'local.rules on the IDS has %s' % ('no alert rules' if fresh else 'rules already'))
    on(C.WEB, 'sudo iptables -F; sudo iptables -L -n | head -20', label='[web] clear any iptables rules left by a previous run')
    return True


def restore():
    """Put the VMs back in a student's starting state, so the owner can test the same set by hand."""
    section('CLEANUP: restore the starting state')
    on(C.IDS, 'sudo sh -c "if [ -f {0}.labtest-orig ]; then mv {0}.labtest-orig {0}; fi"; sudo pkill -x tcpdump; sudo rm -f /tmp/incident.pcap'.format(SNORT_RULES), label='[ids] restore local.rules, remove the capture')
    restart_snort()
    on(C.WEB, 'sudo iptables -F; sudo iptables -L INPUT -n | wc -l', label='[web] remove iptables rules')
    pub = sh('cat ~/.ssh/id_ed25519.pub 2>/dev/null; sudo cat /root/.ssh/id_ed25519.pub 2>/dev/null', quiet=True)[1].split('\n')
    keys = [l.split()[1] for l in pub if len(l.split()) > 1]
    for h in (C.IDS, C.WEB):
        on(h, 'sudo rm -f %s; ' % SUDOERS_DROPIN + ''.join("sed -i '\\#%s#d' ~/.ssh/authorized_keys; " % k for k in keys), quiet=True)
    sh('rm -f ~/.ssh/id_ed25519 ~/.ssh/id_ed25519.pub; sudo rm -f /root/.ssh/id_ed25519 /root/.ssh/id_ed25519.pub', quiet=True)
    result('CLEANUP', 'INFO', 'restored local.rules, flushed iptables on web_server, removed the capture and the tester\'s SSH keys and sudo drop-ins')


def run_all(args):
    U = C.U
    section('LABSHEET: Getting Snort up and running')
    rc, out = on(C.IDS, 'sudo grep -nE "^output (alert_fast|log_tcpdump)" /etc/snort/snort.conf')
    result('S1-OUTPUT', 'OK' if 'alert_fast' in out and 'log_tcpdump' in out else 'PROBLEM', 'snort.conf output lines: %s' % ' | '.join(out.split()))
    rc, out = on(C.IDS, 'grep -E "^DEBIAN_SNORT_(INTERFACE|HOME_NET)" /etc/snort/snort.debian.conf; ip -o link | grep -c ens19')
    result('S1-IFACE', 'OK' if 'ens19' in out else 'PROBLEM', 'snort.debian.conf: %s' % ' | '.join(out.split()))
    ok = restart_snort()
    result('S1-START', 'OK' if ok else 'PROBLEM', 'Snort running after sudo service snort stop/start: %s' % ok)
    rc, out = on(C.IDS, 'sudo snort -T -c /etc/snort/snort.conf 2>&1 | tail -3', timeout=300)
    result('S1-SNORT-T', 'OK' if 'successfully validated' in out else 'PROBLEM', 'snort -T (the Tip): %s' % (out.strip().splitlines() or [''])[-1])

    section('LABSHEET: Testing your rules (FTP fetch while tailing the alert file)')
    rc, env = sh('env | grep -i proxy || true')
    result('T-PROXY', 'INFO', 'proxy variables on the desktop: %s' % (env.strip().replace('\n', ' ') or 'none'))
    lines = csv_lines()
    result('T-CSV', 'OK' if len(lines) >= 2 else 'PROBLEM', 'clients.csv on web_server has %d lines; first starts %r' % (len(lines), lines[0][:25] if lines else ''))
    ni = re.findall(r'QQ[0-9]{6}[A-D]', '\n'.join(lines)) or ['QQ000000A']
    if not lines:
        lines = ['Nobody Atall,1 Nowhere,QQ000000A']
    write_rules(rule('ftp_selftest', 'content:"%s";' % ni[0], 1000901))
    before = (on(C.IDS, 'sudo wc -l %s' % ALERTS, quiet=True)[1].split() or ['0'])[0]
    rc, out = sh("curl --noproxy '*' -sS ftp://%s:%s@%s/trade_secrets/clients.csv | head -3" % (U, C.PW, C.WEB), label="curl --noproxy '*' ftp://USER:PASS@WEB/trade_secrets/clients.csv (sheet command)")
    result('T-FTP', 'OK' if ni[0] in out else 'PROBLEM', 'sheet FTP fetch rc=%d, file content returned: %s' % (rc, ni[0] in out))
    time.sleep(3)
    rc, out = on(C.IDS, 'sudo tail -n +%d %s | grep -c ftp_selftest' % (int(before) + 1 if before.isdigit() else 1, ALERTS))
    result('T-ALERT', 'OK' if out.strip() not in ('', '0') else 'PROBLEM', 'self-test alert lines in %s: %s' % (ALERTS, out.strip()))

    section('HACKERBOT #1: text-based rule')
    bot_goto(1)
    write_rules()      # no rule with the message yet
    r = bot_ready(1, 'mistake: no rule with the message')
    expect('A1-NORULE', r, "Couldn't find an alert rule", 'no rule')
    rules.append(rule(C.MSG[1], 'content:"no-such-text-xyz";', 1000001))
    write_rules()
    r = bot_ready(1, 'mistake: rule never matches')
    expect('A1-NOTRIG', r, "wasn't triggered", 'rule that never fires')
    rules[-1] = rule(C.MSG[1], 'content:"%s";' % ni[0], 1000001)
    write_rules()
    on(C.IDS, 'sudo service snort stop', label='[ids] stop Snort (mistake)')
    r = bot_ready(1, 'mistake: Snort stopped')
    expect('A1-DOWN', r, "Snort isn't running", 'Snort stopped')
    restart_snort()
    r = bot_ready(1, 'correct content rule')
    expect_flag('A1-PASS', r, 'content rule on an NI number')

    section('HACKERBOT #2: regular expression rule + quiz')
    bot_goto(2)
    name = lines[0].split(',')[0].strip('"')
    rules.append(rule(C.MSG[2], 'pcre:"/%s/";' % name, 1000002))
    write_rules()
    r = bot_ready(2, 'mistake: pcre containing a name')
    expect('A2-LITERAL', r, 'contains some of the sensitive data', 'literal name in pcre')
    rules[-1] = rule(C.MSG[2], 'content:"QQ";', 1000002)
    write_rules()
    r = bot_ready(2, 'mistake: content, no pcre')
    expect('A2-NOPCRE', r, "doesn't use", 'no pcre')
    rules[-1] = rule(C.MSG[2], 'pcre:"/QQ[0-9]{6}[A-D]/";', 1000002)
    write_rules()
    r = bot_ready(2, 'correct format pcre')
    if expect_flag('A2-PASS', r, 'pcre on the NI number format'):
        r = bot_answer(2, '/tmp', 'mistake: directory only')
        expect('A2-Q-WRONG', r, 'Incorrect', 'quiz: /tmp')
        rc, out = on(C.WEB, "grep -rlE 'QQ[0-9]{6}[A-D]' /tmp 2>/dev/null", label='[web] grep -rlE (sheet hint)')
        found = [l for l in out.split() if l.startswith('/tmp/')]
        result('A2-Q-FIND', 'OK' if len(found) == 1 else 'PROBLEM', 'files in /tmp matching the NI format: %s' % found)
        if found:
            r = bot_answer(2, found[0], 'correct path')
            expect_flag('A2-Q-PASS', r, 'quiz: resting copy')

    section('HACKERBOT #3: hash-based rule')
    bot_goto(3)
    first = lines[0] + '\n'
    rest = (first + '\n'.join(lines[1:]))[len(name):len(name) + 20]
    md5 = (sh('printf %%s %s | md5sum' % shlex.quote(rest), quiet=True)[1].split() or ['0' * 32])[0]
    pc = 'content:"%s"; protected_content:"%s"; hash:md5; distance:0; length:20;' % (name, md5)
    rules.append(rule(C.MSG[3], pc + ' content:"%s";' % ni[0], 1000003))
    write_rules()
    r = bot_ready(3, 'mistake: protected_content plus an NI number')
    expect('A3-LITERAL', r, 'contains some of the sensitive data', 'literal NI number alongside protected_content')
    rules[-1] = rule(C.MSG[3], pc, 1000003)
    write_rules()
    r = bot_ready(3, 'correct protected_content anchored on a name')
    expect_flag('A3-PASS', r, 'protected_content (md5 of 20 bytes after the first name)')

    section('HACKERBOT #4-8: the incident')
    bot_goto(4)
    on(C.IDS, 'sudo rm -f /tmp/incident.pcap; (sudo setsid nohup tcpdump -i ens19 -w /tmp/incident.pcap >/dev/null 2>&1 &) ; sleep 3; pgrep -x tcpdump', label='[ids] start capture (sheet: tcpdump -w)')
    r = bot_ready(4, 'incident exfiltration')
    asked = 'destination port' in ' '.join(r.lines)
    result('A4-ASK', 'OK' if asked else 'PROBLEM', 'bot asked for the destination port: %s; said %r' % (asked, r.verdict[:120]))
    time.sleep(3)
    on(C.IDS, 'sudo pkill -INT -x tcpdump; sleep 2', label='[ids] stop capture')
    rc, out = on(C.IDS, "sudo tcpdump -nn -A -r /tmp/incident.pcap 'src host %s and dst host %s' 2>/dev/null" % (C.WEB, C.HB), quiet=True)
    port, recs = None, set()
    cur = None
    for l in out.splitlines():
        m = re.search(r'IP [\d.]+\.\d+ > [\d.]+\.(\d+):', l)
        if m:
            cur = m.group(1)
        for nino in re.findall(r'QQ[0-9]{6}[A-D]', l):
            recs.add(nino)
            port = port or cur
    result('A4-PCAP', 'OK' if port and recs else 'PROBLEM', 'from the capture: data went to port %s, %d records (NI numbers) seen' % (port, len(recs)))
    rc, out3632 = on(C.IDS, "sudo tcpdump -nn -r /tmp/incident.pcap 'dst host %s and dst port 3632' 2>/dev/null | head -3" % C.WEB, quiet=True)
    result('A6-PCAP', 'OK' if out3632.strip() else 'PROBLEM', 'traffic to web_server:3632 (distcc) in the capture: %s' % bool(out3632.strip()))
    r = bot_answer(4, '4444', 'mistake: default MSF port')
    expect('A4-Q-WRONG', r, 'Incorrect', 'quiz: 4444')
    if port:
        r = bot_answer(4, port, 'port from the capture')
        expect_flag('A4-Q-PASS', r, 'quiz: destination port')
    r = bot_answer(5, str(len(recs) + 1), 'mistake: off by one')
    expect('A5-WRONG', r, 'Incorrect', 'records: off by one')
    r = bot_answer(5, str(len(recs)), 'records from the capture')
    expect_flag('A5-PASS', r, 'records taken')
    r = bot_answer(6, 'ssh', 'mistake: ssh')
    expect('A6-WRONG', r, 'Incorrect', 'service: ssh')
    r = bot_answer(6, 'distcc', 'service from the capture')
    expect_flag('A6-PASS', r, 'service exploited')
    r = bot_answer(7, 'T1048', 'mistake: T1048')
    expect('A7-WRONG', r, 'Incorrect', 'ATT&CK: T1048')
    r = bot_answer(7, 'T1041', 'Exfiltration Over C2 Channel')
    expect_flag('A7-PASS', r, 'ATT&CK technique')
    r = bot_answer(8, '24', 'mistake: 24')
    expect('A8-WRONG', r, 'Incorrect', 'GDPR: 24')
    r = bot_answer(8, '72', 'Article 33')
    expect_flag('A8-PASS', r, 'GDPR deadline')

    section('HACKERBOT #9: containment with iptables on web_server')
    bot_goto(9)
    r = bot_ready(9, 'mistake: no packet filter')
    expect('A9-LEAK', r, 'I still got a shell', 'no filter')
    on(C.WEB, 'sudo iptables -A INPUT -p tcp --dport 3632 -j DROP; sudo iptables -A INPUT -p tcp --dport 80 -j DROP', label='[web] mistake: block distcc and the website')
    r = bot_ready(9, 'mistake: website blocked too')
    expect('A9-SITEDOWN', r, "website on web_server isn't answering", 'website blocked')
    on(C.WEB, 'sudo iptables -F; sudo iptables -A INPUT -s %s -p tcp --dport 22 -j DROP' % C.HB, label='[web] mistake: block SSH from hackerbot only')
    r = bot_ready(9, 'mistake: SSH from hackerbot blocked')
    expect('A9-NOSSH', r, 'over SSH', 'SSH blocked')
    on(C.WEB, 'sudo iptables -F; sudo iptables -A INPUT -p tcp --dport 3632 -j DROP; sudo iptables -L INPUT -n --line-numbers', label='[web] sheet: iptables -A INPUT -p tcp --dport 3632 -j DROP')
    r = bot_ready(9, 'correct: block distcc only')
    if expect_flag('A9-PASS', r, 'contained'):
        r = bot_answer(9, 'alert', 'mistake: alert')
        expect('A9-Q-WRONG', r, 'Incorrect', 'IPS action: alert')
        r = bot_answer(9, 'drop', 'drop')
        expect_flag('A9-Q-PASS', r, 'IPS action')
    on(C.WEB, 'sudo iptables -F', label='[web] remove the filter')

    section('FLAGS')
    for where, f in flags_seen:
        note('%s: %s' % (where, f))
    n = len({f for _, f in flags_seen})
    result('FLAGS', 'OK' if n == 11 else 'PROBLEM', '%d distinct flags collected (11 expected)' % n)


# --------------------------------------------------------------------------------------------- main
def main():
    global _log_fh, bot
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument('--password', default='tiaspbiqe2r', help="the lab user's password (sudo, ssh)")
    ap.add_argument('--irc-host', default='hackerbot')
    ap.add_argument('--irc-port', type=int, default=6667)
    ap.add_argument('--bot', default='Hackerbot')
    ap.add_argument('--irc-check', action='store_true', help='only reach the bot and print what was discovered')
    ap.add_argument('--yes', action='store_true', help="don't ask for confirmation")
    ap.add_argument('--force', action='store_true', help='run even if the VMs do not look fresh')
    ap.add_argument('--ids-ip', required=True)
    ap.add_argument('--web-ip', required=True)
    args = ap.parse_args()

    _log_fh = open(FULL_LOG, 'w')
    started = datetime.datetime.now()
    C.U = subprocess.run(['whoami'], stdout=subprocess.PIPE).stdout.decode().strip()
    C.IDS, C.WEB, C.PW = args.ids_ip, args.web_ip, args.password
    say('%s_test %s - full log: %s' % (LAB, VERSION, FULL_LOG))
    bot = Hackerbot(args.irc_host, args.irc_port, args.bot, log=log)
    prompts = discover()
    say('Bot reachable: %d attacks' % len(prompts))
    if args.irc_check:
        for n in sorted(prompts):
            say('  #%d: %s' % (n, prompts[n][:150]))
        return
    if not args.yes and input('This modifies the lab VMs (disposable builds only). Type yes: ').strip().lower() != 'yes':
        sys.exit('aborted')

    install_sudo(args.password)
    try:
        for h in (C.IDS, C.WEB):
            setup_ssh(args.password, h)
            remote_sudo_setup(h)
        if not clean_start() and not args.force:
            sys.exit('The VMs do not look freshly built - rebuild for a valid run (or use --force)')
        try:
            run_all(args)
        except Exception as e:                        # keep whatever we have
            import traceback
            log(traceback.format_exc())
            result('SCRIPT', 'ERROR', 'test aborted early: %r (see full log)' % e)
    finally:
        try:
            restore()
        except Exception as e:
            result('CLEANUP', 'ERROR', 'restore failed: %r' % e)
        remove_sudo()
    write_report(['%s automated test report  (script %s)' % (LAB, VERSION),
                  'started %s, took %d min' % (started.strftime('%Y-%m-%d %H:%M'), (datetime.datetime.now() - started).seconds // 60),
                  'host=%s user=%s' % (socket.gethostname(), C.U)])
    say('\nDone. Report: %s   Full log: %s' % (REPORT, FULL_LOG))


if __name__ == '__main__':
    main()
