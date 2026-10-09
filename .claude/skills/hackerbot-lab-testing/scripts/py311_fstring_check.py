#!/usr/bin/env python3
"""Flag f-strings that Python < 3.12 can't parse, so a tester written on a newer Python still runs on
the lab VMs (Debian 12 = Python 3.11; older bases are older still).

    python3 py311_fstring_check.py my_lab_test.py

Catches an expression part that reuses the enclosing quote character, and a backslash inside an
expression part. Needs Python 3.12+ to run (it uses the 3.12 tokenizer's FSTRING_* tokens); on an older
Python just run `python3 -c "import ast; ast.parse(open('my_lab_test.py').read())"` there instead.
"""
import sys
import tokenize

if not hasattr(tokenize, 'FSTRING_START'):
    sys.exit('needs Python 3.12+; on older Pythons a plain ast.parse() is the real check')
bad = 0
with open(sys.argv[1], 'rb') as f:
    toks = list(tokenize.tokenize(f.readline))
stack = []
for t in toks:
    if t.type == tokenize.FSTRING_START:
        q = t.string.lstrip('fFrRbB')
        if stack and len(stack[-1]) == 1 and q[0] == stack[-1][0]:
            print('%d: nested f-string reuses outer quote %r' % (t.start[0], q))
            bad += 1
        stack.append(q)
    elif t.type == tokenize.FSTRING_END:
        stack.pop()
    elif stack and t.type == tokenize.STRING:
        s = t.string.lstrip('rRbBuU')
        if len(stack[-1]) == 1 and s[0] == stack[-1]:
            print('%d: string %r inside an f-string reuses the outer quote' % (t.start[0], t.string[:30]))
            bad += 1
        if '\\' in t.string:
            print('%d: backslash inside an f-string expression: %r' % (t.start[0], t.string[:30]))
            bad += 1
print('%d problem(s)' % bad)
sys.exit(1 if bad else 0)
