#!/usr/bin/env python3
"""Talk to a SecGen Hackerbot over IRC, as a student would - from a lab VM that can reach the bot.

Works for any hackerbot_config lab: it only relies on the bot's control words (list, goto N, ready,
answer X) and on every lab's 'repeat' message mentioning 'ready' plus 'next'/'previous'
(override with --end-ready / --end-goto for an unusual lab). Python 3.7+, standard library only.

Command line (prints the bot's replies, then a summary; exit 0 unless the bot couldn't be reached):

    python3 hackerbot_irc.py list                      # every attack's prompt
    python3 hackerbot_irc.py goto 4                    # move the bot to attack 4
    python3 hackerbot_irc.py ready                     # run the current attack
    python3 hackerbot_irc.py ready 4                   # goto 4, then ready
    python3 hackerbot_irc.py answer 1e9c3265           # answer the current quiz
    python3 hackerbot_irc.py say 'some text'           # anything else
    options: --host hackerbot --port 6667 --bot Hackerbot --json

As a module (e.g. in a lab tester; copy this file next to it):

    from hackerbot_irc import Hackerbot
    bot = Hackerbot('hackerbot')
    prompts = bot.list_attacks()          # {1: 'Use scp to ...', ...}
    bot.goto(4)
    r = bot.ready()                       # Reply: r.lines, r.fyi, r.verdict, r.flags, r.ended
    r = bot.answer('1e9c3265')

Bot state (the current attack) is shared by everyone talking to the bot, so always goto before ready
when testing. Replies to 'ready' can take minutes (the bot SSHes in and runs checks).
"""
import argparse
import json
import random
import re
import socket
import sys
import time

FLAG_RE = re.compile(r'flag\{[^}]*\}')
# default end-of-reply markers (see the module docstring)
END_READY_RE = re.compile(r"'ready'.*'(next|previous)'|^Say 'ready'", re.I)
END_GOTO_RE = re.compile(r"'ready'", re.I)
END_ANSWER_RE = re.compile(r"'ready'|^Incorrect|^There is no question", re.I)
PRIVMSG_RE = re.compile(r':([^!\s]+)!\S* PRIVMSG \S+ :(.*)$')


class Reply(object):
    """What the bot said in response to one message."""

    def __init__(self, lines, ended):
        self.lines = lines            # every line the bot sent, in order
        self.ended = ended            # False if we timed out before the end-of-reply marker
        self.flags = FLAG_RE.findall('\n'.join(lines))
        self.fyi = fyi_block(lines)   # the FYI: output (command output the bot checked), joined with ' / '
        self.verdict = verdict(lines)

    def as_dict(self):
        return {'lines': self.lines, 'ended': self.ended, 'flags': self.flags, 'fyi': self.fyi, 'verdict': self.verdict}


def fyi_block(lines):
    """The bot's FYI output: the 'FYI: ...' line plus its continuation lines (IRC splits multi-line text)."""
    out, inside = [], False
    for l in lines:
        if l.startswith('FYI:'):
            inside = True
            out.append(l)
        elif inside:
            if re.match(r'^(:\)|:\(|\*\* #)', l) or FLAG_RE.search(l) or END_READY_RE.search(l):
                break
            out.append(l)
    return ' / '.join(out)


def verdict(lines):
    """Best guess at the bot's condition message: a line with a flag, else the first ':)' / ':(' line,
    else the last line before the end-of-reply marker that isn't an attack header."""
    for l in lines:
        if FLAG_RE.search(l):
            return l
    for l in lines:
        if l.startswith(':)') or l.startswith(':('):
            return l
    cands = [l for l in lines if not END_READY_RE.search(l) and not l.startswith('** #') and not l.startswith('FYI:')]
    return cands[-1] if cands else ''


