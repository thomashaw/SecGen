(function () {
  // NICK, TARGET, WS_URL come from config.js, loaded before this file.

  var HISTORY_KEY = 'hackerbot_webclient_history';
  // Served by serve.py next to this file: the copy of the history kept on
  // the hackerbot server. Relative, so it follows whatever host served us.
  var HISTORY_API = 'api/history';
  var COMMANDS = ['ready', 'next', 'previous', 'list', 'goto 1', 'answer yes', '/clear'];
  // Commands handled here in the browser; never sent to the bot.
  var LOCAL_COMMANDS = {
    '/clear': 'blank the chat window (scroll up to see earlier messages)',
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

  function renderRecordDom(rec) {
    if (rec.dir === 'clear') {
      renderClearDom();
      return;
    }
    var p = document.createElement('p');
    p.className = rec.dir;
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
  function newRecord(dir, from, text) {
    var rec = { id: newId(), ts: Date.now(), date: TODAY, dir: dir, from: from, text: text };
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

  // Returns true if text was a client command (starts with '/'), which is
  // handled here and never sent to the bot.
  function localCommand(text) {
    if (text.charAt(0) !== '/') {
      return false;
    }
    var cmd = text.trim().split(/\s+/)[0].toLowerCase();
    if (cmd === '/clear') {
      newRecord('clear', NICK, '');
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
        send('PRIVMSG ' + TARGET + ' :hello');
        return;
      }

      if (msg.command === 'PRIVMSG') {
        var from = msg.prefix.split('!')[0];
        var text = msg.params[msg.params.length - 1];
        var toSelf = msg.params[0].toLowerCase() === NICK.toLowerCase();
        var fromBot = from.toLowerCase() === TARGET.toLowerCase();
        if (toSelf && fromBot) {
          newRecord('bot', from, text);
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
