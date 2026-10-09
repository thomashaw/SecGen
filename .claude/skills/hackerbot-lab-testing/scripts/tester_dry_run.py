#!/usr/bin/env python3
"""Dry-run a lab tester (built from assets/lab_test_skeleton.py) without VMs.

Every shell command the tester would run is syntax-checked with bash -n instead of executed (also the
script inside `bash -c '...'` and the remote command after `ssh ... user@host`), and the bot is the fake
one, so every Python code path in run_all() executes. Catches typos, bad quoting and crashes before
you spend a VM build on them. The fake bot answers everything with a pass, so results are meaningless.

    python3 fake_hackerbot.py bot.xml 16667 &
    python3 tester_dry_run.py lab_dev/mylab_test.py --port 16667 [--set U=alice --set IP=10.0.0.3 ...]

--set fills tester.C attributes that discover() would normally parse from the real prompts.
Standard library only; run from anywhere (hackerbot_irc.py must be next to the tester or on sys.path).
"""
import argparse
import importlib.util
import io
import os
import shlex
import subprocess
import sys

ap = argparse.ArgumentParser()
ap.add_argument('tester')
ap.add_argument('--host', default='127.0.0.1')
ap.add_argument('--port', type=int, default=16667)
ap.add_argument('--set', action='append', default=[], help='C attribute, e.g. U=alice')
a = ap.parse_args()

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))          # hackerbot_irc.py from the skill
spec = importlib.util.spec_from_file_location('tester', a.tester)
t = importlib.util.module_from_spec(spec)
spec.loader.exec_module(t)
out_dir = os.environ.get('TMPDIR', '/tmp')
t._log_fh = io.StringIO()
t.REPORT = os.path.join(out_dir, 'dry_run_report.txt')
t.bot = t.Hackerbot(a.host, a.port, 'Hackerbot')
t.discover()
for kv in a.set:
    k, v = kv.split('=', 1)
    setattr(t.C, k, v)

errors, count = [], [0]


def check(cmd, where):
    count[0] += 1
    p = subprocess.run(['bash', '-n', '-c', cmd], stdout=subprocess.PIPE, stderr=subprocess.PIPE, universal_newlines=True)
    if p.returncode != 0:
        errors.append('%s: %s :: %s' % (where, p.stderr.strip(), cmd[:200]))
    try:
        words = shlex.split(cmd)
    except ValueError as e:
        errors.append('%s: shlex %s :: %s' % (where, e, cmd[:200]))
        return
    for i, w in enumerate(words):
        if w == '-c' and i > 0 and words[i - 1] in ('bash', 'sh') and i + 1 < len(words):
            check(words[i + 1], where + ' > bash -c')
        if '@' in w and i > 0 and words[0] == 'ssh' and i + 1 < len(words) and i == len(words) - 2:
            check(words[i + 1], where + ' > ssh')


def fake_sh(cmd, timeout=0, quiet=False, label=None):
    check(cmd, 'sh')
    return 0, '0\n'


t.sh = fake_sh
t.run_all(None)
print('%d commands syntax-checked, %d errors' % (count[0], len(errors)))
for e in errors:
    print('  ' + e)
print('%d summary rows: %s' % (len(t.summary), ' '.join(r[0] for r in t.summary)))
t.write_report(['(dry run)'])
print('report: ' + t.REPORT)
sys.exit(1 if errors else 0)
