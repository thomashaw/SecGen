#!/usr/bin/env python3
"""DEV ONLY: automated end-to-end test of the RSA (asymmetric_enc_rsa) Hackerbot lab and its lab sheet.

Run on the desktop VM, as the lab's main user, on a FRESH build, with the lab sheet and hackerbot_irc.py
in the same directory:

    python3 rsa_lab_test.py --irc-check      # can we reach the bot, do the prompts parse?
    python3 rsa_lab_test.py --yes            # the full run; writes ~/rsa_report.txt

It runs the lab sheet's code blocks verbatim, section by section (bash blocks as scripts, Python blocks
pasted into `python3 -i` as a student would), checks every output the sheet promises, and plays all 13
Hackerbot challenges over IRC: deliberate student mistakes first (recording each hint), then the right
answer (expecting a flag). It changes the VM - only use disposable builds. Standard library only.
"""
import argparse
import datetime
import os
import re
import shlex
import socket
import subprocess
import sys
import time

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from hackerbot_irc import Hackerbot  # noqa: E402

LAB = 'rsa'
VERSION = '2026-10-30.2'
HERE = os.path.dirname(os.path.abspath(__file__))
SHEET = os.path.join(HERE, '7_asymmetric_enc_rsa.md')
HOME = os.path.expanduser('~')
REPORT = os.path.join(HOME, LAB + '_report.txt')
FULL_LOG = os.path.join(HOME, LAB + '_full_log.txt')

_log_fh = None
summary = []
details = []
flags_seen = []
bot = None
prompts = {}


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
    summary.append((rid, status, text))
    details.append('[%s] %s: %s' % (status, rid, text))
    say('  [%s] %s: %s' % (status, rid, text))


def check(rid, cond, text):
    result(rid, 'OK' if cond else 'PROBLEM', text)
    return cond


