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

VERSION = '2026-10-06.1'
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
    keep = re.compile(r'^:\)|^:\(|^Ok, good|^I just deleted|^Oh no|^Took too long|^Correct|^Incorrect|'
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
        if re.match(r'^(:\)|:\(|Ok, good|I just deleted|Oh no)', l):
            return FLAG_RE.sub('flag{..}', l)[:200]
    return '(no verdict line) ' + ' | '.join(lines)[:200]


# --------------------------------------------------------------------------------------------
# guarded /etc restores: the labsheet restores all of /etc with --fake-super as root. Snapshot
# every mode/owner first, run the restore, report what changed, then put modes/owners back -
# all inside ONE root shell, so a broken /etc/sudoers can't lock us out mid-test.
# --------------------------------------------------------------------------------------------
def etc_guarded(rid, label, restore_cmds, expect_files):
    body = '\n'.join(restore_cmds)
    files = ' '.join(expect_files)
    script = f'''
snap() {{ find /etc -xdev ! -type l -printf '%m %U %G %p\\n' 2>/dev/null | sort -k4; }}
snap > /root/.labtest_before
{body}
snap > /root/.labtest_after
echo "=== CHANGED (before/after) ==="
diff /root/.labtest_before /root/.labtest_after | grep '^[<>]' | head -60
echo "=== CHANGED_COUNT $(diff /root/.labtest_before /root/.labtest_after | grep -c '^>')"
echo "=== SUDOERS"; stat -c '%a %U:%G %n' /etc/sudoers /etc/sudoers.d/* 2>&1
echo "=== RESTORED FILES"; for f in {files}; do stat -c '%a %U:%G %n' "$f" 2>&1; done
echo "=== SUDO CHECK (would sudo still work for a student?)"; visudo -c 2>&1 | tail -3
while read -r m u g p; do [ -e "$p" ] && chown -h "$u:$g" "$p" && chmod "$m" "$p"; done < /root/.labtest_before
echo "=== AFTER REPAIR, still differing: $(snap | diff /root/.labtest_before - | grep -c '^>')"
'''
    rc, out = sh(f'sudo bash -c {shlex.quote(script)}', label=f'[guarded root shell] {label}: ' + ' ; '.join(restore_cmds))
    m = re.search(r'=== CHANGED_COUNT (\d+)', out)
    changed = int(m.group(1)) if m else -1
    missing = [f for f in expect_files if re.search(re.escape(f) + r'.*No such file|cannot stat.*' + re.escape(f), out)]
    sudo_bad = 'parse error' in out or re.search(r'bad permissions|should be', out)
    status = 'PROBLEM' if (changed > 0 or missing or sudo_bad) else 'OK'
    changed_lines = re.search(r'=== CHANGED \(before/after\) ===\n(.*?)\n=== CHANGED_COUNT', out, re.S)
    sample = ''
    if changed_lines and changed_lines.group(1).strip():
        sample = ' e.g. ' + '; '.join(changed_lines.group(1).strip().splitlines()[:4])
    result(rid, status, f'{label}: {changed} /etc entries changed mode/owner by the restore'
                        f'{" (sudoers affected!)" if sudo_bad else ""}{sample}; missing after restore: {missing or "none"}')
    return out


# --------------------------------------------------------------------------------------------
# setup
# --------------------------------------------------------------------------------------------
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
    m1 = re.search(r'backup_server: ([\d.]+):/home/([^/]+)/(remote-bin-backup-[0-9a-f]+)/', p1)
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
    remote('rm -rf ~/ssh_etc_backup ~/ssh_backup ~/remote-* ~/b2_* ~/b8_* ~/p2_* ~/incr1_saved', quiet=True)
    sh('sudo rm -rf ~/backups ~/b1 /tmp/b2src /tmp/b2_r* /tmp/b8 /tmp/p3; '
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

    # ---------------------------------------------------------------- Getting started
    section('LABSHEET: Getting started (lines 22-67)')
    rc, out = sh('ls /home')
    users = set(out.split())
    result('P4', 'OK' if users == {U, S} else 'CONFIRMED',
           f'ls /home -> {sorted(users)} (want exactly YOURUSER={U} and SECONDUSER={S})')
    rc, out = sh('uptime')

    # ---------------------------------------------------------------- Copy
    section('LABSHEET: Copy (lines 93-116)')
    sh('mkdir ~/backups/')
    sh('cp /etc/passwd ~/backups/')
    _, a = sh('ls -la ~/backups/passwd')
    _, b = sh('ls -la /etc/passwd')
    result('L-copy', 'INFO', f'cp loses ownership as the sheet says: backup "{a.split()[2:4] if a.split() else a}" vs original "{b.split()[2:4] if b.split() else b}"')

    # ---------------------------------------------------------------- SCP
    section('LABSHEET: SSH/SCP (lines 118-167) + B6 (two dir names) + B8')
    t = time.time()
    rc1, out1 = sh(f'sudo scp -pr /etc/ {U}@{IP}:{H}/ssh_etc_backup')
    d1 = time.time() - t
    sh("sudo bash -c 'echo > /etc/hi'")
    rc2, out2 = sh(f'sudo scp -pr /etc/ {U}@{IP}:{H}/ssh_backup/')
    _, lay = remote('for d in ssh_etc_backup ssh_backup; do printf "%s: " $d; '
                    'if [ -d $d/etc ]; then echo "has etc/ subdir"; elif [ -e $d/passwd ]; then echo "holds /etc CONTENTS directly"; '
                    'else echo "missing/other"; fi; done')
    remote('ls -la ssh_backup/ | head -5')
    result('B6-sheet', 'CONFIRMED' if ('ssh_etc_backup: holds' in lay) != ('ssh_backup: holds' in lay) or rc2 != 0 else 'NOT-REPRODUCED',
           f'1st scp -> ssh_etc_backup (rc={rc1}, {d1:.0f}s), 2nd "repeat" -> ssh_backup/ (rc={rc2}); layouts: {lay.strip()!r}')
    _, sec = remote('ls -l ssh_etc_backup/shadow ssh_etc_backup/etc/shadow 2>/dev/null')
    result('L-scp-shadow', 'INFO', f'sudo scp of /etc puts shadow on the backup_server as: {sec.strip()[:160]!r}')

    # B8: scp semantics + /bin size
    _, sz = sh('readlink -f /bin; ls /bin | wc -l; du -shL /bin/ 2>/dev/null | cut -f1')
    sh('mkdir -p /tmp/b8/d && echo x > /tmp/b8/d/f', quiet=True)
    remote('mkdir -p b8_exists b8_exists_s', quiet=True)
    for src, dst in [('/tmp/b8/d', 'b8_new'), ('/tmp/b8/d', 'b8_new_slash/'), ('/tmp/b8/d/', 'b8_new_srcslash'),
                     ('/tmp/b8/d', 'b8_exists/'), ('/tmp/b8/d/', 'b8_exists_s/')]:
        sh(f'scp -r {src} {IP}:{H}/{dst}')
    _, tree = remote('find b8_* | sort')
    t = set(tree.split())
    conf = 'b8_new/f' in t and 'b8_exists/d/f' in t
    result('B8', 'CONFIRMED' if conf else 'NOT-REPRODUCED',
           f'scp -r dir -> missing dest gives dest=copy, existing dest gives dest/dir; tree={sorted(t)}; /bin: {" ".join(sz.split())}')
    remote('rm -rf b8_*', quiet=True)

    # ---------------------------------------------------------------- Attack 1
    section('BOT: Attack 1 (scp /bin)')
    bot_goto(1)
    lines, _ = bot_ready('A1', 1, 'nothing copied yet')
    result('A1-none', 'INFO', 'nothing copied -> ' + verdict(lines))
    remote(f'mkdir -p {C.BIN_DIR} && cp /bin/ls /bin/mkdir {C.BIN_DIR}/', quiet=True)
    lines, _ = bot_ready('A1', 1, 'contents copied without bin/ (what scp -r /bin/ to a missing dir does)')
    result('A1-nobin', 'INFO', 'contents without bin/ -> ' + verdict(lines))
    remote(f'rm -rf {C.BIN_DIR} && mkdir -p {C.BIN_DIR}', quiet=True)
    t = time.time()
    rc, _ = sh(f'scp -rq /bin {IP}:{H}/{C.BIN_DIR}/', label=f'scp -rq /bin {IP}:{H}/{C.BIN_DIR}/   (after ssh mkdir)')
    dur = time.time() - t
    _, du = remote(f'du -sh {C.BIN_DIR} | cut -f1; ls {C.BIN_DIR}')
    lines, fl = bot_ready('A1', 1, 'correct: mkdir remote dir then scp -r /bin')
    result('A1', 'OK' if fl else 'PROBLEM', f'correct solution (copy took {dur:.0f}s, {du.split()[0] if du.split() else "?"}) -> ' + verdict(lines))

    # ---------------------------------------------------------------- rsync local
    section('LABSHEET: Rsync, deltas and epoch backups (lines 181-207)')
    sh('sudo rsync -av /etc ~/backups/rsync_backup/', label='sudo rsync -av /etc ~/backups/rsync_backup/')
    sh("sudo bash -c 'echo hello > /etc/hello'")
    rc, out = sh('sudo rsync -av /etc ~/backups/rsync_backup/')
    tr = transferred(out)
    result('L-rsync-local', 'OK' if tr == ['etc/hello'] else 'INFO', f'2nd local rsync transferred {tr[:6]} (sheet says: only the new file)')

    # ---------------------------------------------------------------- rsync remote + fake-super
    section('LABSHEET: Rsync remote copies via SSH, --fake-super (lines 209-262) + B2 evidence')
    rc, out = sh(f'sudo rsync -avzh --fake-super /etc {U}@{IP}:{H}/remote-rsync-backup/')
    sent = re.search(r'sent ([\d.,]+\w?) bytes', out)
    _, du = sh('sudo du -sh /etc')
    result('L-rsync-z', 'INFO', f'rsync -z sent {sent.group(1) if sent else "?"} vs du /etc {du.split()[0] if du.split() else "?"}')
    sh('sudo rm /etc/hello')
    sh(f'sudo rsync -avzh --fake-super /etc {U}@{IP}:{H}/remote-rsync-backup/')
    rc, _ = remote('test -f remote-rsync-backup/etc/hello')
    result('L-nodelete', 'OK' if rc == 0 else 'PROBLEM', 'without --delete the server keeps etc/hello' + ('' if rc == 0 else ' - NOT kept!'))
    sh(f'sudo rsync -avz --fake-super {U}@{IP}:{H}/remote-rsync-backup/etc/hello /etc/')
    _, own = sh('sudo stat -c "%a %U:%G" /etc/hello; sudo getfattr -d /etc/hello 2>/dev/null || '
                'sudo python3 -c \'import os;print({k:os.getxattr("/etc/hello",k) for k in os.listxattr("/etc/hello")})\'')
    result('L-restore-hello', 'INFO', f'/etc/hello restored with local --fake-super: {" ".join(own.split())[:200]}')
    sh('sudo rm /etc/hello')
    sh(f'sudo rsync -avzh --fake-super --delete /etc {U}@{IP}:{H}/remote-rsync-backup/')
    rc, _ = remote('test ! -e remote-rsync-backup/etc/hello')
    result('L-delete', 'OK' if rc == 0 else 'PROBLEM', '--delete removed etc/hello from the server' + ('' if rc == 0 else ' - it did NOT'))
    _, srv = remote("stat -c '%a %U:%G' remote-rsync-backup/etc/shadow; python3 -c 'import os;p=\"remote-rsync-backup/etc/shadow\";print(os.listxattr(p))'")
    result('B2-sheet', 'CONFIRMED' if U in srv and 'user.rsync' not in srv else 'NOT-REPRODUCED',
           f'server copy of etc/shadow after the sheet\'s `sudo rsync --fake-super`: {" ".join(srv.split())} (root ownership lost if owned by {U} and no user.rsync.%stat xattr)')

    # ---------------------------------------------------------------- Attack 2
    section('BOT: Attack 2 (full backup) + P3')
    rc, out = sh(f'rsync -av /home/{S} /tmp/p3/', label=f'(P3) rsync -av /home/{S} /tmp/p3/   (no sudo)')
    _, mode = sh(f'stat -c "%a %U" /home/{S}', quiet=True)
    result('P3', 'INFO', f'home dir {mode.strip()}; non-sudo rsync rc={rc}, {len(warnings_in(out))} warning/error lines e.g. {warnings_in(out)[:2]}')
    sh('rm -rf /tmp/p3', quiet=True)
    bot_goto(2)
    rc, out = sh(f'sudo rsync -avzh --fake-super /home/{S} {U}@{IP}:{FULL}/{S}',
                 label=f'(mistake: dest names {S} too, dir not yet created) sudo rsync -avzh --fake-super /home/{S} {U}@{IP}:{FULL}/{S}')
    result('A2-mkdir', 'INFO', f'rsync into a not-yet-existing nested dest: rc={rc} {warnings_in(out)[:1]}')
    remote(f'mkdir -p {FULL}/{S}', quiet=True)
    sh(f'sudo rsync -avzh --fake-super /home/{S} {U}@{IP}:{FULL}/{S}', label=f'(mistake: nested, dest dir exists) same command again')
    lines, _ = bot_ready('A2', 2, f'nested {S}/{S}')
    result('A2-nested', 'INFO', 'nested SECONDUSER/SECONDUSER -> ' + verdict(lines))
    remote(f'rm -rf {FULL}', quiet=True)
    sh(f'sudo rsync -avzh --fake-super /home/{S} {U}@{IP}:{FULL}/')
    lines, fl = bot_ready('A2', 2, 'correct full backup')
    result('A2', 'OK' if fl else 'PROBLEM', 'correct full backup -> ' + verdict(lines))

    # ---------------------------------------------------------------- Differential (/etc)
    section('LABSHEET: Differential backups (lines 280-340) + B1 + guarded /etc restore')
    sh("sudo bash -c 'echo \"hello there\" > /etc/hello'")
    rc, out = sh('sudo rsync -av /etc --compare-dest=~/backups/rsync_backup/ ~/backups/rsync_backup_week1/')
    n_local = lcount('~/backups/rsync_backup_week1')
    sh(f'sudo rsync -avzh --fake-super /etc --compare-dest={H}/remote-rsync-backup/ {U}@{IP}:{H}/remote-rsync-backup-week1/')
    n_remote = rcount('remote-rsync-backup-week1')
    result('B1-sheet', 'CONFIRMED' if n_local > 50 else 'NOT-REPRODUCED',
           f'week1 differential: local (--compare-dest=~/...) has {n_local} files, remote (absolute path) has {n_remote}; '
           f'rsync said: {warnings_in(out)[:1]}')
    sh('ls -la ~/backups/rsync_backup_week1/etc | head -8')
    sh("sudo bash -c 'echo \"hello there!\" > /etc/hi'")
    sh('sudo rsync -av /etc --compare-dest=~/backups/rsync_backup/ ~/backups/rsync_backup_week2/')
    sh(f'sudo rsync -avzh --fake-super /etc --compare-dest={H}/remote-rsync-backup/ {U}@{IP}:{H}/remote-rsync-backup-week2/')
    note(f'week2: local {lcount("~/backups/rsync_backup_week2")} files, remote {rcount("remote-rsync-backup-week2")} files (sheet: "your two new files")')
    sh('sudo rm /etc/wgetrc /etc/hello')
    etc_guarded('L-etc-restore-diff', 'sheet lines 335/337 remote restore (full, then week2)',
                [f'rsync -avz --fake-super {U}@{IP}:{H}/remote-rsync-backup/etc/ /etc/',
                 f'rsync -avz --fake-super {U}@{IP}:{H}/remote-rsync-backup-week2/etc/ /etc/'],
                ['/etc/wgetrc', '/etc/hello', '/etc/hi'])
    sh('sudo rm /etc/wgetrc /etc/hello')
    etc_guarded('L-etc-restore-local', 'sheet line 340 "try restoring from the local copy"',
                [f'rsync -av {H}/backups/rsync_backup/etc/ /etc/', f'rsync -av {H}/backups/rsync_backup_week2/etc/ /etc/'],
                ['/etc/wgetrc', '/etc/hello'])

    # ---------------------------------------------------------------- Attack 3, 4
    section('BOT: Attacks 3-4 (changes A, differential1) + P2')
    bot_goto(3)
    lines, _ = bot_ready('A3', 3, 'bot creates notes/log2/flag')
    result('A3', 'OK' if lines and 'Ok, good' in ' '.join(lines) else 'PROBLEM', verdict(lines))
    _, hf = sh(f'sudo cat /home/{S}/personal_secrets/flag')
    for f in FLAG_RE.findall(hf):
        flags_seen.append(('attack 3 hidden file', f))
    bot_goto(4)
    sh(f'sudo rsync -avzh --fake-super /home/{S} {U}@{IP}:{D1}/', label=f'(mistake: no --compare-dest) sudo rsync -avzh --fake-super /home/{S} {U}@{IP}:{D1}/')
    lines, _ = bot_ready('A4', 4, 'full copy, no --compare-dest')
    result('A4-full', 'INFO', 'no --compare-dest -> ' + verdict(lines))
    remote(f'rm -rf {D1}', quiet=True)
    rc, out = sh(f'sudo rsync -avzh --fake-super /home/{S} --compare-dest={FULL}/{S}/ {U}@{IP}:{D1}/',
                 label=f'(P2 mistake: layout mismatch) ... --compare-dest={FULL}/{S}/ ...')
    n = rcount(D1)
    lines, _ = bot_ready('A4', 4, 'compare-dest layout mismatch')
    result('P2', 'CONFIRMED' if n > 3 else 'NOT-REPRODUCED',
           f'mismatched --compare-dest copied {n} files, rsync warnings: {warnings_in(out)[:1] or "none"}; bot -> {verdict(lines)}')
    remote(f'rm -rf {D1}', quiet=True)
    sh(f'sudo rsync -avzh --fake-super /home/{S} --compare-dest={FULL}/ {U}@{IP}:{D1}/')
    remote(f'cd {D1} && find . -type f')
    lines, fl = bot_ready('A4', 4, 'correct differential1')
    result('A4', 'OK' if fl else 'PROBLEM', 'correct differential1 -> ' + verdict(lines))

    # ---------------------------------------------------------------- Incremental (/etc)
    section('LABSHEET: Incremental backups (lines 364-408) + guarded /etc restore')
    sh("sudo bash -c 'echo \"Another test change\" > /etc/test1'")
    sh("sudo bash -c 'echo \"Another test change\" > /etc/hello'")
    sh('sudo rsync -av /etc --compare-dest=~/backups/rsync_backup/ --compare-dest=~/backups/rsync_backup_week2/ ~/backups/rsync_backup_monday/')
    sh(f'sudo rsync -avzh --fake-super /etc --compare-dest={H}/remote-rsync-backup/ --compare-dest={H}/remote-rsync-backup-week2/ {U}@{IP}:{H}/remote-rsync-backup-monday/')
    sh("sudo bash -c 'echo \"Another test change\" > /etc/test2'")
    sh('sudo rsync -av /etc --compare-dest=~/backups/rsync_backup/ --compare-dest=~/backups/rsync_backup_week2/ --compare-dest=~/backups/rsync_backup_monday/ ~/backups/rsync_backup_tuesday/')
    sh(f'sudo rsync -avzh --fake-super /etc --compare-dest={H}/remote-rsync-backup/ --compare-dest={H}/remote-rsync-backup-week2/ --compare-dest={H}/remote-rsync-backup-monday/ {U}@{IP}:{H}/remote-rsync-backup-tuesday/')
    note(f'monday: local {lcount("~/backups/rsync_backup_monday")} / remote {rcount("remote-rsync-backup-monday")} files; '
         f'tuesday: local {lcount("~/backups/rsync_backup_tuesday")} / remote {rcount("remote-rsync-backup-tuesday")} files')
    sh('sudo rm /etc/wgetrc /etc/hello /etc/test1 /etc/test2')
    etc_guarded('L-etc-restore-incr', 'line 408 restore full -> week2 -> monday -> tuesday (remote)',
                [f'rsync -avz --fake-super {U}@{IP}:{H}/remote-rsync-backup{s}/etc/ /etc/' for s in ['', '-week2', '-monday', '-tuesday']],
                ['/etc/wgetrc', '/etc/hello', '/etc/test1', '/etc/test2'])

    # ---------------------------------------------------------------- Attack 5, 6
    section('BOT: Attacks 5-6 (changes B, differential2) + B6 (prompt path)')
    bot_goto(5)
    lines, _ = bot_ready('A5', 5, 'bot appends/creates more files')
    result('A5', 'OK' if 'Ok, good' in ' '.join(lines) else 'PROBLEM', verdict(lines))
    bot_goto(6)
    sh(f'sudo rsync -avzh --fake-super /home/{S}/ --compare-dest={FULL}/{S}/ {U}@{IP}:{D2}/',
       label=f'(follows prompt literally: .../differential2/.) sudo rsync -avzh --fake-super /home/{S}/ --compare-dest={FULL}/{S}/ {U}@{IP}:{D2}/')
    lines, fl = bot_ready('A6', 6, 'prompt path taken literally (contents into differential2/)')
    result('B6', 'CONFIRMED' if not fl else 'NOT-REPRODUCED', 'attack 6 prompt says .../remote-rsync-differential2/. ; doing that -> ' + verdict(lines))
    remote(f'rm -rf {D2}', quiet=True)
    sh(f'sudo rsync -avzh --fake-super /home/{S} --compare-dest={FULL}/ --compare-dest={D1}/ {U}@{IP}:{D2}/',
       label='(mistake: incremental instead of differential) --compare-dest=full + differential1')
    lines, _ = bot_ready('A6', 6, 'incremental instead of differential')
    result('A6-incr', 'INFO', 'compared against diff1 too -> ' + verdict(lines))
    remote(f'rm -rf {D2}', quiet=True)
    sh(f'sudo rsync -avzh --fake-super /home/{S} --compare-dest={FULL}/ {U}@{IP}:{D2}/')
    remote(f'cd {D2} && find . -type f')
    lines, fl = bot_ready('A6', 6, 'correct differential2')
    result('A6', 'OK' if fl else 'PROBLEM', 'correct differential2 -> ' + verdict(lines))

    # ---------------------------------------------------------------- Attack 7, 8
    section('BOT: Attacks 7-8 (changes C, incremental1) + B4')
    bot_goto(7)
    lines, _ = bot_ready('A7', 7, 'bot changes files')
    result('A7', 'OK' if 'Ok, good' in ' '.join(lines) else 'PROBLEM', verdict(lines))
    bot_goto(8)
    sh(f'sudo rsync -avzh --fake-super /home/{S} --compare-dest={FULL}/ {U}@{IP}:{I1}/',
       label='(mistake: forgot differential2 --compare-dest) only --compare-dest=full')
    lines, _ = bot_ready('A8', 8, 'forgot --compare-dest for differential2')
    result('B4', 'CONFIRMED' if "wasn't an incremental" in ' '.join(lines) else 'NOT-REPRODUCED',
           'forgot diff2 compare-dest -> ' + verdict(lines) + ' (a specific hint would name the missing --compare-dest)')
    remote(f'rm -rf {I1}', quiet=True)
    sh(f'sudo rsync -avzh --fake-super /home/{S} --compare-dest={FULL}/ --compare-dest={D2}/ {U}@{IP}:{I1}/')
    remote(f'cd {I1} && find . -type f')
    lines, fl = bot_ready('A8', 8, 'correct incremental1')
    result('A8', 'OK' if fl else 'PROBLEM', 'correct incremental1 -> ' + verdict(lines))

    # ---------------------------------------------------------------- Snapshots (/etc)
    section('LABSHEET: Rsync snapshot backups (lines 454-484) + B1 link-dest')
    sh('sudo rsync -av --delete --link-dest=~/backups/rsync_backup/ /etc ~/backups/rsync_backup_snapshot_1')
    sh("sudo bash -c 'echo \"Another test change\" > /etc/test3'")
    sh("sudo bash -c 'echo \"Another test change\" > /etc/test4'")
    sh('sudo rsync -av --delete --link-dest=~/backups/rsync_backup/ /etc ~/backups/rsync_backup_snapshot_2')
    sh('sudo rm /etc/test3')
    sh('sudo rsync -av --delete --link-dest=~/backups/rsync_backup/ /etc ~/backups/rsync_backup_snapshot_3')
    _, links = sh('for s in snapshot_1 snapshot_2 snapshot_3; do echo "$s $(sudo find ~/backups/rsync_backup_$s -type f -links +1 | wc -l)"; done; '
                  'sudo du -sh ~/backups/rsync_backup ~/backups/rsync_backup_snapshot_1 ~/backups/rsync_backup_snapshot_2 ~/backups/rsync_backup_snapshot_3')
    sh('sudo cp -a ~/backups/rsync_backup_snapshot_2/etc/test3 /etc/ && cat /etc/test3', label='recover test3 from snapshot_2')
    m = re.search(r'snapshot_1 (\d+)', links)
    result('B1-linkdest-sheet', 'CONFIRMED' if m and int(m.group(1)) == 0 else 'NOT-REPRODUCED',
           'hard-linked files per snapshot / du: ' + ' '.join(links.split()))

    # ---------------------------------------------------------------- Attack 9, 10 (+ B5, quiz)
    section('BOT: Attacks 9-10 (changes D, incremental2) + B5 + quiz')
    bot_goto(9)
    lines, _ = bot_ready('A9', 9, 'bot changes files')
    result('A9', 'OK' if 'Ok, good' in ' '.join(lines) else 'PROBLEM', verdict(lines))
    bot_goto(10)
    remote(f'cp -a {I1} {H}/incr1_saved', label='[backup_server] set aside the good incremental1')
    sh(f'sudo rsync -avzh --fake-super /home/{S} --compare-dest={FULL}/ --compare-dest={D2}/ {U}@{IP}:{I1}/',
       label='(B5 mistake: redo incremental1 AFTER attack 9) same incr1 command again')
    sh(f'sudo rsync -avzh --fake-super /home/{S} --compare-dest={FULL}/ --compare-dest={D2}/ --compare-dest={I1}/ {U}@{IP}:{I2}/')
    n = rcount(I2)
    lines, _ = bot_ready('A10', 10, 'incremental1 retaken late')
    result('B5', 'CONFIRMED' if 'specified remote directory' in ' '.join(lines) else 'NOT-REPRODUCED',
           f'incr2 dir exists ({n} files) but bot says -> ' + verdict(lines))
    remote(f'rm -rf {I1} {I2} && mv {H}/incr1_saved {I1}', quiet=True)
    sh(f'sudo rsync -avzh --fake-super /home/{S} --compare-dest={FULL}/ --compare-dest={D2}/ --compare-dest={I1}/ {U}@{IP}:{I2}/')
    remote(f'cd {I2} && find . -type f')
    lines, fl = bot_ready('A10', 10, 'correct incremental2')
    result('A10', 'OK' if fl else 'PROBLEM', 'correct incremental2 -> ' + verdict(lines))
    _, ans = sh(f'sudo cat /home/{S}/personal_secrets/nothing_much', label='(quiz answered from the DESKTOP, not the backups)')
    _, ans_b = remote(f'cat {I2}/{S}/personal_secrets/nothing_much')
    lines, _ = irc.tell(f'answer {ans.strip()}', until=ANSWER_DONE_RE, timeout=40)
    for f in FLAG_RE.findall('\n'.join(lines)):
        flags_seen.append(('attack 10 quiz', f))
    details.append('BOT quiz answer: ' + ' | '.join(FLAG_RE.sub('flag{..}', l) for l in lines)[:300])
    result('Q10', 'CONFIRMED' if any(l.startswith('Correct') for l in lines) else 'NOT-REPRODUCED',
           f'quiz answered with `sudo cat` on the desktop ({ans.strip()!r}; backup has {ans_b.strip()!r}) -> '
           f'{"Correct" if any(l.startswith("Correct") for l in lines) else lines[:2]}')

    # ---------------------------------------------------------------- Attack 11, 12
    section('BOT: Attacks 11-12 (deletion, restore) + B3 (attack 12) + ownership after restore')
    bot_goto(11)
    lines, _ = bot_ready('A11', 11, 'bot deletes the files')
    result('A11', 'OK' if 'I just deleted' in ' '.join(lines) else 'PROBLEM', verdict(lines))
    sh(f'sudo ls -la /home/{S}')
    bot_goto(12)
    lines, _ = bot_ready('A12', 12, 'nothing restored yet')
    result('A12-none', 'CONFIRMED' if 'restored something' in ' '.join(lines) else 'INFO',
           'nothing restored -> ' + verdict(lines) + ' (B3 risk: random hex in a path can turn "didn\'t restore anything" into "restored something")')
    order_wrong = [FULL, D2, I2, I1]
    for src in order_wrong:
        sh(f'sudo rsync -avz --fake-super {U}@{IP}:{src}/{S}/ /home/{S}/', label=f'(wrong order: incr2 before incr1) restore from {src.split("/")[-1]}')
    lines, _ = bot_ready('A12', 12, 'restored in the wrong order')
    result('A12-order', 'OK' if 'Close' in ' '.join(lines) else 'INFO', 'wrong order -> ' + verdict(lines))
    sh(f'sudo rsync -avz --fake-super {U}@{IP}:{I2}/{S}/ /home/{S}/', label='re-apply incremental2 last')
    lines, fl = bot_ready('A12', 12, 'correct order')
    result('A12', 'OK' if fl else 'PROBLEM', 'restore full -> diff2 -> incr1 -> incr2 -> ' + verdict(lines))
    _, ls = sh(f'sudo ls -lnR /home/{S} | head -25')
    _, owners = sh(f'sudo find /home/{S} -mindepth 1 -printf "%u\\n" | sort | uniq -c', label='owners of restored files')
    rc, _ = sh(f'sudo -u {S} touch /home/{S}/notes', label=f'can {S} still write their own notes file?')
    result('B2-restore', 'CONFIRMED' if rc != 0 or S not in owners else 'NOT-REPRODUCED',
           f'after the labsheet-style restore, owners: {" ".join(owners.split())}; {S} can write notes: {"yes" if rc == 0 else "NO"}')

    # ---------------------------------------------------------------- Attack 13
    section('BOT: Attack 13 (earliest notes) + B7')
    bot_goto(13)
    _, n_full = remote(f'test -e {FULL}/{S}/notes && echo present || echo absent')
    _, n1 = remote(f'cat {D1}/{S}/notes')
    _, n2 = remote(f'cat {D2}/{S}/notes')
    sh(f'sudo rsync -avz --fake-super {U}@{IP}:{D2}/{S}/notes /home/{S}/notes', label='restore notes from differential2 (NOT the earliest)')
    lines, fl = bot_ready('A13', 13, 'notes from differential2')
    result('B7', 'CONFIRMED' if fl else 'NOT-REPRODUCED',
           f'notes in full backup: {n_full.strip()}; diff1 has {len(n1.strip().splitlines())} line(s), diff2 has '
           f'{len(n2.strip().splitlines())}; restoring the diff2 version -> ' + verdict(lines))
    if not fl:
        sh(f'sudo rsync -avz --fake-super {U}@{IP}:{D1}/{S}/notes /home/{S}/notes', label='restore notes from differential1')
        lines, fl = bot_ready('A13', 13, 'notes from differential1')
    result('A13', 'OK' if fl else 'PROBLEM', 'attack 13 solved -> ' + verdict(lines))

    # ---------------------------------------------------------------- B2 detailed
    section('EXTRA: B2 --fake-super vs -M--fake-super (walkthrough section 3)')
    sh(f'sudo install -d -o {S} -g {S} -m 750 /tmp/b2src && sudo -u {S} bash -c "echo hi > /tmp/b2src/f && chmod 640 /tmp/b2src/f"', quiet=True)
    _, orig = sh('sudo stat -c "%a %u:%g" /tmp/b2src/f')
    sh(f'sudo rsync -av --fake-super /tmp/b2src {U}@{IP}:{H}/b2_labsheet/')
    sh(f'sudo rsync -av -M--fake-super /tmp/b2src {U}@{IP}:{H}/b2_remote/')
    xa = "python3 -c 'import os,sys;p=sys.argv[1];print({k:os.getxattr(p,k).decode(errors=\"replace\") for k in os.listxattr(p)})'"
    _, sA = remote(f'stat -c "%a %u:%g" b2_labsheet/b2src/f; {xa} b2_labsheet/b2src/f')
    _, sB = remote(f'stat -c "%a %u:%g" b2_remote/b2src/f; {xa} b2_remote/b2src/f')
    restores = {
        'R1 labsheet restore of labsheet backup': f'sudo rsync -av --fake-super {U}@{IP}:{H}/b2_labsheet/b2src/ /tmp/b2_r1/',
        'R2 labsheet restore of -M backup': f'sudo rsync -av --fake-super {U}@{IP}:{H}/b2_remote/b2src/ /tmp/b2_r2/',
        'R3 -M restore of -M backup': f'sudo rsync -av -M--fake-super {U}@{IP}:{H}/b2_remote/b2src/ /tmp/b2_r3/',
        'R4 plain restore of labsheet backup': f'sudo rsync -av {U}@{IP}:{H}/b2_labsheet/b2src/ /tmp/b2_r4/',
    }
    rr = {}
    for k, cmd in restores.items():
        sh(cmd)
        n = k.split()[0].lower()
        _, st = sh(f'sudo stat -c "%a %u:%g" /tmp/b2_{n}/f')
        rr[k] = st.strip()
    want = orig.strip()
    result('B2', 'CONFIRMED' if rr['R3 -M restore of -M backup'] == want and rr['R1 labsheet restore of labsheet backup'] != want else 'INFO',
           f'original {want}; server A(labsheet)={" ".join(sA.split())}; server B(-M)={" ".join(sB.split())}; restores: ' +
           '; '.join(f'{k}={v}' for k, v in rr.items()))
    sh('sudo rm -rf /tmp/b2src /tmp/b2_r*', quiet=True)
    remote('rm -rf b2_*', quiet=True)

    # ---------------------------------------------------------------- P1
    section('EXTRA: P1 sudo + no user@')
    _, user = sh(f'sudo ssh -G {IP} | grep "^user "')
    rc_ssh, out_ssh = sh(f'sudo ssh -o BatchMode=yes {IP} true')
    rc_rs, out_rs = sh(f'sudo rsync -av --dry-run -e "ssh -o BatchMode=yes" /etc/hostname {IP}:')
    result('P1', 'CONFIRMED' if 'root' in user and rc_ssh != 0 and rc_rs != 0 else 'PROBLEM',
           f'sudo ssh BACKUPIP logs in as "{user.strip()}" -> rc={rc_ssh}; sudo rsync ... BACKUPIP: rc={rc_rs} '
           f'({(warnings_in(out_rs) or [out_rs.strip()])[0][:100]})')

    # ---------------------------------------------------------------- flags
    section('FLAGS')
    for where, f in flags_seen:
        note(f'{where}: {f}')
    uniq = {f for _, f in flags_seen}
    result('FLAGS', 'OK' if len(uniq) == 10 else 'PROBLEM', f'{len(uniq)} distinct flags collected (generator mints 10: 9 in chat + 1 hidden file)')


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
