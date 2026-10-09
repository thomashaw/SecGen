#!/usr/bin/env python3
"""DEV ONLY: automated end-to-end test of the <LAB NAME> Hackerbot lab.

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

LAB = 'mylab'                                   # <- change
VERSION = '0000-00-00.1'                        # bump every time you change it, so reports say which ran
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
    IP = None       # e.g. the second server's IP
    # add what your lab needs: second user, random dir names, ...


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


def setup_ssh(password):
    """Key-based SSH as C.U and as root@here -> C.U@C.IP, pushed once with the lab password via
    SSH_ASKPASS (OpenSSH >= 8.4), so labsheet commands with ssh/scp/rsync run unattended."""
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
def discover():
    """Fill C from the bot's prompts (bot.list_attacks()) and the VM. Exit with a clear message if
    the prompts don't parse - that usually means the bot isn't running or the prompt wording changed."""
    prompts = bot.list_attacks()
    # e.g. m = re.search(r'([\d.]+):/home/([^/]+)/', prompts.get(1, '')); C.IP, bot_user = m.group(1), m.group(2)
    if not prompts:
        sys.exit('No attacks listed - is the bot running?')
    return prompts


def clean_start():
    """Remove artefacts of earlier runs; return False if the VMs don't look freshly built
    (results from a used build aren't trustworthy - rebuild)."""
    section('SETUP: remove artefacts of earlier runs')
    return True


def run_all(args):
    """Walk the labsheet top to bottom. For each section: run its commands with sh()/remote(), check the
    effect, result(...). For each Hackerbot attack: bot_goto(n); make the student mistakes first (each
    followed by bot_ready and a result() on whether the hint says the right thing), clean up, then the
    correct answer (expect a flag). Never restore over live system dirs (/etc ...): restore into a
    scratch copy and diff it against the real one (see references/tester-design.md)."""
    section('LABSHEET: <first section>')
    rc, out = sh('uptime')
    result('EXAMPLE', 'INFO', 'uptime rc=%d' % rc)

    section('FLAGS')
    for where, f in flags_seen:
        note('%s: %s' % (where, f))
    result('FLAGS', 'INFO', '%d distinct flags collected' % len({f for _, f in flags_seen}))


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
    args = ap.parse_args()

    _log_fh = open(FULL_LOG, 'w')
    started = datetime.datetime.now()
    C.U = subprocess.run(['whoami'], stdout=subprocess.PIPE).stdout.decode().strip()
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
        # setup_ssh(args.password)       # if the lab has a second server to reach
        if not clean_start() and not args.force:
            sys.exit('The VMs do not look freshly built - rebuild for a valid run (or use --force)')
        try:
            run_all(args)
        except Exception as e:                        # keep whatever we have
            import traceback
            log(traceback.format_exc())
            result('SCRIPT', 'ERROR', 'test aborted early: %r (see full log)' % e)
    finally:
        remove_sudo()
    write_report(['%s automated test report  (script %s)' % (LAB, VERSION),
                  'started %s, took %d min' % (started.strftime('%Y-%m-%d %H:%M'), (datetime.datetime.now() - started).seconds // 60),
                  'host=%s user=%s' % (socket.gethostname(), C.U)])
    say('\nDone. Report: %s   Full log: %s' % (REPORT, FULL_LOG))


if __name__ == '__main__':
    main()
