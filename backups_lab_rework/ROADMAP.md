# Backups lab rework: roadmap

Files in play:

- Bot: `modules/generators/structured_content/hackerbot_config/backups/templates/lab.xml.erb` (+ `secgen_local/local.rb`)
- Scenario: `scenarios/labs/response_and_investigation/3_backups_and_recovery.xml`
- Labsheet: `backups_lab.md` (repo root of this branch; a copy of the remote labsheet repo's file)
- Test helper: `backups_lab_rework/hb_sim.rb` runs on your **laptop**, not on any VM (needs only Ruby +
  `nokogiri`, same as `hb_check.rb`). It reads a rendered `bot.xml` and replays the bot's condition matching.
- DEV verify page: `backups_lab_rework/build_verify_page.sh` renders `VERIFY_WALKTHROUGH.md` (pandoc) into the
  hackerbot web client → `http://hackerbot:8080/verify_walkthrough.html`. Re-run it after editing the walkthrough.

Issue IDs (B1–B8, P1–P3) refer to `VERIFY_WALKTHROUGH.md`. **Phase 0 gates everything**: drop or
reshape any item whose issue doesn't reproduce.

Per-change test loop (after every item):

```bash
S=.claude/skills/secgen-hackerbot/scripts/hb_check.rb
ruby $S modules/generators/structured_content/hackerbot_config/backups \
  --scenario scenarios/labs/response_and_investigation/3_backups_and_recovery.xml --accounts 2 --out /tmp/hbout   # 0 errors
ruby backups_lab_rework/regress.rb /tmp/hbout/bot.xml                                              # 38 offline cases
```

…then on a deployed VM for anything touching real commands (marked 🖥).

---

## Phase 0 — Verify (you)

- [x] Automated run 1 (2026-10-07, `reports/run1_2026-10-07.txt`): 8 min, all 13 attacks solved, 10/10 flags.
      **Confirmed:** B1 (compare-dest and link-dest), B2 (and `-M--fake-super` round-trips owner+mode exactly),
      B3 (real, at attack 12: `<rand2>`=`064f`), B4, B5, B6 (prompt + labsheet), B7, B8, P1, P2, P3, quiz
      answerable from the desktop. **New:** N1–N7 below.
- [x] Review the new findings + proposals, and settle the Open decisions below (they change several items).

### New findings from run 1

- **N1 (severe). Restoring all of `/etc` the way the sheet does (lines 335/337, 408) damages the student VM.**
  `sudo rsync -avz --fake-super USER@BACKUP:…/etc/ /etc/` changed ~850 entries: every **symlink in /etc became
  a regular file** (mode 777, ~840 of them — e.g. `/etc/alsa/conf.d/*`, and by implication `/etc/alternatives/*`,
  `localtime`, `os-release`…), and every `/etc/sudoers.d/*` got **bad permissions** (`visudo`: "should be mode
  0440"). The local-copy restore (no `--fake-super`) did not do this. Cause: as root, `--fake-super` stores
  ownership in xattrs instead of chown-ing, and Linux can't put user xattrs on symlinks, so rsync stores them as
  plain files. Likely a real source of "my VM broke during the backups lab".
  *(The test VM used for run 1 has a damaged /etc now — don't reuse it.)*
- **N2. The scp section fails as written.** Line 127 `sudo scp -pr /etc/ …:ssh_etc_backup` exits **rc=1 with ~20
  error lines** (symlinks to /dev/null, dangling links: "not a regular file", "No such file") even though most of
  /etc copies — students will think they did it wrong. Line 140 `sudo scp -pr /etc/ …:ssh_backup/` **fails
  completely** on OpenSSH 9.2 (scp now uses SFTP): `realpath …/ssh_backup/: No such file` — a trailing slash on a
  destination that doesn't exist is an error. So lines 140–160 (`ls -la ssh_backup/`) can't work.
- **N3. B3 is common, not 1%:** at attack 12 any `0` in the 4-hex `<rand2>` filename matches the catch-all `0`
  condition ⇒ ~23% of builds tell a student with nothing restored "You restored something".
- **N4. `ls /home` shows `vagrant`** (base-box account) as well as YOURUSER + SECONDUSER — the 3rd mythical
  account is gone (P4 fix works), but students still see two "other" users.
- **N5. Non-sudo rsync of SECONDUSER's home partly fails** (rc=23: `.ssh`, `.vim` are 0700) — a student who drops
  `sudo` gets a backup that's *mostly* there, then confusing results later.
- **N6. Attack 2's most natural mistake errors out**: `rsync /home/S U@IP:…/remote-rsync-full-backup/S` →
  `rsync: mkdir "…/remote-rsync-full-backup/S" failed: No such file or directory` (rsync only creates the last
  path component). Worth one line in the sheet: what that error means.
- **N7. Attack 1's two failure modes get the same message** ("…trailing / changes…"), whether nothing was
  copied or the contents were copied without `bin/`; with scp the real rule is "does the destination exist".
- `/bin` = `/usr/bin`: 1824 files, 153M; the scp took **8s** (236M on the server) — size is not a problem.
- Restore ownership (B2 impact): after the labsheet-style attack-12 restore, 15 entries are root-owned and
  **SECONDUSER can't write their own `notes`**. `-M--fake-super` (R3) restored `640 1002:1002` exactly.

## Implementation status (2026-10-07)

Phases 1 and 2 are implemented (bot template, labsheet, scenario description), except **N1** (Open decision 5:
what to back up/restore instead of live `/etc`). How it was done, where it differs from the plan below:

- **Every attack is one `pre_shell` on the hackerbot_server with `<get_shell>false</get_shell>`**; the check/stage
  scripts are built in Ruby (`check_fn`, `stage_script` in `lab.xml.erb`) and shipped base64-encoded to the desktop
  or backup_server as `bash -s` (no quoting/XML-escaping traps). Checks print `OK/MISSING/UNEXPECTED/NODIR <label>`
  + `RESULT PASS|FAIL` with stderr discarded (fixes B3/B4/B5/N3); conditions match labels like
  `changes-from-step-7`.
- **Stash = SECONDUSER's original files**, taken on the hackerbot_server (`/root/.hb_backups_lab/`) the first time a
  change step (3) runs, and put back every time a change step runs. So `goto 3/5/7/9` + `ready` rebuilds the exact
  state of that step at any time — even after attack 11's deletion — rather than a stash only for attack 11.
- **Attack 11** only deletes when full/differential2/incremental1/incremental2 all pass (else `HOLD-OFF` + the FAIL
  lines). **Reset for attack 12** = `goto 11` + `ready` (deletes again; the gate still applies).
- **Attack 12 also checks ownership** (`find ! -user SECONDUSER`), with a `-M--fake-super` / `chown` hint.
- `hb_check`: 0 errors, 1 warning ("1 supplied flag never placed") — **false positive**: the hidden flag is inside the
  base64 step-3 script. `backups_lab_rework/regress.rb`: 38/38 offline cases pass (stub and real-shaped accounts).
- Tester v2 (`backups_lab_test.py` 2026-10-07.2) follows the new sheet and also exercises rewind (`goto 7`), the
  gate refusing, the `goto 11` reset, the ownership hint, and tests the sheet's /etc restores into a **scratch copy**.

## Phase 1 — Foundation (no student-visible change yet)

- [x] **F1. Readable check output.** One ERB helper (e.g. `check_items`) that, for a list of
      `{label, path, want: present|absent, contains:}` prints one line per item:
      `CHECK dir present` / `CHECK original files absent OK` / `CHECK 2nd changes MISSING` …, with all
      stderr sent to `/dev/null`, and a final `RESULT PASS|FAIL`. Conditions then match on keywords
      (`MISSING`, `UNEXPECTED`, `NODIR`) instead of digit strings. Fixes B3, B4, B5 for every attack at once,
      and makes the FYI line a diagnosis. (Keep `FYI` on — it's now useful.)
- [x] **F2. Fixed per-stage file states.** Build-time constants for each stage's file contents + a fixed
      mtime per stage (e.g. `touch -d '2024-01-0N 09:00'`), so stages can be re-applied idempotently.
      Used by 3/5/7/9 below.
- [x] **F3. Regression cases for `hb_sim.rb`.** A small script that builds fake trees for each backup
      attack (correct / full copy / wrong dir / nested `SECONDUSER/SECONDUSER` / forgot one compare-dest /
      taken too late) and asserts which condition fires. Run it after every later change.
- [x] **F4.** Decide numbering policy: keep 13 `<attack>`s (extra steps become *stages inside* an attack,
      like `integrity_detection`), so `goto N` in the labsheet stays valid.

## Phase 2 — Top to bottom through the labsheet

### Getting started / Tips
- [x] Labsheet: "which machine am I on?" habit (prompt shows hostname; `hostname` if unsure). 
- [x] Labsheet: P1 — with `sudo`, always write `YOURUSER@BACKUPIP` (omitting it means `root@`). Explain
      the two password prompts (local sudo, then remote).
- [x] Labsheet: add a **timeline figure** — epoch → changes A → diff1 → changes B → diff2 → changes C →
      incr1 → changes D → incr2 → attack → restore — showing what each backup should contain and the
      restore order. Refer back to it at each bot task.
- [x] Labsheet: "if you get stuck: `goto N` re-applies that stage's changes" (once F2/stage items land).
- [x] Scenario `<description>`: sync tips with labsheet, fix "it's contents".
- [x] P4: removed the unused 3rd account (accounts[2]) from the scenario; comment now documents [0]/[1].
      Confirmed on run 1 — but `vagrant` (base box) still appears (N4).
- [x] N4: labsheet line 59–65: "`ls /home` lists you, `vagrant` (ignore it — it's part of the VM image) and the
      user you'll back up". (Removing `vagrant` from the base is out of scope.)

### Copy + SSH/SCP section
- [x] Labsheet B6 + N2: rewrite lines 124–160. Proposal: scp a **symlink-free** directory so it runs cleanly
      (e.g. `/etc/ssh/` or `/usr/share/doc/rsync/` — 🖥 pick one with no links), into one destination name used
      consistently; run it twice to show scp re-sends everything; state the scp rule from B8 (dest exists →
      `dest/src`; else dest *is* the copy; never end a not-yet-existing dest with `/` — OpenSSH 9 errors).
      Optionally keep a short note: "scp can't copy special files/symlinks to /dev/null — rsync can".

### Attack 1 (scp /bin)
- [x] B8: replace the trailing-slash hint with the scp destination-exists rule (e.g. "create the
      remote directory first: `ssh BACKUPIP mkdir -p …`").
- [x] Source dir: keep `/bin` (run 1: 153M, 8s).
- [x] N7: convert check to F1 format; distinct messages for: no dir / contents copied without `bin/` / OK.

### Rsync, deltas, remote copies, `--fake-super`
- [x] Labsheet B2: fix the `--fake-super` explanation + commands (lines 214, 234, 240, 243, 253, 295,
      321, 335, 337, 383, 399) per Open decision 1.
- [ ] **N1: stop restoring live `/etc` wholesale** (lines 335/337 and 408). Proposal (Open decision 5): keep the
      /etc *backups* as they are, but have students restore into a scratch dir and copy back only what was lost,
      e.g. `sudo rsync -a -M--fake-super USER@BACKUP:…/remote-rsync-backup/etc/ ~/restore-test/etc/` then
      `sudo cp -a ~/restore-test/etc/wgetrc /etc/` — teaches the same restore order without risking the VM.
      🖥 tester v2 must check what `-M--fake-super` does to symlinks on a round trip (the backup_server side can't
      xattr symlinks either) before we commit to the wording.

### Attack 2 (full backup)
- [x] F1 check: dir present? nested `SECONDUSER/SECONDUSER`? several original files present (not one
      `.sample`)? Message for each; hint P3 (`sudo`) if files are missing with perms errors.
- [x] Fix else-message path (`remote-rsync-backup` → `remote-rsync-full-backup`).
- [x] Labsheet: give the command *shape* with the trailing-slash rule spelled out for this exact path, plus
      N6 (what `rsync: mkdir … failed: No such file or directory` means) and N5 (why `sudo`: `.ssh`/`.vim` are 0700).

### Differential backups section
- [x] Labsheet B1: replace `~` with `$HOME` in all `--compare-dest=` (lines 292, 318); add a one-line
      "why `~` doesn't work after `=`" note + the rsync warning text from B1
      (confirmed on VM: `--compare-dest arg does not exist: ~/b1/full`; `~` gave a full copy, `$HOME` gave 1 file).
- [x] Labsheet P2: the `--compare-dest` dir must have the *same layout* as the destination
      (`.../full-backup/` holds `SECONDUSER/…`), and it fails silently otherwise.

### Attack 3 (changes A, hidden flag)
- [x] F2: write stage-A state exactly (and remove stage B/C/D files) so re-running = rewind to "just
      before diff1". Fixed mtimes. 🖥
- [x] Message: "now take differential1 *before* saying ready again" (sequencing cue).

### Attack 4 (differential1)
- [x] F1 check + specific hints: everything copied → compare-dest path/layout (P2); changes A missing →
      backup taken before attack 3 ("say `goto 3`"); nested dir; no dir.

### Incremental backups section
- [x] Labsheet B1: `$HOME` in lines 380, 396.

### Attack 5 (changes B)
- [x] F2: stage-B state, `>` not `>>` (write full intended `notes`/`log2` content); remove C/D files. 🖥

### Attack 6 (differential2)
- [x] B6: prompt path → `.../remote-rsync-differential2/SECONDUSER/`.
- [x] F1 check + hints: changes A missing → "a differential compares against the *full* backup, not
      diff1"; changes C present → "taken too late — `goto 5`".

### Attack 7 (changes C)
- [x] F2: stage-C state; remove D files. 🖥

### Attack 8 (incremental1)
- [x] F1 check + hints: 2nd changes present → "add `--compare-dest` for differential2"; changes D present →
      "taken too late — `goto 7`, redo, then continue".

### Snapshot section
- [x] Labsheet B1: `$HOME` in `--link-dest` (lines 463, 479); snapshot_2 should link against
      snapshot_1 (the previous snapshot), not the full backup; add a `find -links +1` / `du` check so
      students *see* the hard links (confirmed on VM: `~` → 0 links, 8M = full copy; `$HOME` → ~1000 links,
      1.4M — mention that directories can't be hard-linked, which is why it isn't ~0).

### Attack 9 (changes D)
- [x] F2: stage-D state. 🖥

### Attack 10 (incremental2 + quiz)
- [x] Prompt: state the base ("based on the full, differential2 and incremental1"), as attack 8 does.
- [x] F1 check + hints (as attack 8, one level deeper); B5's "taken too late" case → `goto 7`.
- [x] Quiz: ask something only the backups can answer (e.g. "what did `notes` say in incremental1?" →
      `Buy eggs <rand2>`), with `<suppress_command_output_feedback/>`.

### Attack 11 (the deletion)
- [x] Safety gate: run the attack-2/4/6/8/10 checks first; if any fail, **refuse** ("your backups
      wouldn't survive this — fix incremental1 first") and stay on attack 11.
- [x] Stash: before `rm`, tar `/home/SECONDUSER` to the hackerbot_server (not student-reachable). 🖥
- [x] Message: what was deleted + "restore order: full → diff2 → incr1 → incr2".

### Attack 12 (full restore)
- [x] F1 check per item (original files, A, B, C, D, latest `notes`), so the student sees which layer
      is missing; "Close — check the order" when only `notes` is stale (run 1: the "Close" hint works well — keep
      its wording). Fixes N3 (~23% of builds currently say "restored something" when nothing was restored).
- [x] Staged reset: on a failed check, say "say `ready` again to have me wipe and re-delete so you can
      retry"; the next `ready` restores the stash state then re-deletes. (Pattern: `integrity_detection`.) 🖥
- [x] (If B2 confirmed) optionally check `SECONDUSER` owns their files, with a hint about `-M--fake-super`.
- [x] Labsheet: restore commands' trailing-slash rule (`.../SECONDUSER/ → /home/SECONDUSER/`), and the
      nested-dir failure mode.

### Attack 13 (earliest `notes`)
- [x] B7: exact-content check (`[ "$(cat notes)" = "<stage A notes>" ]`), not substring grep.
- [x] Reword: "Restore `notes` to the **first version that was backed up**" (+ "it wasn't in the full
      backup — which backup first captured it?"). Fix "it's".

## Phase 3 — Wrap-up
- [x] `hb_check` 0 errors / 0 warnings; F3 regression cases pass.
- [ ] Full deployed play-through: happy path, plus at least: nested dir on 2, full copy on 4, late incr1
      on 8, failed restore + reset on 12. 🖥
- [x] Update labsheet hints/notes to match final bot messages; renumber nothing (F4).
- [ ] Decide fate of `backups_lab_rework/` (delete, or move `hb_sim.rb` into the hackerbot skill's `scripts/`).
- [ ] **Remove the DEV verify page + tester before merge**: the `verify_walkthrough.html` and
      `backups_lab_test.py` file resources in
      `modules/utilities/unix/irc_clients/hackerbot_webclient/manifests/config.pp`, and both files in
      `modules/utilities/unix/irc_clients/hackerbot_webclient/files/`.
- [x] After the fixes: update `backups_lab_rework/backups_lab_test.py` to the new lab (new commands, new bot
      messages, stage/reset behaviour), re-run it on a fresh build, and compare reports. Tester v2 also needs:
      run the *old* /etc-restore check into a scratch copy instead of live /etc (run 1's guard couldn't undo
      symlink→file conversion); a `-M--fake-super` /etc round trip counting symlinks (N1); `2>/dev/null` on the
      P1 `ssh -G` probe.

---

## Open decisions

1. ~~**`--fake-super` in the lab**~~ — resolved: `-M--fake-super` for every backup and restore (implemented).
2. ~~**Attack 1 source**~~ — resolved: `/usr/bin` (the real directory; `/bin` is a symlink to it). 8s on run 1.
3. ~~**Stash location**~~ — resolved: hackerbot_server (`/root/.hb_backups_lab/`), holding SECONDUSER's
   original files; used by every change step (rewind) — see Implementation status.
4. ~~**Labsheet location**~~ — resolved: `backups_lab.md` is a copy of the remote labsheet repo's file,
   committed on this branch and edited here. Thomas syncs it back to the labsheet repo at the end.
5. **Whole-/etc restores (N1)** — *pending*. The sheet now uses `-M--fake-super` here too (which should round-trip
   symlinks: run 2's `N1-*` rows check that against a scratch copy of /etc). Options: (a) keep backing up /etc but
   restore into a scratch dir and copy back only the lost files, (b) keep restoring live /etc with `-M--fake-super`
   (only if run 2 is clean), or (c) **recommended:** practise on a copy, `sudo mkdir -p /srv/practice && sudo cp -a
   /etc /srv/practice/`, and use `/srv/practice/etc` wherever the sheet says `/etc` (backups still contain an `etc/`
   directory, so every other path in the sheet stays the same) — realistic root-owned config with symlinks and
   private files, but a mistake can't break the VM.
