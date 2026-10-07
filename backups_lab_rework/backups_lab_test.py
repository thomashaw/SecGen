#!/usr/bin/env python3
"""DEV ONLY: automated end-to-end test of the SecGen backups lab.

Run on the *desktop* VM, as the logged-in (first) user, on a FRESHLY BUILT lab:

    curl -sO http://hackerbot:8080/backups_lab_test.py && python3 backups_lab_test.py

It walks backups_lab.md top to bottom, runs every command the labsheet gives (with the
placeholders filled in), checks each issue from VERIFY_WALKTHROUGH.md (B1-B8, P1-P4), and
solves every Hackerbot challenge by talking to the bot over IRC itself - including
deliberate student-style mistakes, to record what the bot says for each.

Output: ~/backups_lab_report.txt (compact - paste this back) and ~/backups_lab_full_log.txt
(every command and its full output, for digging).

It changes both VMs (that's the point): files in /etc, the second user's home, backups on the
backup_server, a temporary NOPASSWD sudoers drop-in (removed at the end) and SSH keys for
passwordless login to the backup_server (left in place). Only run it on disposable test VMs.

Quick connectivity check without changing anything:  python3 backups_lab_test.py --irc-check
"""
import argparse
import datetime
import os
import random
import re
import shlex
import socket
import subprocess
import sys
import tempfile
import time

VERSION = '2026-10-07.2'
HOME = os.path.expanduser('~')
REPORT = os.path.join(HOME, 'backups_lab_report.txt')
FULL_LOG = os.path.join(HOME, 'backups_lab_full_log.txt')
FLAG_RE = re.compile(r'flag\{[^}]*\}')

# --------------------------------------------------------------------------------------------
# logging / report
# --------------------------------------------------------------------------------------------
_log_fh = None
summary = []        # (id, status, text)
details = []        # lines for the per-section detail part of the report
flags_seen = []     # (where, flag)


def log(text=''):
    _log_fh.write(text + '\n')
    _log_fh.flush()


def say(text):
    """Progress to the terminal (and full log)."""
    print(text, flush=True)
    log(text)


def section(title):
    say('\n=== ' + title)
    details.append('')
    details.append('=' * 100)
    details.append(title)
    details.append('=' * 100)


def note(text):
    details.append(text)
    log('NOTE: ' + text)


def result(rid, status, text):
    """status: CONFIRMED (issue reproduced) | NOT-REPRODUCED | OK | PROBLEM | INFO | ERROR"""
    summary.append((rid, status, text))
    details.append(f'[{status}] {rid}: {text}')
    say(f'  [{status}] {rid}: {text}')