def trim(out, n=14, width=220):
    lines = [l[:width] for l in out.rstrip().splitlines()]
    if len(lines) > n:
        lines = lines[:n // 2] + ['  ... (%d lines omitted, see full log) ...' % (len(lines) - n)] + lines[-(n - n // 2):]
    return '\n'.join('    | ' + l for l in lines)


def write_report(header):
    with open(REPORT, 'w') as f:
        f.write('\n'.join(header) + '\n\n')
        f.write('SUMMARY  (OK = works as intended; PROBLEM = unexpected; INFO = observation)\n')
        for rid, st, text in summary:
            f.write('%-10s %-14s %s\n' % (st, rid, text))
        f.write('\n\nDETAILS\n' + '\n'.join(details) + '\n')


# --------------------------------------------------------------------------------------------- shell
def sh(cmd, timeout=600, quiet=False, label=None, cwd=None):
    start = time.time()
    log('\n$ ' + cmd)
    try:
        p = subprocess.run(['bash', '-c', cmd], stdin=subprocess.DEVNULL, stdout=subprocess.PIPE,
                           stderr=subprocess.STDOUT, timeout=timeout, cwd=cwd or HOME,
                           env=dict(os.environ, LC_ALL='C', LANG='C'))
        out, rc = p.stdout.decode('utf-8', 'replace'), p.returncode
    except subprocess.TimeoutExpired as e:
        out, rc = (e.output or b'').decode('utf-8', 'replace') + '\n[TIMEOUT after %ds]' % timeout, 124
    dur = time.time() - start
    log(out.rstrip())
    log('[rc=%d %.1fs]' % (rc, dur))
    if not quiet:
        details.append('$ %s   -> rc=%d (%.0fs)' % (label or cmd.strip().splitlines()[0][:150], rc, dur))
        if out.strip():
            details.append(trim(out))
    return rc, out


def repl(code, cwd=None, timeout=300, label='python3 -i'):
    """Paste code into the Python REPL, as a student would. Returns (rc, output)."""
    start = time.time()
    log('\n>>> (REPL) \n' + code)
    p = subprocess.run(['python3', '-i', '-q'], input=(code + '\n').encode(), stdout=subprocess.PIPE,
                       stderr=subprocess.STDOUT, timeout=timeout, cwd=cwd or HOME)
    out = p.stdout.decode('utf-8', 'replace')
    log(out)
    details.append('%s (%d lines pasted) -> rc=%d (%.0fs)' % (label, len(code.splitlines()), p.returncode, time.time() - start))
    details.append(trim(out))
    return p.returncode, out, time.time() - start


# --------------------------------------------------------------------------------------------- the lab sheet
def sheet_blocks():
    """[(anchor, lang, code)] for every fenced block, with the anchor of the heading above it."""
    text = open(SHEET).read()
    out, anchor = [], None
    for m in re.finditer(r'^(#+ [^\n]*\{#([a-z0-9-]+)\})$|^```(\w*)\n(.*?)^```$', text, re.M | re.S):
        if m.group(2):
            anchor = m.group(2)
        else:
            out.append((anchor, m.group(3), m.group(4)))
    return out


BLOCKS = []


def blocks(anchor, lang=None):
    return [code for a, l, code in BLOCKS if a == anchor and (lang is None or l == lang)]


def bash_of(anchor, skip=('python3',)):
    """The section's bash blocks as one script (bare `python3` lines, which open the REPL, dropped)."""
    return '\n'.join(l for code in blocks(anchor, 'bash') for l in code.splitlines() if l.strip() not in skip)


# --------------------------------------------------------------------------------------------- bot
def bot_goto(n):
    # Wait for the bot to go quiet: prompts 11 and 12 contain 'ready', which the client's goto treats as
    # end-of-reply, and the rest of the burst would then be read as the reply to the next 'ready'.
    return bot.tell('goto %d' % n, idle=4, timeout=30)


def bot_ready(n, what):
    say('  ... ready on #%s: %s' % (n, what))
    t0 = time.time()
    r = bot.ready()
    for f in r.flags:
        flags_seen.append(('#%s %s' % (n, what), f))
    details.append('BOT #%s ready (%s) [%.0fs]%s:' % (n, what, time.time() - t0, '' if r.ended else ' [NO END-OF-REPLY]'))
    details.append('    > ' + re.sub(r'flag\{[^}]*\}', 'flag{..}', ' / '.join(r.lines))[:600])
    return r


def bot_answer(n, ans, what):
    r = bot.answer(ans)
    for f in r.flags:
        flags_seen.append(('#%s %s' % (n, what), f))
    details.append('BOT #%s answer %r (%s):' % (n, ans, what))
    details.append('    > ' + re.sub(r'flag\{[^}]*\}', 'flag{..}', ' / '.join(r.lines))[:400])
    return r


def said(r, text):
    return text in ' '.join(r.lines)


def expect_hint(n, what, r, phrase):
    check('BOT#%d' % n, said(r, phrase) and not r.flags, '%s -> %s' % (what, (r.verdict or ' / '.join(r.lines))[:140]))


def expect_flag(n, what, r):
    check('BOT#%d' % n, bool(r.flags), '%s -> %s' % (what, 'flag' if r.flags else (' / '.join(r.lines))[:160]))


def nums(n, *names):
    return [int(re.search(r'\b%s = (\d+)' % nm, prompts[n]).group(1)) for nm in names]


def quiz(n, right, wrong):
    """goto n, ready (expect the question), a wrong answer (expect Incorrect), then the right one (expect a flag)."""
    bot_goto(n)
    r = bot_ready(n, 'ask the question')
    check('BOT#%d' % n, said(r, 'answer X'), 'question asked: %s' % ' / '.join(r.lines)[:120])
    r = bot_answer(n, wrong, 'wrong answer')
    check('BOT#%d' % n, said(r, 'Incorrect') and not r.flags, 'wrong answer %r rejected' % wrong)
    r = bot_answer(n, right, 'right answer')
    expect_flag(n, 'right answer %r' % right, r)


# --------------------------------------------------------------------------------------------- the run
def discover():
    global prompts
    prompts = bot.list_attacks()
    if len(prompts) != 13:
        sys.exit('Expected 13 attacks, got %d - is the bot running the new config?' % len(prompts))


def clean_start():
    section('SETUP')
    rc, out = sh('ls -d ~/challenge_* ~/rsa_example ~/ca_example ~/emudhra_example 2>/dev/null', quiet=True)
    fresh = not out.strip()
    result('FRESH', 'OK' if fresh else 'INFO', 'no lab directories yet' if fresh else 'already there: ' + ' '.join(out.split()))
    sh('python3 --version; openssl version; python3 -c "import Cryptodome; print(\x27pycryptodome\x27, Cryptodome.__version__)"')
    return True


def maths():
    section('SHEET: RSA by hand, with Python')
    code = '\n'.join(blocks('rsa-by-hand-with-python', 'python'))
    rc, out, _ = repl(code)
    for want in ('161 132', '[5, 7, 13, 17, 19,', '53 1', '126 42', '[7, 41, 54, 100, 151, 34, 140, 140, 119, 93]', "'The Hobbit'"):
        check('SHEET-PY', want in out, 'worked example prints %s' % want)
    check('SHEET-PY', 'Error' not in out, 'no Python errors in the worked example')

    section('BOT 1-5: RSA by hand')
    p, q = nums(1, 'p', 'q')
    phi = (p - 1) * (q - 1)
    cands = [int(x) for x in re.search(r'e: ([\d, ]+)\?', prompts[1]).group(1).split(',')]
    import math
    valid = [e for e in cands if math.gcd(e, phi) == 1]
    quiz(1, ' '.join(map(str, valid)), ' '.join(map(str, cands)))

    p, q, e = nums(2, 'p', 'q', 'e')
    d = pow(e, -1, (p - 1) * (q - 1))
    quiz(2, str(d), str(d + 2))

    m, e, n = nums(3, 'm', 'e', 'N')
    quiz(3, str(pow(m, e, n)), str(m))

    c, p, q, e = nums(4, 'c', 'p', 'q', 'e')
    mm = pow(c, pow(e, -1, (p - 1) * (q - 1)), p * q)
    quiz(4, str(mm), str(c))

    d, n = nums(5, 'd', 'N')
    ct = re.search(r'N = \d+: ([\d ]+)$', prompts[5]).group(1)
    # the sheet's hint: put the numbers in a list and use the worked example's join line
    rc, out, _ = repl("ct = [%s]\nd = %d\nN = %d\n''.join(chr(pow(c, d, N)) for c in ct)" % (ct.replace(' ', ', '), d, n))
    word = ''.join(chr(pow(int(c), d, n)) for c in ct.split())
    check('SHEET-PY', repr(word) in out, 'the hint for #5 decrypts the word in the REPL')
    quiz(5, word, word[::-1] + 'x')


def openssl_rsa():
    section('SHEET: RSA with OpenSSL')
    rc, out = sh(bash_of('rsa-with-openssl'))
    check('SHEET-RSA', '-----BEGIN PRIVATE KEY-----' in out, 'genrsa writes BEGIN PRIVATE KEY (as the sheet says for OpenSSL 3)')
    check('SHEET-RSA', 'publicExponent: 65537' in out and 'prime1:' in out and 'privateExponent:' in out, 'rsa -text shows modulus, e = 65537, d, primes')
    check('SHEET-RSA', 'Meet me at the library at 3' in out, 'pkeyutl encrypt/decrypt round trip')
    rc, out = sh('cd ~/rsa_example && openssl pkeyutl -encrypt -pubin -inkey bob.pub -in msg.txt -out msg2.enc && cmp msg.enc msg2.enc; echo cmp=$?')
    check('SHEET-RSA', 'cmp=1' in out, 'encrypting twice gives different ciphertexts (the Note)')

    section('BOT 6: key pair')
    D = '~/challenge_6'
    bot_goto(6)
    expect_hint(6, 'no directory', bot_ready(6, 'no directory'), "I can't find")
    sh('mkdir -p %s && cd %s && openssl genrsa -out key.priv 2048' % (D, D))
    expect_hint(6, 'only key.priv', bot_ready(6, 'only key.priv'), 'key.pub')
    sh('cd %s && cp key.priv key.pub' % D)
    expect_hint(6, 'private key saved as key.pub', bot_ready(6, 'private key as key.pub'), "isn't an RSA public key")
    sh('cd %s && openssl genrsa -out other.priv 2048 && openssl rsa -in other.priv -pubout -out key.pub && rm other.priv' % D)
    expect_hint(6, 'key.pub from another key', bot_ready(6, 'mismatched key.pub'), "isn't the public half")
    sh('cd %s && openssl rsa -in key.priv -pubout -out key.pub' % D)
    expect_flag(6, 'correct key pair', bot_ready(6, 'correct'))

    section('BOT 7: encrypt a message')
    D = '~/challenge_7'
    msg = re.search(r'encrypt the text (\S+) with', prompts[7]).group(1)
    sh('mkdir -p %s && cd %s && openssl genrsa -out key.priv 2048 && openssl rsa -in key.priv -pubout -out key.pub' % (D, D))
    bot_goto(7)
    expect_hint(7, 'no message.enc', bot_ready(7, 'no message.enc'), 'message.enc')
    sh("cd %s && echo '\"%s\"' > message.txt && openssl pkeyutl -encrypt -pubin -inkey key.pub -in message.txt -out message.enc" % (D, msg))
    expect_hint(7, 'quotes in the message', bot_ready(7, 'quotes'), 'but not to')
    sh('cd %s && echo %s > message.txt && openssl pkeyutl -encrypt -pubin -inkey ~/challenge_6/key.pub -in message.txt -out message.enc' % (D, msg))
    expect_hint(7, 'encrypted with the wrong public key', bot_ready(7, 'wrong key'), "couldn't decrypt")
    sh('cd %s && openssl pkeyutl -encrypt -pubin -inkey key.pub -in message.txt -out message.enc' % D)
    expect_flag(7, 'correct', bot_ready(7, 'correct'))

    section('SHEET: digital signatures')
    rc, out = sh(bash_of('digital-signatures'))
    check('SHEET-SIG', out.count('Verified OK') == 1 and re.search(r'Verification [Ff]ailure', out), 'Verified OK, then a verification failure after the edit')

    section('BOT 8: sign')
    D = '~/challenge_8'
    msg = re.search(r'put the text (\S+) in', prompts[8]).group(1)
    sh('mkdir -p %s && cp ~/challenge_6/key.priv ~/challenge_6/key.pub %s/' % (D, D))   # the sheet's hint
    bot_goto(8)
    sh('cd %s && echo %s > message.txt && openssl dgst -sha256 -sign key.priv -out message.sig message.txt && echo x >> message.txt' % (D, msg))
    expect_hint(8, 'message.txt edited after signing', bot_ready(8, 'edited after signing'), 'should contain exactly')
    sh('cd %s && echo %s > message.txt && openssl dgst -sha256 -sign ~/challenge_7/key.priv -out message.sig message.txt' % (D, msg))
    expect_hint(8, 'signed with another private key', bot_ready(8, 'wrong key'), "doesn't verify")
    sh('cd %s && openssl dgst -sha256 -sign key.priv -out message.sig message.txt' % D)
    expect_flag(8, 'correct', bot_ready(8, 'correct'))


def hybrid():
    section('SHEET: hybrid encryption')
    rc, out = sh(bash_of('hybrid-encryption'))
    check('SHEET-HYB', re.search(r'too large|error', out, re.I) is not None, 'RSA on the 3 KB report fails: %s' % (
        [l for l in out.splitlines() if re.search('large|error', l, re.I)] or ['(no error)'])[0][:100])
    check('SHEET-HYB', 'Bob has the report' in out, 'AES + wrapped key round trip')

    section('BOT 9: hybrid encryption')
    D = '~/challenge_9'
    sh('mkdir -p %s && cp ~/challenge_6/key.priv ~/challenge_6/key.pub %s/ && cd %s && head -c 3000 /dev/urandom | base64 > report.txt' % (D, D, D))
    bot_goto(9)
    sh('cd %s && openssl rand -hex 32 > aes.key && openssl enc -aes-256-cbc -in report.txt -out report.enc -pass file:aes.key && '
       'openssl pkeyutl -encrypt -pubin -inkey key.pub -in aes.key -out aes.key.enc' % D)
    expect_hint(9, 'forgot -pbkdf2', bot_ready(9, 'no -pbkdf2'), "didn't decrypt to report.txt")
    sh('cd %s && openssl enc -aes-256-cbc -pbkdf2 -in report.txt -out report.enc -pass file:aes.key' % D)
    expect_hint(9, 'plaintext aes.key left behind', bot_ready(9, 'aes.key left'), 'still sitting')
    sh('cd %s && rm aes.key' % D)
    expect_flag(9, 'correct', bot_ready(9, 'correct'))


def chain():
    section('SHEET: chains of trust')
    rc, out = sh(bash_of('chains-of-trust'))
    check('SHEET-CA', 'leaf.crt: OK' in out and 'issuer=CN = My Intermediate CA' in out, 'chain verifies; leaf issued by the intermediate')
    rc, out = sh('cd ~/ca_example && openssl verify -CAfile root.crt leaf.crt')
    check('SHEET-CA', rc != 0, 'verify without the intermediate fails (the Question): %s' % out.strip().splitlines()[-1][:100])

    section('BOT 10: chain of trust')
    D = '~/challenge_10'
    name = re.search(r'certificate for (\S+), signed', prompts[10]).group(1)
    script = bash_of('chains-of-trust').replace('~/ca_example', D).replace('www.example.com', name)
    no_ext = script.replace(' -extfile ca.ext', '')
    bot_goto(10)
    sh(no_ext)
    expect_hint(10, 'intermediate signed without ca.ext', bot_ready(10, 'no ca.ext'), "isn't marked as a CA")
    sh(script)
    sh('cd %s && openssl x509 -req -in leaf.csr -CA root.crt -CAkey root.key -CAcreateserial -days 365 -extfile leaf.ext -out leaf.crt' % D)
    expect_hint(10, 'leaf signed by the root', bot_ready(10, 'leaf by root'), 'straight by the root')
    sh(script.replace(name, 'wrong.example'))
    expect_hint(10, 'leaf for the wrong name', bot_ready(10, 'wrong name'), "isn't for")
    sh(script)
    expect_flag(10, 'correct', bot_ready(10, 'correct'))


def emudhra():
    section('SHEET: what a key of 4 looks like')
    sh('mkdir -p ~/emudhra_example')
    ex = '%s/emudhra_example' % HOME
    py = blocks('what-a-key-of-4-looks-like', 'python')[0]
    repl(py, cwd=ex)
    sheet_pem, sheet_dump = blocks('what-a-key-of-4-looks-like', '')[:2]
    rc, out = sh('cat emudhra4.pem', cwd=ex)
    check('SHEET-K4', out.strip() == sheet_pem.strip(), 'pycryptodome writes exactly the PEM printed on the sheet')
    rc, out = sh('openssl pkey -in emudhra4.pem -noout -text | head -7', cwd=ex)
    check('SHEET-K4', out.strip() == sheet_dump.strip(), 'openssl pkey -text matches the dump on the sheet')
    rc, out, dur = repl(blocks('brute-forcing-a-tiny-key', 'python')[0], cwd=ex)
    check('SHEET-K4', 'Found the private key: 4' in out, 'brute force finds 4 (%.1fs)' % dur)

    section('BOT 11: brute-force a CA key')
    bot_goto(11)
    r = bot_ready(11, 'place the files')
    check('BOT#11', said(r, 'Start counting') and said(r, 'answer X'), 'files placed, question asked')
    D = '%s/challenge_11' % HOME
    if not os.path.isdir(D):
        result('BOT#11', 'PROBLEM', '~/challenge_11 was not created by the bot')
        os.makedirs(D)
    sh('\n'.join(l for l in blocks('hackerbot-attack-11', 'bash')[0].splitlines() if l.strip() != 'python3'))
    brute = blocks('brute-forcing-a-tiny-key', 'python')[0].replace("'emudhra4_pub.pem'", "'inter_pub.pem'")
    consequence = blocks('hackerbot-attack-11', 'python')[0]
    rc, out, dur = repl(brute + '\n' + consequence, cwd=D)
    m = re.search(r'Found the private key: (\d+)', out)
    k = int(m.group(1)) if m else None
    check('BOT#11', k in (56, 66, 115, 155, 156, 230, 237), 'brute force found k = %s (%.1fs), one of the seven real keys' % (k, dur))
    r = bot_answer(11, str((k or 0) + 1), 'wrong answer')
    check('BOT#11', said(r, 'Incorrect'), 'wrong key rejected')
    expect_flag(11, 'right key', bot_answer(11, str(k), 'right answer'))
    forge = [b for b in blocks('hackerbot-attack-11', 'bash') if 'fake.key' in b][0]
    rc, out = sh(forge, cwd=D)
    check('SHEET-FORGE', 'fake.crt: OK' in out, 'certificate forged with the recovered key verifies against the root')

    section('SHEET: block the key')
    rc, out = sh(bash_of('block-the-key'))
    check('SHEET-SPKI', re.search(r'^[0-9a-f]{64}  -$', out, re.M) is not None, 'SPKI hash command prints a SHA-256 hash')

    section('BOT 12: SPKI hash')
    bot_goto(12)
    sh('rm -rf %s/lab_trust_inter.crt' % D)
    r = bot_ready(12, 'files back')
    check('BOT#12', said(r, 'Hash it') and os.path.exists(D + '/lab_trust_inter.crt'), 'files put back, question asked')
    cmd = bash_of('block-the-key').replace('~/ca_example', D).replace('leaf.crt', 'lab_trust_inter.crt')
    rc, out = sh(cmd)
    m = re.search(r'^([0-9a-f]{64})  -$', out, re.M)
    hexhash = m.group(1) if m else 'no-hash'
    root = (sh(cmd.replace('lab_trust_inter.crt', 'lab_trust_root.crt'), quiet=True)[1].split() or ['no-hash'])[0]
    r = bot_answer(12, root, 'root hash (wrong cert)')
    check('BOT#12', said(r, 'Incorrect'), "root's hash rejected")
    expect_flag(12, 'intermediate SPKI hash (hex)', bot_answer(12, hexhash, 'right answer'))

    section('SHEET: generate keys properly')
    rc, out = sh(bash_of('generate-keys-properly'))
    priv = re.search(r'priv:\n((?:\s+[0-9a-f:]+\n)+)', out)
    nbytes = len(re.findall(r'[0-9a-f]{2}', priv.group(1))) if priv else 0
    zeros = len(re.findall(r'\b00\b', priv.group(1))) if priv else 0
    check('SHEET-GOOD', priv is not None and 46 <= nbytes <= 49 and zeros < 10, 'a proper P-384 key: %d bytes, %d zero' % (nbytes, zeros))


def dh():
    section('SHEET: key exchange')
    rc, out, _ = repl(blocks('key-exchange', 'python')[0])
    check('SHEET-DH', '8 19 2 2' in out, 'worked example prints 8 19 2 2')
    section('BOT 13: Diffie-Hellman')
    p, g, a, B = nums(13, 'p', 'g', 'a', 'B')
    A, s = pow(g, a, p), pow(B, a, p)
    quiz(13, '%d %d' % (A, s), '%d %d' % (s, A))


def run_all(args):
    maths()
    openssl_rsa()
    hybrid()
    chain()
    emudhra()
    dh()
    section('FLAGS')
    distinct = {f for _, f in flags_seen}
    for where, f in flags_seen:
        note('%s: %s' % (where, f))
    check('FLAGS', len(distinct) == 13, '%d distinct flags collected (13 expected)' % len(distinct))


def main():
    global _log_fh, bot, BLOCKS
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument('--irc-host', default='hackerbot')
    ap.add_argument('--irc-port', type=int, default=6667)
    ap.add_argument('--bot', default='Hackerbot')
    ap.add_argument('--irc-check', action='store_true')
    ap.add_argument('--yes', action='store_true')
    args = ap.parse_args()

    _log_fh = open(FULL_LOG, 'w')
    started = datetime.datetime.now()
    say('%s_test %s - full log: %s' % (LAB, VERSION, FULL_LOG))
    BLOCKS = sheet_blocks()
    say('Lab sheet: %d code blocks' % len(BLOCKS))
    bot = Hackerbot(args.irc_host, args.irc_port, args.bot, log=log)
    discover()
    say('Bot reachable: %d attacks' % len(prompts))
    if args.irc_check:
        for n in sorted(prompts):
            say('  #%d: %s' % (n, prompts[n][:160]))
        return
    if not args.yes:
        sys.exit('add --yes to run (this modifies the VM)')
    clean_start()
    try:
        run_all(args)
    except Exception as e:
        import traceback
        log(traceback.format_exc())
        result('SCRIPT', 'ERROR', 'test aborted early: %r (see full log)' % e)
    write_report(['%s automated test report  (script %s)' % (LAB, VERSION),
                  'started %s, took %d min' % (started.strftime('%Y-%m-%d %H:%M'), (datetime.datetime.now() - started).seconds // 60),
                  'host=%s user=%s' % (socket.gethostname(), os.environ.get('USER', '?'))])
    say('\nDone. Report: %s   Full log: %s' % (REPORT, FULL_LOG))


if __name__ == '__main__':
    main()