class Hackerbot(object):
    def __init__(self, host='hackerbot', port=6667, bot='Hackerbot', nick=None, log=None,
                 end_ready=END_READY_RE, end_goto=END_GOTO_RE, end_answer=END_ANSWER_RE):
        self.bot = bot
        self.log = log or (lambda s: None)
        self.end_ready, self.end_goto, self.end_answer = end_ready, end_goto, end_answer
        self.buf = b''
        self.sock = socket.create_connection((host, port), timeout=20)
        self.nick = nick or 'labtest%04d' % random.randint(0, 9999)
        self._send('NICK ' + self.nick)
        self._send('USER %s 0 * :hackerbot lab tester' % self.nick)
        end = time.time() + 40
        while time.time() < end:
            line = self._readline(end - time.time())
            if not line:
                continue
            parts = line.split()
            if len(parts) > 1 and parts[1] == '001':
                self.log('IRC: registered as ' + self.nick)
                return
            if len(parts) > 1 and parts[1] == '433':      # nick in use
                self.nick += 'x'
                self._send('NICK ' + self.nick)
        raise RuntimeError('IRC registration timed out')

    # ---- low level
    def _send(self, s):
        self.sock.sendall((s + '\r\n').encode())

    def _readline(self, timeout):
        """One line from the server, '' for a PING we answered, None on timeout."""
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

    def _bot_line(self, line):
        m = PRIVMSG_RE.match(line or '')
        if m and m.group(1).lower() == self.bot.lower():
            return m.group(2)
        return None

    def tell(self, msg, until=None, timeout=330, idle=None):
        """PRIVMSG the bot; collect its lines until `until` matches one (plus anything in the same burst),
        or - if idle is set - until it has been quiet for `idle` seconds, or until timeout.
        Returns Reply."""
        self.log('  ME> ' + msg)
        self._send('PRIVMSG %s :%s' % (self.bot, msg))
        got, end = [], time.time() + timeout
        while time.time() < end:
            wait = end - time.time()
            if idle and got:
                wait = min(wait, idle)
            line = self._readline(wait)
            if line is None:
                return Reply(got, bool(idle and got))
            text = self._bot_line(line)
            if text is None:
                continue
            got.append(text)
            self.log('  BOT< ' + text)
            if until is not None and until.search(text):
                while True:                        # the rest of the same burst
                    extra = self._readline(1.5)
                    if not extra:
                        break
                    t2 = self._bot_line(extra)
                    if t2 is not None:
                        got.append(t2)
                        self.log('  BOT< ' + t2)
                return Reply(got, True)
        return Reply(got, False)

    # ---- the bot's commands
    def list_attacks(self):
        r = self.tell('list', idle=6, timeout=40)
        prompts = {}
        for l in r.lines:
            m = re.match(r'^(?:--> )?attack (\d+): (.*)$', l)
            if m:
                prompts[int(m.group(1))] = m.group(2)
        return prompts

    def goto(self, n):
        return self.tell('goto %d' % int(n), until=self.end_goto, timeout=30)

    def ready(self, timeout=330):
        return self.tell('ready', until=self.end_ready, timeout=timeout)

    def answer(self, text):
        return self.tell('answer ' + text, until=self.end_answer, timeout=40, idle=4)

    def close(self):
        try:
            self._send('QUIT :done')
            self.sock.close()
        except OSError:
            pass


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument('action', choices=['list', 'goto', 'ready', 'answer', 'say'])
    ap.add_argument('arg', nargs='*')
    ap.add_argument('--host', default='hackerbot')
    ap.add_argument('--port', type=int, default=6667)
    ap.add_argument('--bot', default='Hackerbot')
    ap.add_argument('--end-ready', help='regex marking the end of a ready reply (default: the repeat message)')
    ap.add_argument('--end-goto', help="regex marking the end of a goto reply (default: any line with 'ready')")
    ap.add_argument('--json', action='store_true', help='print the reply as JSON')
    ap.add_argument('-v', '--verbose', action='store_true')
    a = ap.parse_args()
    kw = {}
    if a.end_ready:
        kw['end_ready'] = re.compile(a.end_ready)
    if a.end_goto:
        kw['end_goto'] = re.compile(a.end_goto)
    try:
        bot = Hackerbot(a.host, a.port, a.bot, log=(lambda s: print(s, file=sys.stderr)) if a.verbose else None, **kw)
    except (OSError, RuntimeError) as e:
        sys.exit('Could not reach the bot at %s:%d (%s)' % (a.host, a.port, e))

    if a.action == 'list':
        prompts = bot.list_attacks()
        if a.json:
            print(json.dumps(prompts, indent=1))
        else:
            for n in sorted(prompts):
                print('#%d: %s' % (n, prompts[n]))
        bot.close()
        return
    if a.action == 'goto':
        r = bot.goto(a.arg[0])
    elif a.action == 'ready':
        if a.arg:
            bot.goto(a.arg[0])
        r = bot.ready()
    elif a.action == 'answer':
        r = bot.answer(' '.join(a.arg))
    else:
        r = bot.tell(' '.join(a.arg), idle=5, timeout=60)
    bot.close()
    if a.json:
        print(json.dumps(r.as_dict(), indent=1))
        return
    for l in r.lines:
        print('BOT> ' + l)
    print('--\nverdict: %s\nflags: %s%s' % (r.verdict, ' '.join(r.flags) or '(none)',
                                          '' if r.ended else '\n(no end-of-reply marker seen - timed out)'))


if __name__ == '__main__':
    main()