def trim(out, n=12, width=220):
    lines = [l[:width] for l in out.rstrip().splitlines()]
    if len(lines) > n:
        lines = lines[:n // 2] + [f'  ... ({len(lines) - n} lines omitted, see full log) ...'] + lines[-(n - n // 2):]
    return '\n'.join('    | ' + l for l in lines)


# --------------------------------------------------------------------------------------------
# shell helpers
# --------------------------------------------------------------------------------------------
def sh(cmd, timeout=1800, quiet=False, label=None):
    """Run cmd with bash -c as the current user (so ~ expands exactly as for a student).
    stdin is /dev/null so nothing can block on a prompt. Returns (rc, combined output)."""
    start = time.time()
    log(f'\n$ {cmd}')
    try:
        p = subprocess.run(['bash', '-c', cmd], stdin=subprocess.DEVNULL, stdout=subprocess.PIPE,
                           stderr=subprocess.STDOUT, timeout=timeout,
                           env=dict(os.environ, LC_ALL='C', LANG='C'))
        out, rc = p.stdout.decode('utf-8', 'replace'), p.returncode
    except subprocess.TimeoutExpired as e:
        out, rc = (e.output or b'').decode('utf-8', 'replace') + f'\n[TIMEOUT after {timeout}s]', 124
    dur = time.time() - start
    log(out.rstrip())
    log(f'[rc={rc} {dur:.1f}s]')
    if not quiet:
        details.append(f'$ {label or cmd}   -> rc={rc} ({dur:.0f}s)')
        if out.strip():
            details.append(trim(out))
    return rc, out


def remote(cmd, **kw):
    """Run cmd on the backup_server as YOURUSER (key auth, set up in setup_ssh)."""
    return sh(f'ssh -o BatchMode=yes {C.U}@{C.IP} {shlex.quote(cmd)}',
              label=kw.pop('label', None) or f'[backup_server] {cmd}', **kw)


def rcount(path):
    """Number of regular files under a path on the backup_server (-1 if missing)."""
    rc, out = remote(f'test -e {path} && find {path} -type f | wc -l || echo -1', quiet=True)
    try:
        return int(out.strip().splitlines()[-1])
    except (ValueError, IndexError):
        return -1


def lcount(path):
    rc, out = sh(f'sudo test -e {path} && sudo find {path} -type f | wc -l || echo -1', quiet=True)
    try:
        return int(out.strip().splitlines()[-1])
    except (ValueError, IndexError):
        return -1


def transferred(rsync_out):
    """Files rsync -v listed as sent (excludes dirs and the summary lines)."""
    skip = re.compile(r'^(sending|receiving|sent |total size|created directory|building file list|'
                      r'deleting |\s*$|rsync|cannot delete|--compare-dest|WARNING|skipping)')
    return [l for l in rsync_out.splitlines() if not skip.match(l) and not l.endswith('/')]


def warnings_in(out):
    return [l for l in out.splitlines() if re.search(r'warn|error|does not exist|denied|failed', l, re.I)]


class C:
    """Discovered lab values."""
    U = S = IP = BIN_DIR = None
    BOT = 'Hackerbot'


# --------------------------------------------------------------------------------------------
# IRC client for talking to Hackerbot
# --------------------------------------------------------------------------------------------
REPEAT_RE = re.compile(r"^Let me know when you are 'ready'|^Say 'ready', 'next', or 'previous'\.")
SAY_READY_RE = re.compile(r"simply say 'ready'|^'Ready'\?$")
ANSWER_DONE_RE = re.compile(r"simply say 'ready'|^'Ready'\?$|^Incorrect|^There is no question")


class IRC:
    def __init__(self, host, port, bot):
        self.bot = bot
        self.buf = b''
        self.sock = socket.create_connection((host, port), timeout=20)
        self.nick = 'labtest%04d' % random.randint(0, 9999)
        self._send(f'NICK {self.nick}')
        self._send(f'USER {self.nick} 0 * :backups lab automated test')
        end = time.time() + 40
        while time.time() < end:
            line = self._readline(end - time.time())
            if line is None:
                continue
            parts = line.split()
            if len(parts) > 1 and parts[1] == '001':
                log(f'IRC: registered as {self.nick}')
                return
            if len(parts) > 1 and parts[1] == '433':
                self.nick += 'x'
                self._send(f'NICK {self.nick}')
        raise RuntimeError('IRC registration timed out')

    def _send(self, s):
        self.sock.sendall((s + '\r\n').encode())

    def _readline(self, timeout):
        end = time.time() + max(0.0, timeout)
        while b'\n' not in self.buf:
            rem = end - time.time()
            if rem <= 0:
                return None
            self.sock.settimeout(rem)
            try:
                data = self.sock.recv(4096)
            except socket.timeout:
                return None
            if not data:
                raise RuntimeError('IRC connection closed by server')
            self.buf += data
        line, _, self.buf = self.buf.partition(b'\n')
        line = line.rstrip(b'\r').decode('utf-8', 'replace')
        if line.startswith('PING'):
            self._send('PONG' + line[4:])
            return ''
        return line

    def tell(self, msg, until=None, timeout=330, idle=None):
        """PRIVMSG the bot, collect its replies until `until` matches a reply, or (if idle is set)
        until no reply for `idle` seconds after at least one reply, or timeout."""
        log(f'  ME> {msg}')
        self._send(f'PRIVMSG {self.bot} :{msg}')
        got, end = [], time.time() + timeout
        while time.time() < end:
            wait = end - time.time()
            if idle and got:
                wait = min(wait, idle)
            line = self._readline(wait)
            if line is None:
                if idle and got:
                    return got, True
                break
            m = re.match(r':([^!\s]+)!\S* PRIVMSG \S+ :(.*)$', line)
            if not m or m.group(1).lower() != self.bot.lower():
                continue
            text = m.group(2)
            got.append(text)
            log(f'  BOT< {text}')
            if until is not None and until.search(text):
                # pick up anything sent in the same burst
                while True:
                    extra = self._readline(1.5)
                    if not extra:
                        break
                    m2 = re.match(r':([^!\s]+)!\S* PRIVMSG \S+ :(.*)$', extra)
                    if m2 and m2.group(1).lower() == self.bot.lower():
                        got.append(m2.group(2))
                        log(f'  BOT< {m2.group(2)}')
                return got, True
        return got, False


irc = None


def bot_key_lines(lines):
    keep = re.compile(r'^:\)|^:\(|^Ok,|^I just deleted|^Oh no|^Took too long|^Correct|^Incorrect|'
                      r'^\*\* #|^Looks like|^Access the backups|flag\{')
    out = [l for l in lines if keep.search(l)]
    starts = [i for i, l in enumerate(lines) if l.startswith('FYI:')]
    if starts:
        # FYI output continues on the following lines until the bot's next "real" message
        block = [lines[starts[0]]]
        for l in lines[starts[0] + 1:]:
            if keep.search(l) or REPEAT_RE.search(l):
                break
            block.append(l)
        out.insert(0, ' / '.join(block)[:400])
    return out


def bot_goto(n):
    lines, ok = irc.tell(f'goto {n}', until=SAY_READY_RE, timeout=30)
    if not ok:
        note(f'(goto {n}: no say_ready reply; got {lines})')
    return lines


def bot_ready(rid, n, what):
    """Say ready on attack n; record the bot's verdict. Returns (lines, flags)."""
    say(f'  ... ready on attack {n}: {what}')
    t0 = time.time()
    lines, ok = irc.tell('ready', until=REPEAT_RE, timeout=330)
    flags = FLAG_RE.findall('\n'.join(lines))
    for f in flags:
        flags_seen.append((f'attack {n}: {what}', f))
    details.append(f'BOT #{n} ready ({what}) [{time.time() - t0:.0f}s]{"" if ok else " [NO END-OF-REPLY SEEN]"}:')
    for l in bot_key_lines(lines):
        details.append('    > ' + l[:300])
    return lines, flags


def verdict(lines):
    """The bot's condition message (the :) / :( / Ok line) for compact reporting."""
    for l in lines:
        if re.match(r'^(:\)|:\(|Ok,|I just deleted|Oh no)', l):
            return FLAG_RE.sub('flag{..}', l)[:200]
    return '(no verdict line) ' + ' | '.join(lines)[:200]


# --------------------------------------------------------------------------------------------
# /etc restores are tested against a scratch copy of /etc, never the live one (see below).
# --------------------------------------------------------------------------------------------
def etc_restore_check(rid, label, restore_cmds, expect_files):
    """Run the labsheet's /etc restore commands, but into a scratch copy of /etc (DEST) instead of the live
    /etc, then compare types/modes/owners and symlink counts with the real /etc. Afterwards copy just the
    files the sheet deleted back into the live /etc, so the rest of the labsheet carries on as for a student.
    (Run 1 restored live /etc and turned ~840 symlinks into files - this way nothing can break the VM.)"""
    T = '/tmp/etc_restore_test'
    body = '\n'.join(c.replace('{DEST}', T + '/') for c in restore_cmds)
    files = ' '.join(expect_files)
    script = f'''
rm -rf {T}; cp -a /etc {T}
{body}
( cd /etc && find . -xdev -printf '%y %m %U %G %p\\n' | sort ) > /root/.lt_a
( cd {T} && find . -xdev -printf '%y %m %U %G %p\\n' | sort ) > /root/.lt_b
echo "=== DIFF"
diff /root/.lt_a /root/.lt_b | grep '^[<>]'
echo "=== SYMLINKS etc=$(find /etc -xdev -type l | wc -l) restored=$(find {T} -xdev -type l | wc -l)"
echo "=== SUDOERS"; stat -c '%a %U:%G %n' {T}/sudoers {T}/sudoers.d/* 2>&1
for f in {files}; do b=${{f#/etc/}}; if [ -e {T}/$b ]; then echo "RESTORED $f $(stat -c '%a %U:%G' {T}/$b)"; cp -a {T}/$b $f; else echo "NOT-RESTORED $f"; fi; done
'''
    rc, out = sh(f'sudo bash -c {shlex.quote(script)}', quiet=True)
    details.append(f'$ [restore into scratch copy of /etc] {label}: ' + ' ; '.join(c.replace("{DEST}", "/etc/") for c in restore_cmds) + f'   -> rc={rc}')
    expected = {f[len('/etc/'):] for f in expect_files}
    diff = re.search(r'=== DIFF\n(.*?)=== SYMLINKS', out, re.S)
    lines = [l for l in (diff.group(1).splitlines() if diff else [])
             if not any(l.endswith(' ./' + e) for e in expected)]
    changed = [l for l in lines if l.startswith('>')]
    sym = re.search(r'=== SYMLINKS etc=(\d+) restored=(\d+)', out)
    sudo_bad = [l for l in out.splitlines() if re.match(r'(?!440)\d+ \S+ \S+/sudoers', l)]
    missing = re.findall(r'NOT-RESTORED (\S+)', out)
    restored = re.findall(r'RESTORED (\S+ \S+ \S+)', out)
    details.append(trim('\n'.join(changed[:20]) or '(no unexpected type/mode/owner changes)'))
    status = 'OK' if not changed and not missing and not sudo_bad and sym and sym.group(1) == sym.group(2) else 'PROBLEM'
    result(rid, status, f'{label}: {len(changed)} other /etc entries differ after the restore'
                        f'{" e.g. " + "; ".join(changed[:3]) if changed else ""}; symlinks /etc={sym.group(1) if sym else "?"} '
                        f'restored={sym.group(2) if sym else "?"}; sudoers modes bad: {sudo_bad or "none"}; '
                        f'restored: {restored}; missing: {missing or "none"}')
    return out


def install_sudo(password):
    rc, out = sh('sudo -n true', quiet=True)
    if rc == 0:
        log('sudo already passwordless')
        return
    line = f'{C.U} ALL=(ALL) NOPASSWD: ALL'
    p = subprocess.run(['sudo', '-S', '-p', '', 'bash', '-c',
                        f'echo {shlex.quote(line)} > /etc/sudoers.d/zz-backups-labtest && '
                        f'chmod 440 /etc/sudoers.d/zz-backups-labtest && visudo -cf /etc/sudoers.d/zz-backups-labtest'],
                       input=(password + '\n').encode(), stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    log(p.stdout.decode())
    if subprocess.run(['sudo', '-n', 'true']).returncode != 0:
        sys.exit('Could not enable passwordless sudo - is --password right?')


def remove_sudo():
    sh('sudo rm -f /etc/sudoers.d/zz-backups-labtest', quiet=True)


def discover():
    lines, _ = irc.tell('list', idle=6, timeout=40)
    prompts = {}
    for l in lines:
        m = re.match(r'^(?:--> )?attack (\d+): (.*)$', l)
        if m:
            prompts[int(m.group(1))] = m.group(2)
    p1, p2 = prompts.get(1, ''), prompts.get(2, '')
    m1 = re.search(r'([\d.]+):/home/([^/]+)/(remote-bin-backup-[0-9a-f]+)/', p1)
    m2 = re.search(r'remote backups for (\S+) \(a user', p2)
    if not (m1 and m2):
        sys.exit(f'Could not parse the bot prompts (got {len(prompts)} attacks). Is the bot running? Lines: {lines[:5]}')
    return prompts, m1.group(1), m1.group(2), m1.group(3), m2.group(1)


def setup_ssh(password):
    section('SETUP: passwordless SSH to the backup_server (for automation only)')
    sh('mkdir -p ~/.ssh && chmod 700 ~/.ssh && { [ -f ~/.ssh/id_ed25519 ] || ssh-keygen -q -t ed25519 -N "" -f ~/.ssh/id_ed25519; }', quiet=True)
    sh('sudo bash -c \'mkdir -p /root/.ssh && { [ -f /root/.ssh/id_ed25519 ] || ssh-keygen -q -t ed25519 -N "" -f /root/.ssh/id_ed25519; }\'', quiet=True)
    sh(f'ssh-keyscan -H {C.IP} 2>/dev/null >> ~/.ssh/known_hosts; sudo bash -c "ssh-keyscan -H {C.IP} 2>/dev/null >> /root/.ssh/known_hosts"', quiet=True)
    rc, _ = sh(f'ssh -o BatchMode=yes {C.U}@{C.IP} true && sudo ssh -o BatchMode=yes {C.U}@{C.IP} true', quiet=True)
    if rc != 0:
        fd, askpass = tempfile.mkstemp(prefix='askpass')
        with os.fdopen(fd, 'w') as f:
            f.write('#!/bin/sh\necho ' + shlex.quote(password) + '\n')
        os.chmod(askpass, 0o700)
        try:
            pubs = sh('cat ~/.ssh/id_ed25519.pub; sudo cat /root/.ssh/id_ed25519.pub', quiet=True)[1]
            log('$ (push both public keys to backup_server authorized_keys via SSH_ASKPASS)')
            p = subprocess.run(['ssh', '-o', 'PubkeyAuthentication=no', '-o', 'StrictHostKeyChecking=accept-new',
                                f'{C.U}@{C.IP}', 'umask 077; mkdir -p ~/.ssh; cat >> ~/.ssh/authorized_keys'],
                               input=pubs.encode(), stdout=subprocess.PIPE, stderr=subprocess.STDOUT, timeout=60,
                               env=dict(os.environ, SSH_ASKPASS=askpass, SSH_ASKPASS_REQUIRE='force', DISPLAY=':0'))
            log(p.stdout.decode())
        finally:
            os.unlink(askpass)
    rc, out = sh(f'ssh -o BatchMode=yes {C.U}@{C.IP} true && sudo ssh -o BatchMode=yes {C.U}@{C.IP} true && echo KEYS_OK', quiet=True)
    if 'KEYS_OK' not in out:
        sys.exit('Could not set up key-based SSH to the backup_server (password auth off? wrong --password?). See ' + FULL_LOG)
    note('Key-based SSH as YOURUSER and as root@desktop -> YOURUSER@backup_server is set up; labsheet commands run verbatim.')


def clean_start():
    section('SETUP: remove artefacts of earlier runs')
    remote('rm -rf ~/ssh_etc_backup ~/ssh_backup ~/scp_backup ~/remote-* ~/b2_* ~/b8_* ~/p2_* ~/incr1_saved ~/incr2_aside', quiet=True)
    sh('sudo rm -rf ~/backups ~/b1 /tmp/b2src /tmp/b2_r* /tmp/b8 /tmp/p3 /tmp/etc_restore_test; '
       'sudo rm -f /etc/hi /etc/hello /etc/test1 /etc/test2 /etc/test3 /etc/test4 /etc/b1test', quiet=True)
    rc, _ = sh(f'sudo test -d /home/{C.S}/trade_secrets && sudo test ! -e /home/{C.S}/notes', quiet=True)
    return rc == 0


# --------------------------------------------------------------------------------------------
# the test, top to bottom through backups_lab.md
# --------------------------------------------------------------------------------------------
def run_all(args):
    U, S, IP = C.U, C.S, C.IP
    H = f'/home/{U}'
    FULL, D1, D2 = f'{H}/remote-rsync-full-backup', f'{H}/remote-rsync-differential1', f'{H}/remote-rsync-differential2'
    I1, I2 = f'{H}/remote-rsync-incremental1', f'{H}/remote-rsync-incremental2'
    M = '-M--fake-super'

    def bk(dest, *compare, src=f'/home/{S}', label=None):
        """A SECONDUSER backup the way the new labsheet teaches it."""
        cd = ''.join(f' --compare-dest={c}/' for c in compare)
        return sh(f'sudo rsync -avzh {M} {src}{cd} {U}@{IP}:{dest}/', label=label)

    def said(lines, text):
        return text in ' '.join(lines)

    # ---------------------------------------------------------------- Getting started
    section('LABSHEET: Getting started')
    rc, out = sh('ls /home')
    users = set(out.split())
    result('N4', 'OK' if users <= {U, S, 'vagrant'} else 'PROBLEM',
           f'ls /home -> {sorted(users)} (sheet now says: you, vagrant (ignore), and SECONDUSER={S})')
    sh('uptime')

    # ---------------------------------------------------------------- Copy
    section('LABSHEET: Copy')
    sh('mkdir ~/backups/')
    sh('cp /etc/passwd ~/backups/')
    _, a = sh('ls -la ~/backups/passwd')
    _, b = sh('ls -la /etc/passwd')
    result('L-copy', 'INFO', f'cp loses ownership as the sheet says: backup {a.split()[2:4]} vs original {b.split()[2:4]}')

    # ---------------------------------------------------------------- SCP (new section)
    section('LABSHEET: SSH/SCP (rewritten: /etc/ssh into a pre-created scp_backup/)')
    rc0, _ = sh(f'ssh {U}@{IP} mkdir -p scp_backup')
    rc1, out1 = sh(f'sudo scp -pr /etc/ssh {U}@{IP}:{H}/scp_backup/')
    sh('sudo bash -c \'echo "# backup test" >> /etc/ssh/ssh_config\'')
    rc2, out2 = sh(f'sudo scp -pr /etc/ssh {U}@{IP}:{H}/scp_backup/')
    _, lay = remote('ls -la scp_backup/ssh/ | head -6; test -f scp_backup/ssh/ssh_config && tail -1 scp_backup/ssh/ssh_config')
    ok = rc0 == 0 and rc1 == 0 and rc2 == 0 and '# backup test' in lay
    result('N2/B6', 'OK' if ok else 'PROBLEM',
           f'mkdir rc={rc0}, 1st scp rc={rc1}, 2nd scp rc={rc2}, scp_backup/ssh has the change: {"# backup test" in lay}'
           f'{"; errors: " + warnings_in(out1 + out2)[0][:120] if warnings_in(out1 + out2) else ""}')

    # B8 semantics, unchanged from run 1 (the sheet now teaches this rule)
    sh('mkdir -p /tmp/b8/d && echo x > /tmp/b8/d/f', quiet=True)
    remote('mkdir -p b8_exists', quiet=True)
    sh(f'scp -r /tmp/b8/d {IP}:{H}/b8_new', quiet=True)
    sh(f'scp -r /tmp/b8/d {IP}:{H}/b8_exists/', quiet=True)
    _, tree = remote('find b8_* | sort', quiet=True)
    t = set(tree.split())
    result('B8', 'OK' if 'b8_new/f' in t and 'b8_exists/d/f' in t else 'PROBLEM', f'scp rule as taught in the sheet: {sorted(t)}')
    remote('rm -rf b8_*', quiet=True)

    # ---------------------------------------------------------------- Attack 1
    section('BOT: Attack 1 (scp /usr/bin)')
    bot_goto(1)
    lines, _ = bot_ready('A1', 1, 'nothing copied yet')
    result('A1-none', 'OK' if said(lines, "There's no") else 'PROBLEM', 'nothing copied -> ' + verdict(lines))
    t = time.time()
    sh(f'scp -rq /usr/bin {IP}:{H}/{C.BIN_DIR}', label=f'(mistake: dest dir not created first) scp -rq /usr/bin {IP}:{H}/{C.BIN_DIR}')
    lines, _ = bot_ready('A1', 1, 'scp to a not-yet-existing dir (contents, no bin/)')
    result('A1-nobin', 'OK' if said(lines, 'contents* of bin') else 'PROBLEM', 'contents without bin/ -> ' + verdict(lines))
    remote(f'rm -rf {C.BIN_DIR}', quiet=True)
    sh(f'ssh {U}@{IP} mkdir -p {C.BIN_DIR}')
    sh(f'scp -rq /usr/bin {IP}:{H}/{C.BIN_DIR}/')
    dur = time.time() - t
    lines, fl = bot_ready('A1', 1, 'correct: mkdir then scp -r /usr/bin into it')
    result('A1', 'OK' if fl else 'PROBLEM', f'correct solution ({dur:.0f}s for both copies) -> ' + verdict(lines))

    # ---------------------------------------------------------------- rsync local + remote + -M
    section('LABSHEET: Rsync local, remote, -M--fake-super')
    sh('sudo rsync -av /etc ~/backups/rsync_backup/', quiet=True)
    sh("sudo bash -c 'echo hello > /etc/hello'")
    rc, out = sh('sudo rsync -av /etc ~/backups/rsync_backup/')
    result('L-rsync-local', 'OK' if transferred(out) == ['etc/hello'] else 'INFO', f'2nd local rsync transferred {transferred(out)[:6]}')
    sh(f'sudo rsync -avzh {M} /etc {U}@{IP}:{H}/remote-rsync-backup/')
    _, srv = remote("stat -c '%a %U:%G' remote-rsync-backup/etc/shadow; python3 -c 'import os;print(os.listxattr(\"remote-rsync-backup/etc/shadow\"))'")
    result('B2-server', 'OK' if 'user.rsync.%stat' in srv else 'PROBLEM',
           f'server copy of etc/shadow now records the real owner in an xattr: {" ".join(srv.split())}')
    sh('sudo rm /etc/hello')
    sh(f'sudo rsync -avzh {M} /etc {U}@{IP}:{H}/remote-rsync-backup/', quiet=True)
    sh(f'sudo rsync -avz {M} {U}@{IP}:{H}/remote-rsync-backup/etc/hello /etc/')
    _, own = sh('stat -c "%a %U:%G" /etc/hello; ls -l /etc/hello', label='sheet: check that the ownership survived')
    _, own_srv = remote('ls -l remote-rsync-backup/etc/hello')
    result('L-restore-hello', 'OK' if own.split()[1:2] == ['root:root'] else 'PROBLEM',
           f'/etc/hello restored with -M: {own.split()[0:2]}; server copy: {" ".join(own_srv.split()[0:4])}')
    sh('sudo rm /etc/hello')
    sh(f'sudo rsync -avzh {M} --delete /etc {U}@{IP}:{H}/remote-rsync-backup/')
    rc, _ = remote('test ! -e remote-rsync-backup/etc/hello')
    result('L-delete', 'OK' if rc == 0 else 'PROBLEM', '--delete removed etc/hello from the server')

    # ---------------------------------------------------------------- Attack 2
    section('BOT: Attack 2 (full backup)')
    rc, out = sh(f'rsync -av /home/{S} /tmp/p3/', label='(P3) no sudo')
    result('P3', 'INFO', f'non-sudo rsync rc={rc}: {warnings_in(out)[:1]} (sheet now explains why sudo)')
    sh('rm -rf /tmp/p3', quiet=True)
    bot_goto(2)
    rc, out = sh(f'sudo rsync -avzh {M} /home/{S} {U}@{IP}:{FULL}/{S}', label='(mistake: names SECONDUSER in the dest, parent missing)')
    result('N6', 'INFO', f'rc={rc} {warnings_in(out)[:1]} (sheet now explains this error)')
    lines, _ = bot_ready('A2', 2, 'no backup yet')
    result('A2-nodir', 'OK' if said(lines, "can't find") else 'PROBLEM', 'no backup -> ' + verdict(lines))
    bk(FULL, src=f'/home/{S}/', label='(mistake: trailing slash on source) contents into remote-rsync-full-backup/')
    lines, _ = bot_ready('A2', 2, 'contents at top')
    result('A2-top', 'OK' if said(lines, 'contents* of') else 'PROBLEM', 'contents at top -> ' + verdict(lines))
    remote(f'rm -rf {FULL} && mkdir -p {FULL}/{S}', quiet=True)
    bk(f'{FULL}/{S}', label='(mistake: nested) dest .../remote-rsync-full-backup/SECONDUSER/')
    lines, _ = bot_ready('A2', 2, 'nested')
    result('A2-nested', 'OK' if said(lines, 'nested') else 'PROBLEM', 'nested -> ' + verdict(lines))
    remote(f'rm -rf {FULL}', quiet=True)
    bk(FULL)
    lines, fl = bot_ready('A2', 2, 'correct full backup')
    result('A2', 'OK' if fl else 'PROBLEM', 'correct full backup -> ' + verdict(lines))

    # ---------------------------------------------------------------- Differential (/etc)
    section('LABSHEET: Differential backups (/etc) + B1 fix + restore into a scratch copy')
    sh("sudo bash -c 'echo \"hello there\" > /etc/hello'")
    rc, out = sh('sudo rsync -av /etc --compare-dest=$HOME/backups/rsync_backup/ ~/backups/rsync_backup_week1/')
    n_local = lcount('~/backups/rsync_backup_week1')
    sh(f'sudo rsync -avzh {M} /etc --compare-dest={H}/remote-rsync-backup/ {U}@{IP}:{H}/remote-rsync-backup-week1/')
    n_remote = rcount('remote-rsync-backup-week1')
    result('B1', 'OK' if n_local <= 5 else 'PROBLEM', f'week1 differential with $HOME: local {n_local} files, remote {n_remote} files')
    sh("sudo bash -c 'echo \"hello there!\" > /etc/hi'")
    sh('sudo rsync -av /etc --compare-dest=$HOME/backups/rsync_backup/ ~/backups/rsync_backup_week2/')
    sh(f'sudo rsync -avzh {M} /etc --compare-dest={H}/remote-rsync-backup/ {U}@{IP}:{H}/remote-rsync-backup-week2/')
    note(f'week2: local {lcount("~/backups/rsync_backup_week2")} / remote {rcount("remote-rsync-backup-week2")} files')
    sh('sudo rm /etc/wgetrc /etc/hello')
    etc_restore_check('N1-diff', 'remote restore full -> week2 (sheet commands, -M)',
                      [f'rsync -avz {M} {U}@{IP}:{H}/remote-rsync-backup/etc/ {{DEST}}',
                       f'rsync -avz {M} {U}@{IP}:{H}/remote-rsync-backup-week2/etc/ {{DEST}}'],
                      ['/etc/wgetrc', '/etc/hello'])
    sh('sudo rm /etc/wgetrc /etc/hello')
    etc_restore_check('N1-local', 'local restore (sheet: "try restoring from the local copy")',
                      [f'rsync -av {H}/backups/rsync_backup/etc/ {{DEST}}', f'rsync -av {H}/backups/rsync_backup_week2/etc/ {{DEST}}'],
                      ['/etc/wgetrc', '/etc/hello'])

    # ---------------------------------------------------------------- Attack 3, 4
    section('BOT: Attacks 3-4 (step 3 changes, differential1)')
    bot_goto(3)
    lines, _ = bot_ready('A3', 3, 'step 3 changes')
    result('A3', 'OK' if said(lines, 'has made their changes') else 'PROBLEM', verdict(lines))
    _, hf = sh(f'sudo cat /home/{S}/personal_secrets/flag')
    for f in FLAG_RE.findall(hf):
        flags_seen.append(('attack 3 hidden file', f))
    _, own = sh(f'sudo stat -c "%U %n" /home/{S}/notes /home/{S}/personal_secrets/flag')
    note(f'step-3 files owned by: {" ".join(own.split())}')
    bot_goto(4)
    bk(D1, label='(mistake: no --compare-dest)')
    lines, _ = bot_ready('A4', 4, 'full copy')
    result('A4-full', 'OK' if said(lines, '--compare-dest') else 'PROBLEM', 'no --compare-dest -> ' + verdict(lines))
    remote(f'rm -rf {D1}', quiet=True)
    bk(D1, f'{FULL}/{S}', label='(P2 mistake: compare-dest one level too deep)')
    lines, _ = bot_ready('A4', 4, 'compare-dest too deep')
    result('P2', 'OK' if said(lines, '--compare-dest') else 'PROBLEM', 'mismatched --compare-dest -> ' + verdict(lines))
    remote(f'rm -rf {D1}', quiet=True)
    bk(D1, FULL)
    lines, fl = bot_ready('A4', 4, 'correct differential1')
    result('A4', 'OK' if fl else 'PROBLEM', 'correct differential1 -> ' + verdict(lines))

    # ---------------------------------------------------------------- Incremental (/etc)
    section('LABSHEET: Incremental backups (/etc) + restore into a scratch copy')
    sh("sudo bash -c 'echo \"Another test change\" > /etc/test1'")
    sh("sudo bash -c 'echo \"Another test change\" > /etc/hello'")
    sh('sudo rsync -av /etc --compare-dest=$HOME/backups/rsync_backup/ --compare-dest=$HOME/backups/rsync_backup_week2/ ~/backups/rsync_backup_monday/')
    sh(f'sudo rsync -avzh {M} /etc --compare-dest={H}/remote-rsync-backup/ --compare-dest={H}/remote-rsync-backup-week2/ {U}@{IP}:{H}/remote-rsync-backup-monday/')
    sh("sudo bash -c 'echo \"Another test change\" > /etc/test2'")
    sh('sudo rsync -av /etc --compare-dest=$HOME/backups/rsync_backup/ --compare-dest=$HOME/backups/rsync_backup_week2/ --compare-dest=$HOME/backups/rsync_backup_monday/ ~/backups/rsync_backup_tuesday/')
    sh(f'sudo rsync -avzh {M} /etc --compare-dest={H}/remote-rsync-backup/ --compare-dest={H}/remote-rsync-backup-week2/ --compare-dest={H}/remote-rsync-backup-monday/ {U}@{IP}:{H}/remote-rsync-backup-tuesday/')
    note(f'monday: local {lcount("~/backups/rsync_backup_monday")} / remote {rcount("remote-rsync-backup-monday")}; '
         f'tuesday: local {lcount("~/backups/rsync_backup_tuesday")} / remote {rcount("remote-rsync-backup-tuesday")} files')
    sh('sudo rm /etc/wgetrc /etc/hello /etc/test1 /etc/test2')
    etc_restore_check('N1-incr', 'restore full -> week2 -> monday -> tuesday (remote, -M)',
                      [f'rsync -avz {M} {U}@{IP}:{H}/remote-rsync-backup{s}/etc/ {{DEST}}' for s in ['', '-week2', '-monday', '-tuesday']],
                      ['/etc/wgetrc', '/etc/hello', '/etc/test1', '/etc/test2'])

    # ---------------------------------------------------------------- Attack 5, 6
    section('BOT: Attacks 5-6 (step 5 changes, differential2)')
    bot_goto(5)
    lines, _ = bot_ready('A5', 5, 'step 5 changes')
    result('A5', 'OK' if said(lines, 'has made more changes') else 'PROBLEM', verdict(lines))
    bot_goto(6)
    bk(D2, FULL, src=f'/home/{S}/', label='(mistake: trailing slash on source)')
    lines, _ = bot_ready('A6', 6, 'contents at top')
    result('A6-top', 'OK' if said(lines, 'contents* of') else 'PROBLEM', 'contents at top -> ' + verdict(lines))
    remote(f'rm -rf {D2}', quiet=True)
    bk(D2, FULL, D1, label='(mistake: incremental instead of differential)')
    lines, _ = bot_ready('A6', 6, 'incremental instead of differential')
    result('A6-incr', 'OK' if said(lines, "step 3's") else 'PROBLEM', 'compared against diff1 too -> ' + verdict(lines))
    remote(f'rm -rf {D2}', quiet=True)
    bk(D2, FULL)
    lines, fl = bot_ready('A6', 6, 'correct differential2')
    result('A6', 'OK' if fl else 'PROBLEM', 'correct differential2 -> ' + verdict(lines))

    # ---------------------------------------------------------------- Attack 7, 8
    section('BOT: Attacks 7-8 (step 7 changes, incremental1) + B4 fix')
    bot_goto(7)
    lines, _ = bot_ready('A7', 7, 'step 7 changes')
    result('A7', 'OK' if said(lines, 'has made more changes') else 'PROBLEM', verdict(lines))
    bot_goto(8)
    bk(I1, FULL, label='(mistake: forgot differential2 --compare-dest)')
    lines, _ = bot_ready('A8', 8, 'forgot diff2 compare-dest')
    result('B4', 'OK' if said(lines, 'second --compare-dest') else 'PROBLEM', 'forgot diff2 --compare-dest -> ' + verdict(lines))
    remote(f'rm -rf {I1}', quiet=True)
    bk(I1, FULL, D2)
    lines, fl = bot_ready('A8', 8, 'correct incremental1')
    result('A8', 'OK' if fl else 'PROBLEM', 'correct incremental1 -> ' + verdict(lines))

    # ---------------------------------------------------------------- Snapshots (/etc)
    section('LABSHEET: Snapshots (/etc) with $HOME and linking to the previous snapshot')
    sh('sudo rsync -av --delete --link-dest=$HOME/backups/rsync_backup/ /etc ~/backups/rsync_backup_snapshot_1', quiet=True)
    _, l1 = sh('sudo find ~/backups/rsync_backup_snapshot_1 -type f -links +1 | wc -l; sudo du -sh ~/backups/rsync_backup ~/backups/rsync_backup_snapshot_1')
    sh("sudo bash -c 'echo \"Another test change\" > /etc/test3'")
    sh("sudo bash -c 'echo \"Another test change\" > /etc/test4'")
    sh('sudo rsync -av --delete --link-dest=$HOME/backups/rsync_backup_snapshot_1/ /etc ~/backups/rsync_backup_snapshot_2', quiet=True)
    _, l2 = sh('sudo find ~/backups/rsync_backup_snapshot_2 -type f -links +1 | wc -l')
    n1 = int(l1.split()[0]) if l1.split() and l1.split()[0].isdigit() else 0
    result('B1-linkdest', 'OK' if n1 > 100 else 'PROBLEM', f'snapshot_1 hard links: {n1}; snapshot_2 hard links: {l2.strip()}; du: {" ".join(l1.split()[1:])}')

    # ---------------------------------------------------------------- Attack 9, 10 (+ rewind, quiz)
    section('BOT: Attacks 9-10 (step 9, incremental2) + B5 + REWIND with goto 7 + quiz')
    bot_goto(9)
    lines, _ = bot_ready('A9', 9, 'step 9 changes')
    result('A9', 'OK' if said(lines, 'has made more changes') else 'PROBLEM', verdict(lines))
    bot_goto(10)
    remote(f'rm -rf {I1}', label='(B5 mistake) delete incremental1 and redo it now, after step 9')
    bk(I1, FULL, D2)
    bk(I2, FULL, D2, I1)
    lines, _ = bot_ready('A10', 10, 'incremental1 redone late -> empty incremental2')
    result('B5', 'OK' if said(lines, 'redone after step 9') else 'PROBLEM', 'late incr1 -> ' + verdict(lines))
    # the rewind workflow the bot suggests
    remote(f'rm -rf {I1} {I2}', quiet=True)
    bot_goto(7)
    lines, _ = bot_ready('A7', 7, 'REWIND to step 7')
    _, st = sh(f'sudo ls /home/{S}/personal_secrets/; sudo cat /home/{S}/notes')
    result('REWIND-7', 'OK' if said(lines, 'has made more changes') and 'nothing_much' not in st else 'PROBLEM',
           f'goto 7 + ready -> {verdict(lines)}; files now: {" ".join(st.split())[:160]}')
    bk(I1, FULL, D2)
    bot_goto(8)
    lines, fl = bot_ready('A8', 8, 'incremental1 after rewind')
    result('REWIND-8', 'OK' if said(lines, 'Well done') else 'PROBLEM', 'incr1 retaken after the rewind -> ' + verdict(lines))
    bot_goto(9)
    bot_ready('A9', 9, 'step 9 again')
    bot_goto(10)
    bk(I2, FULL, D2, I1)
    lines, fl = bot_ready('A10', 10, 'correct incremental2')
    result('A10', 'OK' if fl else 'PROBLEM', 'correct incremental2 -> ' + verdict(lines))
    _, desk = sh(f'sudo cat /home/{S}/notes', label='(quiz) desktop notes - should NOT be the answer')
    lines, _ = irc.tell(f'answer {desk.strip()}', until=ANSWER_DONE_RE, timeout=40)
    wrong_ok = any(l.startswith('Incorrect') for l in lines)
    _, ans = remote(f'cat {I1}/{S}/notes', label='(quiz) incremental1 notes on the backup_server')
    lines, _ = irc.tell(f'answer {ans.strip()}', until=ANSWER_DONE_RE, timeout=40)
    for f in FLAG_RE.findall('\n'.join(lines)):
        flags_seen.append(('attack 10 quiz', f))
    right_ok = any(l.startswith('Correct') for l in lines)
    result('Q10', 'OK' if wrong_ok and right_ok else 'PROBLEM',
           f'desktop notes answer rejected: {wrong_ok}; incremental1 notes ({ans.strip()!r}) accepted: {right_ok}')

    # ---------------------------------------------------------------- Attack 11 (gate) and 12 (restore, reset, ownership)
    section('BOT: Attack 11 (safety gate) + 12 (restore, ownership, goto-11 reset)')
    bot_goto(11)
    remote(f'mv {I2} {H}/incr2_aside', label='(gate test) hide incremental2')
    lines, _ = bot_ready('A11', 11, 'incremental2 missing')
    _, still = sh(f'sudo test -d /home/{S}/trade_secrets && echo files-still-there || echo files-GONE')
    result('A11-gate', 'OK' if said(lines, 'Not yet') and 'still-there' in still else 'PROBLEM',
           f'with incremental2 missing -> {verdict(lines)} ({still.strip()})')
    remote(f'mv {H}/incr2_aside {I2}', quiet=True)
    lines, _ = bot_ready('A11', 11, 'backups good')
    result('A11', 'OK' if said(lines, 'I just deleted') else 'PROBLEM', verdict(lines))
    bot_goto(12)
    lines, _ = bot_ready('A12', 12, 'nothing restored')
    result('A12-none', 'OK' if said(lines, 'full backup') else 'PROBLEM', 'nothing restored -> ' + verdict(lines) + ' (B3/N3 fixed?)')

    def restore(srcs, mflag=M, what=''):
        for s in srcs:
            sh(f'sudo rsync -av {mflag} {U}@{IP}:{s}/{S}/ /home/{S}/', label=f'{what} restore from {s.split("/")[-1]}')

    restore([FULL, D2, I2, I1], what='(wrong order)')
    lines, _ = bot_ready('A12', 12, 'wrong order')
    result('A12-order', 'OK' if said(lines, 'Close') else 'PROBLEM', 'wrong order -> ' + verdict(lines))
    bot_goto(11)
    lines, _ = bot_ready('A11', 11, 'RESET via goto 11')
    _, left = sh(f'sudo ls /home/{S}')
    result('RESET-11', 'OK' if said(lines, 'I just deleted') and not left.strip() else 'PROBLEM',
           f'goto 11 + ready -> {verdict(lines)}; /home/{S} now: {left.split()[:6]}')
    bot_goto(12)
    restore([FULL, D2, I1, I2], mflag='', what='(no -M)')
    lines, _ = bot_ready('A12', 12, 'restored without -M')
    _, owners = sh(f'sudo find /home/{S} -mindepth 1 -printf "%u\\n" | sort | uniq -c')
    result('A12-owner', 'OK' if said(lines, 'owned by') else 'PROBLEM',
           f'restore without -M (owners: {" ".join(owners.split())}) -> ' + verdict(lines))
    bot_goto(11)
    bot_ready('A11', 11, 'reset again')
    bot_goto(12)
    restore([FULL, D2, I1, I2], what='(correct, -M)')
    lines, fl = bot_ready('A12', 12, 'correct restore with -M')
    _, owners = sh(f'sudo find /home/{S} -mindepth 1 -printf "%u\\n" | sort | uniq -c')
    rc, _ = sh(f'sudo -u {S} touch /home/{S}/notes', label=f'can {S} write their own notes?')
    result('A12', 'OK' if fl and rc == 0 else 'PROBLEM',
           f'correct -M restore -> {verdict(lines)}; owners: {" ".join(owners.split())}; {S} can write: {rc == 0}')

    # ---------------------------------------------------------------- Attack 13
    section('BOT: Attack 13 (first backed-up version of notes)')
    bot_goto(13)
    sh(f'sudo rsync -av {M} {U}@{IP}:{D2}/{S}/notes /home/{S}/notes', label='(mistake) notes from differential2')
    lines, _ = bot_ready('A13', 13, 'notes from differential2')
    result('B7', 'OK' if said(lines, 'differential2') else 'PROBLEM', 'differential2 version -> ' + verdict(lines))
    sh(f'sudo rsync -av {M} {U}@{IP}:{D1}/{S}/notes /home/{S}/notes')
    lines, fl = bot_ready('A13', 13, 'notes from differential1')
    result('A13', 'OK' if fl else 'PROBLEM', 'differential1 version -> ' + verdict(lines))

    # ---------------------------------------------------------------- P1
    section('EXTRA: P1 sudo + no user@')
    _, user = sh(f'sudo ssh -G {IP} 2>/dev/null | grep "^user "')
    rc_rs, out_rs = sh(f'sudo rsync -av --dry-run -e "ssh -o BatchMode=yes" /etc/hostname {IP}:')
    result('P1', 'INFO', f'sudo ssh BACKUPIP connects as "{user.strip()}"; sudo rsync ... BACKUPIP: rc={rc_rs} (sheet now warns about this)')

    # ---------------------------------------------------------------- flags
    section('FLAGS')
    for where, f in flags_seen:
        note(f'{where}: {f}')
    uniq = {f for _, f in flags_seen}
    result('FLAGS', 'OK' if len(uniq) == 10 else 'PROBLEM', f'{len(uniq)} distinct flags collected (10 expected: 9 in chat + 1 hidden file)')


# --------------------------------------------------------------------------------------------
def write_report(header):
    with open(REPORT, 'w') as f:
        f.write('\n'.join(header) + '\n\n')
        f.write('SUMMARY  (CONFIRMED = issue reproduced; OK = works as intended; PROBLEM = unexpected; INFO = observation)\n')
        for rid, st, text in summary:
            f.write(f'{st:<15} {rid:<20} {text}\n')
        f.write('\n\nDETAILS\n')
        f.write('\n'.join(details) + '\n')


def main():
    global _log_fh, irc
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument('--password', default='tiaspbiqe2r', help='your login password (sudo and backup_server ssh)')
    ap.add_argument('--irc-host', default='hackerbot')
    ap.add_argument('--irc-port', type=int, default=6667)
    ap.add_argument('--bot', default='Hackerbot')
    ap.add_argument('--irc-check', action='store_true', help='only connect to the bot, list attacks, print discovered values')
    ap.add_argument('--yes', action='store_true', help="don't ask for confirmation")
    ap.add_argument('--force', action='store_true', help='run even if the VMs do not look freshly built')
    args = ap.parse_args()

    _log_fh = open(FULL_LOG, 'w')
    started = datetime.datetime.now()
    C.U = subprocess.run(['whoami'], stdout=subprocess.PIPE).stdout.decode().strip()
    C.BOT = args.bot
    say(f'backups_lab_test.py {VERSION} - full log: {FULL_LOG}')

    irc = IRC(args.irc_host, args.irc_port, args.bot)
    prompts, C.IP, bot_user, C.BIN_DIR, C.S = discover()
    say(f'Bot reachable. YOURUSER={C.U} (bot says {bot_user}) SECONDUSER={C.S} BACKUPIP={C.IP} scp dir={C.BIN_DIR} attacks={len(prompts)}')
    if args.irc_check:
        for n in sorted(prompts):
            say(f'  #{n}: {prompts[n][:150]}')
        return
    if bot_user != C.U:
        sys.exit(f'Run this as {bot_user} (the bot expects backups under /home/{bot_user}), not {C.U}.')
    if not args.yes:
        a = input('This modifies the desktop and backup_server VMs (disposable test builds only). Type yes: ')
        if a.strip().lower() != 'yes':
            sys.exit('aborted')

    install_sudo(args.password)
    try:
        setup_ssh(args.password)
        fresh = clean_start()
        if not fresh:
            msg = f'/home/{C.S} does not look fresh (no trade_secrets/ or notes already exists) - rebuild the VMs for a valid run'
            note('WARNING: ' + msg)
            if not args.force:
                sys.exit(msg + ' (or use --force)')
        _, vers = sh(f'rsync --version | head -1; ssh -V 2>&1; ssh -o BatchMode=yes {C.U}@{C.IP} "rsync --version | head -1; '
                     f'cat /etc/debian_version; python3 --version"', quiet=True)
        try:
            run_all(args)
        except Exception as e:  # keep whatever we have
            import traceback
            log(traceback.format_exc())
            result('SCRIPT', 'ERROR', f'test aborted early: {e!r} (see full log)')
    finally:
        remove_sudo()
    header = [f'Backups lab automated test report  (script {VERSION})',
              f'started {started:%Y-%m-%d %H:%M}, took {(datetime.datetime.now() - started).seconds // 60} min',
              f'desktop={socket.gethostname()} YOURUSER={C.U} SECONDUSER={C.S} BACKUPIP={C.IP}',
              'versions: ' + ' | '.join(l.strip() for l in vers.splitlines() if l.strip()),
              'Note: key-based SSH was set up for automation, so no password prompts appear in the transcripts.']
    write_report(header)
    say(f'\nDone. Report: {REPORT}   Full log: {FULL_LOG}')


if __name__ == '__main__':
    main()
