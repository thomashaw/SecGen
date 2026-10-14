#!/usr/bin/env python3
"""A tiny fake Hackerbot IRC server for testing lab testers / hackerbot_irc.py offline.

It answers 'list', 'goto N', 'ready' and 'answer X' using the real attack prompts from a rendered
bot.xml, with canned replies shaped like the real bot's (FYI block, ':) Well done! flag{...}', the
repeat message). It does NOT run any checks - use it to exercise IRC handling and code paths, not
to judge a lab. Standard library only.

    python3 fake_hackerbot.py bot.xml 16667 &       # then point the client at 127.0.0.1:16667
    python3 hackerbot_irc.py --host 127.0.0.1 --port 16667 list
"""
import re
import socket
import sys
import threading
import time
import xml.etree.ElementTree as ET

xml = open(sys.argv[1]).read()
xml = re.sub(r'<tutorial>.*?</tutorial>', '', xml, flags=re.S)
root = ET.fromstring(xml[xml.index('<hackerbot'):])
prompts = [(a.findtext('prompt') or '').strip() for a in root.findall('attack')]
port = int(sys.argv[2]) if len(sys.argv) > 2 else 16667
state = {'cur': 0}


def handle(conn):
    f = conn.makefile('rb')
    nick = None

    def send(line):
        conn.sendall((line + '\r\n').encode())

    def bot(text):
        for part in text.split('\n'):
            send(':Hackerbot!~hb@127.0.0.1 PRIVMSG %s :%s' % (nick, part))
            time.sleep(0.02)

    for raw in f:
        line = raw.decode().rstrip('\r\n')
        if line.startswith('NICK '):
            nick = line.split()[1]
        elif line.startswith('USER '):
            send(':fake 001 %s :Welcome' % nick)
            send('PING :keepalive')
        elif line.startswith('PRIVMSG Hackerbot :'):
            msg = line.split(' :', 1)[1]
            cur = state['cur']
            if msg == 'list':
                for i, p in enumerate(prompts):
                    bot('%sattack %d: %s' % ('--> ' if i == cur else '', i + 1, p))
            elif re.match(r'^goto \d+$', msg):
                state['cur'] = int(msg.split()[1]) - 1
                bot('Ok, skipping it along.')
                bot('** #%d **' % (state['cur'] + 1))
                bot(prompts[state['cur']])
                bot("When you are ready, simply say 'ready'.")
            elif msg == 'ready':
                bot('Here we go...')
                bot('FYI: OK something\nRESULT PASS')
                bot(':) Well done! flag{fake%02d}' % cur)
                if cur + 1 < len(prompts):
                    state['cur'] = cur + 1
                    bot('** #%d **' % (cur + 2))
                    bot(prompts[cur + 1])
                bot("Say 'ready', 'next', or 'previous'.")
            elif msg.startswith('answer '):
                bot('Correct')
                bot(':) flag{fakequiz}')
                bot("'Ready'?")


srv = socket.socket()
srv.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
srv.bind(('127.0.0.1', port))
srv.listen()
while True:
    c, _ = srv.accept()
    threading.Thread(target=handle, args=(c,), daemon=True).start()
