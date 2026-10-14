# Pitfalls (each one cost real time once)

Grouped by where they bite. Check the relevant group before writing or debugging the matching part.

## Bot templates (lab.xml.erb)

- **dash, not bash.** hackerbot.rb runs `pre_shell`/`post_shell` with Ruby backticks = `/bin/sh` = dash
  on the Kali hackerbot_server. `G=$((cmd ...) | x)` is read as arithmetic `$((` and fails; write
  `$( (`. macOS's sh is bash and accepts it, so offline tests on a Mac miss it. Run
  `scripts/hb_lint.rb` (uses dash) on every render.
- **`pre_shell` captures stdout only**; a syntax error or ssh failure goes to stderr, the bot shows an
  empty `FYI:` and then the else message. Append `2>&1` inside, and emit a marker (e.g. `UNREACHABLE`)
  when ssh exits non-zero so a condition can say "couldn't connect".
- **Every output runs through the conditions** - pre_shell output too. Unmatched output fires the
  `else_condition` message, so give intermediate outputs (stash/restore markers) their own condition,
  or suppress them.
- **Unanchored regexes over merged stdout+stderr.** Random hex in error paths (`.../personal_secrets/1230:
  No such file`) matched `[1-9][1-9][1-9]0` → false pass; a bare `0` condition matched any zero.
  Print one `OK|MISSING|UNEXPECTED <label>` line per checked item with stderr discarded, end with
  `RESULT PASS|FAIL`, and match labels. `[1-9]?{2}` in Ruby is "contains a 0".
- **Ship scripts base64-encoded** (`echo <b64> | base64 -d | ssh ... bash -s`) - no quoting/XML escaping
  traps; XML-escape the outer command (`s.encode(xml: :text)`). Nori isn't installed off-VM, so don't rely
  on CDATA.
- **hb_check "supplied flag never placed"** is a false positive when a flag sits inside a base64 payload;
  `hb_lint.rb` lists those flags.
- **OpenSSH 10 on the bot server** prints `** WARNING: ... post-quantum key exchange` (3 lines) when
  talking to older sshd - it lands in the student's FYI. Filter `grep -v '^\*\* '`.
- **Quiz answers**: must not be derivable from anything still on the student's VM at quiz time (file names
  count), and should be ≥ `hex(4)` - the bot allows unlimited attempts. Answers are matched as
  `/^(?:answer)$/i`.
- **Idempotent change steps**: write fixed contents *and* fixed mtimes (`touch -d @epoch`), restore the
  originals from a stash on the hackerbot_server first, remove later steps' files. Then `goto N` +
  `ready` rebuilds step N exactly (size+mtime identical, so rsync quick-checks agree).
- **`hb_check.rb` uses Ruby 3 endless defs** (`def err(msg) = ...`); on Ruby 2.7 run a patched scratch copy
  (`def err(msg); ...; end`) or fix the skill on master.

## Student commands (things the labsheet / hints must get right)

- **`~` after `=`** (`--compare-dest=~/x`) is not expanded by the local shell → full copy *for a local
  destination*. For a remote destination rsync passes the path as its own word and the remote shell
  expands it - so it "works". Verify with `rsync -avvn ... 2>&1 | grep 'opening connection'`.
- **Relative `--compare-dest`/`--link-dest`** are looked up inside the destination dir → silent full copy
  (warning `--compare-dest arg does not exist`).
- **`--fake-super` only affects the side it's given to.** Locally as root on a restore it stores
  ownership in xattrs instead of chown-ing **and turns symlinks into regular files** (~840 in /etc).
  Use `-M--fake-super` both ways.
- **`sudo` + no `user@`** → connects as root.
- **OpenSSH 9 scp** (SFTP mode): a non-existent destination ending in `/` errors
  (`realpath ... No such file` / `path canonicalization failed`); scp won't follow a symlink to a
  directory (`/usr/bin/X11 -> .` gives a harmless "failed to upload directory").
- **rsync only creates the last path component** of a destination (`mkdir ... failed: No such file`).

## Labsheet (Hacktivity site rendering)

The site renders markdown with kramdown, then a script rewrites `==type: ...==` highlights and callouts.
Things that broke on the backups sheet (check the rendered page, not just the markdown):

- **`> Hackerbot:` blocks must be short summaries, not the bot's prompt** (the
  `convert_hackerbot_to_hacktivity_lab_sheets` rule): no `==edit:==`, paths, random names or options -
  the bot gives those in chat. Copying prompts in broke all three rules below at once.
- **`==edit:==` in quote text** is only rewritten in `Note:` and `Hint:` callouts; in `Tip:`, `Warning:`,
  `Log Book Question:` and `Hackerbot:` blocks it shows raw. Inside `` `inline code` `` it always works.
- **Callouts must start with plain text.** `> Tip: **bold** ...` or `` > Hint: `cmd` `` isn't
  recognised as a callout (stays a plain quote, and loses `==edit:==` processing); nested tags
  (`` **`code`** ``) break it too. Write `> Hint: Run `man scp` ...`.
- **`--` outside code becomes an en dash** (kramdown smart typography): `-M--fake-super` in prose turned
  into `-M–fake-super`. Put options in backticks; don't use `--` as a dash (owner's style anyway).
- **No apostrophes inside `==edit:==` in code blocks** (`ssh ==edit: the server's IP==`): the
  highlighter opens a string at the `'` and the rest of the line breaks.
- Use the owner's `writing-style` skill for new prose; never reword quotes that must match a program's
  output verbatim.

## VMs / build environment

- **Build from master + the lab's files**, not from an old feature branch: older bases embed the
  Proxmox password in the generated Vagrantfile. Overlay the lab's files on a worktree of master
  (`git checkout <branch> -- <paths>`), after confirming master hasn't changed those paths.
- **`/etc/environment` keeps the build proxy** (`http_proxy=172.33.0.51:3128` on this setup) after the
  VM moves to its isolated VLAN → `curl http://hackerbot:8080/...` fails in a login shell. Use
  `curl --noproxy '*'`. (Known issue, owner's TODO.)
- **sudoers order**: some bases add `USER ALL=(ALL) ALL` after `#includedir /etc/sudoers.d`, overriding a
  NOPASSWD drop-in. Use `Defaults:USER !authenticate` (the skeleton does).
- **Guest-agent exec waits for the process**: start long jobs with `setsid nohup ... &` (all fds
  redirected) and poll; a call that times out may still have started the job - check before retrying.
- **Run the tester as the lab user** (`su - <user> -c ...`), not root - ~ / $HOME / sudo behaviour differ.
- **Students (and the owner) can't paste into VMs** - serve everything from `hackerbot:8080`; make guides
  work values out themselves (`U=$(whoami)`, `read -p ... IP`) rather than relying on page input boxes.
- **No pandoc on the dev server** - `build_dev_pages.sh` needs it; ask before installing.
- **Debian 12 = Python 3.11** (older bases older): no PEP 701 f-strings in testers.

## Git (this owner's rules)

- Ask before rebase / reset --hard / branch rename. Merge, don't rebase, when a pushed branch moved.
- `git sparse-checkout` on git 2.25 in a worktree sets `extensions.worktreeConfig` in the **shared**
  config - avoid, or undo it.
- `pkill -f <pattern>` can match your own shell command line; kill by PID.
