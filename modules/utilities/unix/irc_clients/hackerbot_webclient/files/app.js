(function () {
  // NICK, TARGET, WS_URL come from config.js, loaded before this file.

  var HISTORY_KEY = 'hackerbot_webclient_history';
  // Served by serve.py next to this file: the copy of the history kept on
  // the hackerbot server. Relative, so it follows whatever host served us.
  var HISTORY_API = 'api/history';
  var TIMESTAMPS_KEY = 'hackerbot_webclient_timestamps';
  var COMMANDS = ['ready', 'next', 'previous', 'list', 'goto 1', 'answer yes', '/clear', '/flags'];
  // Commands handled here in the browser; never sent to the bot.
  var LOCAL_COMMANDS = {
    '/clear': 'blank the chat window (scroll up to see earlier messages)',
    '/flags': 'list the flags Hackerbot has sent to this browser, by attack, and the attacks still to do',
    '/timestamps': 'show or hide message times',
    '/help': 'list these commands'
  };
  var FLAG_RE = /(flag\{[^}]*\})/g;
  var FLAG_TEST_RE = /^flag\{[^}]*\}$/;

  // Longest/most specific patterns first, so e.g. :-) is matched before :).
  var EMOTICONS = [
    [/:-\)/g, '🙂'], [/:\)/g, '🙂'],
    [/:-\(/g, '☹️'], [/:\(/g, '☹️'],
    [/:-D/g, '😀'], [/:D/g, '😀'],
    [/:-P/gi, '😛'], [/:P/gi, '😛'],
    [/;-\)/g, '😉'], [/;\)/g, '😉'],
    [/B\)/g, '😎'], [/8\)/g, '😎'],
    [/:-O/gi, '😮'], [/:O/gi, '😮'],
    [/<3/g, '❤️']
  ];

  // SHA-256 as hex. crypto.subtle only exists in secure contexts (https or
  // localhost), and this page is plain http on a lab IP, so do it here.
  function sha256Hex(str) {
    var K = [0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,
      0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174,
      0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
      0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7, 0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967,
      0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85,
      0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3, 0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070,
      0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
      0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208, 0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2];
    var H = [0x6a09e667, 0xbb67ae85, 0x3c6ef372, 0xa54ff53a, 0x510e527f, 0x9b05688c, 0x1f83d9ab, 0x5be0cd19];
    var bytes = unescape(encodeURIComponent(str));  // UTF-8, one char per byte
    var n = bytes.length;
    var words = [];
    for (var i = 0; i < n; i++) {
      words[i >> 2] |= bytes.charCodeAt(i) << (24 - (i % 4) * 8);
    }
    words[n >> 2] |= 0x80 << (24 - (n % 4) * 8);
    var total = (((n + 8) >> 6) + 1) * 16;
    for (i = words.length; i < total; i++) {
      words[i] = words[i] || 0;
    }
    words[total - 1] = n * 8;
    function rotr(x, r) { return (x >>> r) | (x << (32 - r)); }
    var w = new Array(64);
    for (var blk = 0; blk < total; blk += 16) {
      var a = H[0], b = H[1], c = H[2], d = H[3], e = H[4], f = H[5], g = H[6], h = H[7];
      for (var t = 0; t < 64; t++) {
        if (t < 16) {
          w[t] = words[blk + t] | 0;
        } else {
          var s0 = rotr(w[t - 15], 7) ^ rotr(w[t - 15], 18) ^ (w[t - 15] >>> 3);
          var s1 = rotr(w[t - 2], 17) ^ rotr(w[t - 2], 19) ^ (w[t - 2] >>> 10);
          w[t] = (w[t - 16] + s0 + w[t - 7] + s1) | 0;
        }
        var t1 = (h + (rotr(e, 6) ^ rotr(e, 11) ^ rotr(e, 25)) + ((e & f) ^ (~e & g)) + K[t] + w[t]) | 0;
        var t2 = ((rotr(a, 2) ^ rotr(a, 13) ^ rotr(a, 22)) + ((a & b) ^ (a & c) ^ (b & c))) | 0;
        h = g; g = f; f = e; e = (d + t1) | 0; d = c; c = b; b = a; a = (t1 + t2) | 0;
      }
      H[0] = (H[0] + a) | 0; H[1] = (H[1] + b) | 0; H[2] = (H[2] + c) | 0; H[3] = (H[3] + d) | 0;
      H[4] = (H[4] + e) | 0; H[5] = (H[5] + f) | 0; H[6] = (H[6] + g) | 0; H[7] = (H[7] + h) | 0;
    }
    return H.map(function (x) { return ('0000000' + (x >>> 0).toString(16)).slice(-8); }).join('');
  }

  function applyEmoticons(text) {
    return EMOTICONS.reduce(function (s, pair) {
      return s.replace(pair[0], pair[1]);
    }, text);
  }

  var logEl = document.getElementById('log');
  var sidebarEl = document.getElementById('sidebar');
  var msgInput = document.getElementById('msg');
  var sendBtn = document.getElementById('sendBtn');
  var cmdBtn = document.getElementById('cmdBtn');
  var cmdPopup = document.getElementById('cmdPopup');

  var registered = false;
  var sock;

  // Which attack each of the bot's chat flags belongs to, from the bot's
  // reply to 'flagmap' (asked for on connect; those lines are never shown or
  // saved). It holds short hashes of the flags, not the flags:
  //   FLAGMAP <attack count> <first attack in line> <hashes>;<hashes>;...
  var FLAGMAP_HASH_LEN = 12;
  var flagMap = null;     // [{ attack: n, hashes: [...] }] for attacks with chat flags
  var flagMapParts = [];

  function handleFlagMapLine(text) {
    var m = text.match(/^FLAGMAP (\d+) (\d+) ?([0-9a-f,;]*)$/);
    if (!m) {
      return;
    }
    var total = +m[1];
    m[3].split(';').forEach(function (entry, i) {
      flagMapParts[+m[2] - 1 + i] = entry ? entry.split(',') : [];
    });
    for (var i = 0; i < total; i++) {
      if (!flagMapParts[i]) {
        return;  // more lines to come
      }
    }
    flagMap = [];
    flagMapParts.slice(0, total).forEach(function (hashes, i) {
      if (hashes.length) {
        flagMap.push({ attack: i + 1, hashes: hashes });
      }
    });
  }

  function todayStr() {
    var d = new Date();
    var mm = ('0' + (d.getMonth() + 1)).slice(-2);
    var dd = ('0' + d.getDate()).slice(-2);
    return '' + d.getFullYear() + mm + dd;
  }

  var TODAY = todayStr();

  function loadHistory() {
    try {
      var raw = localStorage.getItem(HISTORY_KEY);
      return raw ? JSON.parse(raw) : {};
    } catch (e) {
      return {};
    }
  }

  function newId() {
    return Date.now().toString(36) + '-' + Math.random().toString(36).slice(2, 10);
  }

  var chatlog = loadHistory();
  if (!chatlog[TODAY]) {
    chatlog[TODAY] = [];
  }

  // Every record carries an id (to merge with the server copy without
  // duplicates), its date and a timestamp. Records saved by older versions
  // of this client have none; give them ids now, and ts 0 so they sort first.
  var knownIds = {};
  var migrated = false;
  Object.keys(chatlog).forEach(function (d) {
    chatlog[d].forEach(function (rec) {
      if (!rec.id) {
        rec.id = newId();
        rec.ts = 0;
        rec.local = true;
        migrated = true;
      }
      rec.date = d;
      knownIds[rec.id] = true;
    });
  });

  var activeDate = TODAY;

  function saveHistory() {
    try {
      localStorage.setItem(HISTORY_KEY, JSON.stringify(chatlog));
    } catch (e) {
      // localStorage unavailable/full: history just won't persist across reloads.
    }
  }

  if (migrated) {
    saveHistory();
  }

  // Send records to the server's log. Failures are ignored: syncHistory()
  // re-sends anything the server is missing the next time the page loads.
  function pushRecords(recs) {
    if (!recs.length || !window.fetch) {
      return;
    }
    fetch(HISTORY_API, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(recs)
    }).catch(function () {});
  }

  // Two-way merge with the server's copy: records only the server has
  // (another browser, or cleared local storage) are added here, and records
  // only this browser has are sent to the server.
  function syncHistory() {
    if (!window.fetch) {
      return Promise.resolve();
    }
    return fetch(HISTORY_API, { cache: 'no-store' }).then(function (r) {
      if (!r.ok) {
        throw new Error('history API unavailable');
      }
      return r.json();
    }).then(function (serverRecs) {
      if (!Array.isArray(serverRecs)) {
        return;
      }
      var serverIds = {};
      var changedDates = {};
      serverRecs.forEach(function (rec) {
        if (!rec || !rec.id || !rec.date) {
          return;
        }
        serverIds[rec.id] = true;
        if (knownIds[rec.id]) {
          return;
        }
        knownIds[rec.id] = true;
        if (!chatlog[rec.date]) {
          chatlog[rec.date] = [];
        }
        chatlog[rec.date].push(rec);
        changedDates[rec.date] = true;
      });

      var missing = [];
      Object.keys(chatlog).forEach(function (d) {
        chatlog[d].forEach(function (rec) {
          if (!serverIds[rec.id]) {
            missing.push(rec);
          }
        });
      });
      pushRecords(missing);

      var dates = Object.keys(changedDates);
      if (!dates.length) {
        return;
      }
      dates.forEach(function (d) {
        chatlog[d].sort(function (a, b) { return (a.ts || 0) - (b.ts || 0); });
      });
      saveHistory();
      renderSidebar();
      if (changedDates[activeDate]) {
        renderLog(activeDate);
      }
    }).catch(function () {
      // Older serve.py or server unreachable: keep the browser-only history.
    });
  }

  function renderSidebar() {
    var dates = Object.keys(chatlog).sort().reverse();
    sidebarEl.textContent = '';
    dates.forEach(function (d) {
      var div = document.createElement('div');
      div.className = 'histitem' + (d === activeDate ? ' active' : '');
      div.textContent = 'Hackerbot ' + d;
      div.addEventListener('click', function () {
        selectDate(d);
      });
      sidebarEl.appendChild(div);
    });
  }

  // navigator.clipboard needs a secure context (https or localhost); this
  // page is served over plain http on a lab IP, so it's undefined there.
  // Fall back to a hidden textarea + execCommand('copy') in that case.
  function copyText(text) {
    if (navigator.clipboard && navigator.clipboard.writeText) {
      return navigator.clipboard.writeText(text);
    }
    return new Promise(function (resolve, reject) {
      var ta = document.createElement('textarea');
      ta.value = text;
      ta.style.position = 'fixed';
      ta.style.top = '-1000px';
      document.body.appendChild(ta);
      ta.focus();
      ta.select();
      try {
        document.execCommand('copy') ? resolve() : reject();
      } catch (e) {
        reject(e);
      } finally {
        document.body.removeChild(ta);
      }
    });
  }

  // Renders message text as text nodes, except for flag{...} matches, which
  // get their own span with a hover-to-copy button. Never uses innerHTML
  // with message content, since that content comes from other IRC users.
  function appendFlaggedText(el, text) {
    var parts = applyEmoticons(text).split(FLAG_RE);
    parts.forEach(function (part) {
      if (!part) {
        return;
      }
      if (FLAG_TEST_RE.test(part)) {
        var span = document.createElement('span');
        span.className = 'flagspan';

        var codeText = document.createElement('span');
        codeText.textContent = part;
        span.appendChild(codeText);

        var copyBtn = document.createElement('button');
        copyBtn.type = 'button';
        copyBtn.className = 'copybtn';
        copyBtn.title = 'Copy flag';
        span.appendChild(copyBtn);

        span.addEventListener('click', function (e) {
          e.stopPropagation();
          copyText(part).then(function () {
            copyBtn.classList.add('copied');
            setTimeout(function () {
              copyBtn.classList.remove('copied');
            }, 1200);
          });
        });

        el.appendChild(span);
      } else {
        el.appendChild(document.createTextNode(part));
      }
    });
  }

  // /clear: a blank block one window tall. Scrolled to the bottom, the
  // window shows nothing but the blank; earlier messages are still above it.
  function sizeSpacer(el) {
    el.style.height = logEl.clientHeight + 'px';
  }

  function renderClearDom() {
    var div = document.createElement('div');
    div.className = 'clearspacer';
    div.textContent = 'chat cleared';
    sizeSpacer(div);
    logEl.appendChild(div);
    logEl.scrollTop = logEl.scrollHeight;
  }

  window.addEventListener('resize', function () {
    Array.prototype.forEach.call(logEl.querySelectorAll('.clearspacer'), sizeSpacer);
    logEl.scrollTop = logEl.scrollHeight;
  });

  // Times are always rendered and hidden with a class on the log, so
  // /timestamps toggles them without re-rendering. On by default.
  function timestampsOn() {
    try {
      return localStorage.getItem(TIMESTAMPS_KEY) !== 'off';
    } catch (e) {
      return true;
    }
  }

  function applyTimestamps(on) {
    logEl.classList.toggle('hide-ts', !on);
  }

  function fmtTime(ts) {
    var d = new Date(ts);
    return ('0' + d.getHours()).slice(-2) + ':' + ('0' + d.getMinutes()).slice(-2);
  }

  function renderRecordDom(rec) {
    if (rec.dir === 'clear') {
      renderClearDom();
      return;
    }
    var p = document.createElement('p');
    p.className = rec.dir;
    if (rec.ts) {
      var t = document.createElement('span');
      t.className = 'ts';
      t.textContent = '[' + fmtTime(rec.ts) + '] ';
      p.appendChild(t);
    }
    p.appendChild(document.createTextNode('<' + rec.from + '> '));
    appendFlaggedText(p, rec.text);
    logEl.appendChild(p);
    logEl.scrollTop = logEl.scrollHeight;
  }

  function status(text) {
    var p = document.createElement('p');
    p.className = 'status';
    p.textContent = text;
    logEl.appendChild(p);
    logEl.scrollTop = logEl.scrollHeight;
  }

  // New live message: persist to today's history, then render if the
  // today entry is the one currently being viewed.
  // local: this browser saw the message itself. serve.py drops the field,
  // so records copied in from the server log never have it.
  function newRecord(dir, from, text) {
    var rec = { id: newId(), ts: Date.now(), date: TODAY, dir: dir, from: from, text: text, local: true };
    knownIds[rec.id] = true;
    chatlog[TODAY].push(rec);
    saveHistory();
    pushRecords([rec]);
    renderSidebar();
    if (activeDate === TODAY) {
      renderRecordDom(rec);
    }
  }

  function renderLog(d) {
    logEl.textContent = '';
    (chatlog[d] || []).forEach(renderRecordDom);
  }

  function selectDate(d) {
    activeDate = d;
    renderSidebar();
    renderLog(d);

    var isToday = d === TODAY;
    msgInput.disabled = !isToday;
    sendBtn.disabled = !isToday;
    cmdBtn.disabled = !isToday;
    if (isToday) {
      msgInput.focus();
    }
  }

  // Minimal IRC line parser (tags, prefix, command, params, trailing).
  function parseIrcLine(raw) {
    var s = raw;
    if (s.charAt(0) === '@') {
      var tagEnd = s.indexOf(' ');
      s = s.slice(tagEnd + 1);
    }
    var prefix = '';
    if (s.charAt(0) === ':') {
      var pEnd = s.indexOf(' ');
      prefix = s.slice(1, pEnd);
      s = s.slice(pEnd + 1);
    }
    var trailing = null;
    var trailIdx = s.indexOf(' :');
    var rest = s;
    if (trailIdx !== -1) {
      trailing = s.slice(trailIdx + 2);
      rest = s.slice(0, trailIdx);
    }
    var params = rest.split(' ').filter(function (p) { return p.length > 0; });
    var command = params.shift();
    if (trailing !== null) params.push(trailing);
    return { prefix: prefix, command: command, params: params };
  }

  function send(raw) {
    sock.send(raw);
  }

  // Flags come only from bot messages this browser received over IRC
  // (rec.local), never from records merged in from the server log: anyone
  // on the lab network can post to that log, so it could hold fake flags.
  function listFlags() {
    var seen = {};
    var found = [];
    Object.keys(chatlog).sort().forEach(function (d) {
      chatlog[d].forEach(function (rec) {
        if (!rec.local || rec.dir !== 'bot') {
          return;
        }
        (rec.text.match(FLAG_RE) || []).forEach(function (flag) {
          if (!seen[flag]) {
            seen[flag] = true;
            found.push({ date: d, flag: flag });
          }
        });
      });
    });
    if (flagMap && flagMap.length) {
      listFlagsByAttack(found);
      return;
    }
    if (!found.length) {
      status('No flags from ' + TARGET + ' in this browser\'s history yet.');
      return;
    }
    status('Flags from ' + TARGET + ' in this browser (click one to copy):');
    found.forEach(function (f) {
      flagLine('  ' + f.date + '  ', f.flag);
    });
  }

  function flagLine(prefix, flag, rest) {
    var p = document.createElement('p');
    p.className = 'status flaglist';
    p.appendChild(document.createTextNode(prefix));
    if (flag) {
      appendFlaggedText(p, flag);
    }
    if (rest) {
      var span = document.createElement('span');
      span.className = rest.cls || '';
      span.textContent = rest.text;
      p.appendChild(span);
    }
    logEl.appendChild(p);
    logEl.scrollTop = logEl.scrollHeight;
  }

  // One line per attack that awards a flag in chat: the flag if this browser
  // has it, otherwise 'not found yet'. Flags that match no attack (planted
  // elsewhere, or from an earlier build of the lab) are listed after.
  function listFlagsByAttack(found) {
    var byHash = {};
    found.forEach(function (f) {
      byHash[sha256Hex(f.flag).slice(0, FLAGMAP_HASH_LEN)] = f;
    });
    var matched = {};
    var got = 0;
    var lines = flagMap.map(function (a) {
      var flags = [];
      a.hashes.forEach(function (h) {
        if (byHash[h]) {
          flags.push(byHash[h].flag);
          matched[byHash[h].flag] = true;
        }
      });
      if (flags.length) {
        got++;
      }
      return { a: a, flags: flags };
    });
    status(TARGET + ' flags: ' + got + ' of ' + flagMap.length + ' attacks done (click a flag to copy):');
    lines.forEach(function (l) {
      var label = '  attack ' + l.a.attack + ':  ';
      if (l.flags.length) {
        l.flags.forEach(function (flag, i) {
          flagLine(i === 0 ? label : '      ', flag);
        });
      } else {
        flagLine(label, null, { cls: 'todo', text: 'not found yet' });
      }
    });
    if (got < flagMap.length) {
      status('Type "goto N" to return to attack N, or "list" to see every attack.');
    }
    var other = found.filter(function (f) { return !matched[f.flag]; });
    if (other.length) {
      status('Other flags from ' + TARGET + ' in this browser (not from a current attack):');
      other.forEach(function (f) {
        flagLine('  ' + f.date + '  ', f.flag);
      });
    }
  }

  // Returns true if text was a client command (starts with '/'), which is
  // handled here and never sent to the bot.
  function localCommand(text) {
    if (text.charAt(0) !== '/') {
      return false;
    }
    var cmd = text.trim().split(/\s+/)[0].toLowerCase();
    if (cmd === '/clear') {
      newRecord('clear', NICK, '');
    } else if (cmd === '/flags') {
      listFlags();
    } else if (cmd === '/timestamps') {
      var on = !timestampsOn();
      try {
        localStorage.setItem(TIMESTAMPS_KEY, on ? 'on' : 'off');
      } catch (e) {}
      applyTimestamps(on);
      status('Timestamps ' + (on ? 'on' : 'off') + '.');
    } else if (cmd === '/help') {
      status('Commands for this window (not sent to ' + TARGET + '):');
      Object.keys(LOCAL_COMMANDS).forEach(function (c) {
        status('  ' + c + ' : ' + LOCAL_COMMANDS[c]);
      });
    } else {
      status('Unknown command ' + cmd + '. Type /help for the commands this window understands.');
    }
    return true;
  }

  function doSend(text) {
    if (!text || activeDate !== TODAY) {
      return;
    }
    if (localCommand(text)) {
      return;
    }
    if (!registered) {
      return;
    }
    send('PRIVMSG ' + TARGET + ' :' + text);
    newRecord('me', NICK, text);
  }

  function connect() {
    status('Connecting to ' + WS_URL + ' as ' + NICK + '...');
    sock = new WebSocket(WS_URL, 'text.ircv3.net');

    sock.onopen = function () {
      send('NICK ' + NICK);
      send('USER ' + NICK + ' 0 * :' + NICK);
    };

    sock.onclose = function () {
      status('Disconnected.');
    };

    sock.onerror = function () {
      status('Connection error.');
    };

    sock.onmessage = function (ev) {
      var msg = parseIrcLine(ev.data);

      if (msg.command === 'PING') {
        send('PONG :' + msg.params[0]);
        return;
      }

      if (!registered && msg.command === '001') {
        registered = true;
        status('Connected. Starting conversation with ' + TARGET + '...');
        flagMapParts = [];
        send('PRIVMSG ' + TARGET + ' :flagmap');
        send('PRIVMSG ' + TARGET + ' :hello');
        return;
      }

      if (msg.command === 'PRIVMSG') {
        var from = msg.prefix.split('!')[0];
        var text = msg.params[msg.params.length - 1];
        var toSelf = msg.params[0].toLowerCase() === NICK.toLowerCase();
        var fromBot = from.toLowerCase() === TARGET.toLowerCase();
        if (toSelf && fromBot) {
          if (text.indexOf('FLAGMAP ') === 0) {
            handleFlagMapLine(text);
          } else {
            newRecord('bot', from, text);
          }
        }
      }
    };
  }

  document.getElementById('f').addEventListener('submit', function (e) {
    e.preventDefault();
    var text = msgInput.value;
    doSend(text);
    msgInput.value = '';
  });

  // "..." popup: quick-access hackerbot commands. goto/answer just fill the
  // box (they need a target/value filled in), the rest send immediately.
  COMMANDS.forEach(function (cmd) {
    var btn = document.createElement('button');
    btn.type = 'button';
    btn.textContent = cmd;
    btn.addEventListener('click', function () {
      cmdPopup.hidden = true;
      if (cmd.indexOf('goto') === 0 || cmd.indexOf('answer') === 0) {
        msgInput.value = cmd;
        msgInput.focus();
        msgInput.select();
      } else {
        doSend(cmd);
      }
    });
    cmdPopup.appendChild(btn);
  });

  cmdBtn.addEventListener('click', function (e) {
    e.stopPropagation();
    cmdPopup.hidden = !cmdPopup.hidden;
  });
  cmdPopup.addEventListener('click', function (e) {
    e.stopPropagation();
  });
  document.addEventListener('click', function () {
    cmdPopup.hidden = true;
  });

  applyTimestamps(timestampsOn());
  renderSidebar();
  selectDate(TODAY);
  // Merge the server's history before connecting, so the re-render it may
  // cause doesn't wipe the connection status lines; don't wait over 3s.
  var started = false;
  function start() {
    if (!started) {
      started = true;
      connect();
    }
  }
  syncHistory().then(start, start);
  setTimeout(start, 3000);
})();
