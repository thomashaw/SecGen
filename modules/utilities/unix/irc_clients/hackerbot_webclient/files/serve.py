#!/usr/bin/env python3
"""HTTP server for the Hackerbot web client.

Serves the static client files from the working directory, plus a small
history API so the chat log is kept on this server as well as in the
student's browser:

  GET  /api/history   all saved records, as a JSON list
  POST /api/history   a JSON record, or a list of records, to append

Records are appended as JSON lines to history.jsonl in the state directory
(systemd's $STATE_DIRECTORY, or the third argument). The browser sends every
message it shows, and on load merges this log with its own, so the history
survives a cleared browser or a different desktop VM. The log is written by
the client, so it records what the browser displayed; it is not a tamper-proof
record of the bot's conversation.

It also tolerates hostnames over 63 characters: Python 3.13's http.server
calls socket.getfqdn() during bind, which goes through the IDNA codec. IDNA
labels are capped at 63 chars, so VMs whose generated hostname exceeds that
(common with SecGen's <prefix>-<scenario>-<system> naming) crash with
UnicodeEncodeError. The bind falls back to the bare host in that case.
"""
import http.server
import json
import os
import socket
import socketserver
import sys
import threading

API_PATH = '/api/history'
MAX_BODY = 1024 * 1024       # largest POST accepted (a full resync of old history)
MAX_TEXT = 16 * 1024         # longest single message kept
FIELDS = ('id', 'ts', 'date', 'dir', 'from', 'text')
DIRS = ('me', 'bot', 'clear')

history_file = None
lock = threading.Lock()


class SafeHTTPServer(socketserver.ThreadingMixIn, http.server.HTTPServer):
    daemon_threads = True

    def server_bind(self):
        socketserver.TCPServer.server_bind(self)
        host, port = self.server_address[:2]
        try:
            self.server_name = socket.getfqdn(host)
        except (UnicodeError, socket.herror, OSError):
            self.server_name = host or 'localhost'
        self.server_port = port


def clean_record(rec):
    """Keep only known fields of a well-formed record, or return None."""
    if not isinstance(rec, dict):
        return None
    if rec.get('dir') not in DIRS:
        return None
    if not isinstance(rec.get('id'), str) or not isinstance(rec.get('date'), str):
        return None
    out = {}
    for key in FIELDS:
        val = rec.get(key)
        if isinstance(val, str):
            out[key] = val[:MAX_TEXT]
        elif key == 'ts' and isinstance(val, (int, float)):
            out[key] = val
    return out


def read_history():
    records = []
    seen = set()
    try:
        with open(history_file, encoding='utf-8') as f:
            for line in f:
                try:
                    rec = json.loads(line)
                except ValueError:
                    continue  # a torn line from a crash; skip it
                if isinstance(rec, dict) and rec.get('id') not in seen:
                    seen.add(rec.get('id'))
                    records.append(rec)
    except FileNotFoundError:
        pass
    return records


class Handler(http.server.SimpleHTTPRequestHandler):
    def send_json(self, code, obj):
        body = json.dumps(obj).encode('utf-8')
        self.send_response(code)
        self.send_header('Content-Type', 'application/json')
        self.send_header('Cache-Control', 'no-store')
        self.send_header('Content-Length', str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def do_GET(self):
        if self.path.split('?')[0] == API_PATH:
            with lock:
                self.send_json(200, read_history())
            return
        super().do_GET()

    def do_POST(self):
        if self.path.split('?')[0] != API_PATH:
            self.send_json(404, {'error': 'not found'})
            return
        try:
            length = int(self.headers.get('Content-Length', 0))
        except ValueError:
            length = -1
        if length < 0 or length > MAX_BODY:
            self.send_json(413, {'error': 'body too large'})
            return
        try:
            data = json.loads(self.rfile.read(length).decode('utf-8'))
        except (ValueError, UnicodeDecodeError):
            self.send_json(400, {'error': 'invalid JSON'})
            return
        items = data if isinstance(data, list) else [data]
        records = [r for r in (clean_record(i) for i in items) if r]
        if not records:
            self.send_json(400, {'error': 'no valid records'})
            return
        with lock:
            known = {r.get('id') for r in read_history()}
            new = [r for r in records if r['id'] not in known]
            if new:
                with open(history_file, 'a', encoding='utf-8') as f:
                    f.write(''.join(json.dumps(r) + '\n' for r in new))
        self.send_json(200, {'saved': len(new)})


if __name__ == '__main__':
    bind = sys.argv[1] if len(sys.argv) > 1 else '0.0.0.0'
    port = int(sys.argv[2]) if len(sys.argv) > 2 else 8080
    state_dir = (sys.argv[3] if len(sys.argv) > 3
                 else os.environ.get('STATE_DIRECTORY', '').split(':')[0] or '.')
    history_file = os.path.join(state_dir, 'history.jsonl')
    with SafeHTTPServer((bind, port), Handler) as httpd:
        httpd.serve_forever()
