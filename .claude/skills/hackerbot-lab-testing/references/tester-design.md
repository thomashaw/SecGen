# Designing the tests

Worked example for everything here: branch `backups-lab`, directory `backups_lab_rework/`
(`backups_lab_test.py`, `regress.rb`, `MANUAL_TEST.md`, `VERIFY_WALKTHROUGH.md`, `reports/run*`).
Read it before writing a new lab's versions - most of it carries over.

## Contents
1. Issue IDs and the review document
2. Offline regression (`regress.rb` pattern)
3. The on-VM tester
4. Serving dev files from hackerbot:8080
5. Running it on a build and getting the report back
6. The manual test guide

## 1. Issue IDs and the review document

Give every suspected problem an ID (B1… for bugs found by review, P1… for student pitfalls, N1… for new
findings from a run) and keep them in one `ROADMAP.md` with: what, evidence, proposed fix, status. Every
test, report row and commit message refers to IDs - that's what makes runs comparable.

## 2. Offline regression (`regress.rb` pattern)

Runs in seconds, no VMs; re-run after every template change.

- Render with `hb_check.rb --scenario ... --out DIR` (stub accounts; also once with realistic
  `--input accounts=...` JSON, since stub filenames are flat).
- Parse values (users, random dir names, step marker files) back out of the rendered prompts / decoded
  scripts, so the tests track the template instead of duplicating it.
- Build fake directory trees for each scenario (correct answer + each student mistake), run the attack's
  real check script (decoded from its base64 payload, `/home/` rewritten into a temp root **without
  digits in its path**), evaluate the conditions exactly like hackerbot.rb (in order, first match,
  `/re/m`, else_condition), assert a distinctive substring of the expected message.
- Also: every pre_shell parses with dash; each attack's "VM unreachable" path (swap the ssh for
  `sh -c 'cat >/dev/null; echo "ssh: ... No route to host" >&2; exit 255'`); quiz answers accept the right
  value and nothing still on the student VM matches them.
- Prove a new check works by running it against the *old* template too - it should fail.

## 3. The on-VM tester

Start from `assets/lab_test_skeleton.py` (+ `scripts/hackerbot_irc.py` alongside). Shape of `run_all`:

- **Top to bottom through the labsheet.** Run every command it gives, with values filled in, verbatim
  (so `~`, `$HOME`, `sudo` behave as for a student). Record what the sheet *claims* (e.g. "only the new
  file is transferred") and check it.
- **For each Hackerbot attack**: `bot_goto(n)`; then each realistic mistake → `bot_ready` → `result()` on
  whether the hint names the problem (match a distinctive phrase); clean up; then the correct answer →
  expect a flag. Typical mistakes: wrong trailing slash, nested dir, missing sudo, wrong/relative
  reference path, too early / too late, skipping a prerequisite, wrong order, lost ownership.
- **Exercise the recovery paths** the hints promise (rewind with `goto N`, reset, gates refusing).
- **Never restore over live system dirs** (/etc …). Restore into a scratch copy (`cp -a /etc /tmp/x`),
  diff types/modes/owners and symlink counts against the real one, then copy back only the files the sheet
  deleted. (Run 1 of the backups lab lost ~840 /etc symlinks to a restore - inside one root shell, so it
  could at least repair modes; types it couldn't.)
- **Statuses**: OK / PROBLEM / INFO, plus CONFIRMED / NOT-REPRODUCED when testing a suspected issue,
  ERROR when the script itself broke (catch exceptions in main, keep the partial report).
- **Report**: SUMMARY first (one line per check: status, id, what was observed incl. the bot's verdict),
  then DETAILS (commands, trimmed output, bot FYI + verdict per ready) - the summary alone should be
  enough to triage. Full log separately.
- Version string in the report; bump it on every change.
- Before deploying: `py311_fstring_check.py` (on Python ≥ 3.12) or `ast.parse` on the oldest Python,
  and `tester_dry_run.py` against `fake_hackerbot.py`.

## 4. Serving dev files from hackerbot:8080

The hackerbot_server already serves `/opt/hackerbot_webclient` on :8080 (python http.server). For DEV
builds add puppet file resources there - **remove before merge**:

```puppet
# modules/utilities/unix/irc_clients/hackerbot_webclient/manifests/config.pp
  # DEV ONLY (<lab> testing): remove before merging.
  file { '/opt/hackerbot_webclient/<lab>_test.py':
    ensure  => file,
    source  => 'puppet:///modules/hackerbot_webclient/<lab>_test.py',
    owner   => 'root', group => 'root', mode => '0644',
    require => File['/opt/hackerbot_webclient'],
  }
  # ...same for hackerbot_irc.py, manual_test.html
```

and copy the files into `modules/utilities/unix/irc_clients/hackerbot_webclient/files/`. Pages are built
with `scripts/build_dev_pages.sh` (pandoc).

## 5. Running it on a build and getting the report back

```bash
D=projects/<prefix>_SecGen.../.vagrant/machines/desktop/proxmox/id        # node/vmid
S=.claude/skills/hackerbot-lab-testing/scripts
$S/guest-exec $(cat $D) 'ls /home; getent hosts hackerbot'               # agent up? which user?
$S/guest-exec $(cat $D) "su - <user> -c 'curl -sS --noproxy \"*\" -O http://hackerbot:8080/<lab>_test.py -O http://hackerbot:8080/hackerbot_irc.py && python3 <lab>_test.py --irc-check'" 90
$S/guest-exec $(cat $D) "su - <user> -c 'cd ~ && setsid nohup python3 <lab>_test.py --yes > ~/tester.out 2>&1 < /dev/null &'" 30   # may time out; it still started
$S/guest-exec $(cat $D) 'tail -3 /home/<user>/tester.out'                 # poll (background loop every 60 s)
$S/guest-exec $(cat $D) 'cat /home/<user>/<lab>_report.txt' 120 > lab_dev/reports/runN_<date>.txt
```

The main user is the one the bot's prompts mention (`--irc-check` prints it), not necessarily
`getent group sudo` (parameterised_accounts may grant sudo in /etc/sudoers directly).

## 6. The manual test guide

For the owner to judge whether hints actually help - automated checks can only match phrases.

- One section per task, in bot order; each step: the command(s) to paste, what to say to the bot, the
  exact bot reply to **Expect**, and a `- [ ] hint clear?` checkbox. Cover the same mistakes as the tester,
  plus "follow the hint exactly" steps for recovery paths.
- Setup block that works values out itself and asks only for what it can't (`U=$(whoami)`,
  `S=$(ls /home | grep -vx "$U" | grep -vx vagrant)`, `read -p "backup_server IP: " IP`), plus helper
  functions (`look() { ssh ... find ...; }`). Placeholders in page input boxes went unnoticed.
- Optional key setup to stop password prompts (`ssh-keygen` for the user first, then `ssh-copy-id`; root's
  key too if commands use sudo).
- Check it with `scripts/check_guide.rb` (bash -n on every block; expected phrases exist in the template).
- When a run shows the guide was wrong (e.g. `~` "should fail" but works remotely), fix the guide and
  the hint - the student would hit the same confusion.
