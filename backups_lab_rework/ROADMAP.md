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
  --scenario scenarios/labs/response_and_investigation/3_backups_and_recovery.xml --out /tmp/hbout   # 0 errors
ruby backups_lab_rework/hb_sim.rb --xml /tmp/hbout/bot.xml --attack N --root /tmp/hbfs             # good + bad trees
```

…then on a deployed VM for anything touching real commands (marked 🖥).

---

## Phase 0 — Verify (you)

- [ ] Work through `VERIFY_WALKTHROUGH.md`, fill in its Results table.
- [ ] Settle the Open decisions below (they change several items).

## Phase 1 — Foundation (no student-visible change yet)

- [ ] **F1. Readable check output.** One ERB helper (e.g. `check_items`) that, for a list of
      `{label, path, want: present|absent, contains:}` prints one line per item:
      `CHECK dir present` / `CHECK original files absent OK` / `CHECK 2nd changes MISSING` …, with all
      stderr sent to `/dev/null`, and a final `RESULT PASS|FAIL`. Conditions then match on keywords
      (`MISSING`, `UNEXPECTED`, `NODIR`) instead of digit strings. Fixes B3, B4, B5 for every attack at once,
      and makes the FYI line a diagnosis. (Keep `FYI` on — it's now useful.)
- [ ] **F2. Fixed per-stage file states.** Build-time constants for each stage's file contents + a fixed
      mtime per stage (e.g. `touch -d '2024-01-0N 09:00'`), so stages can be re-applied idempotently.
      Used by 3/5/7/9 below.
- [ ] **F3. Regression cases for `hb_sim.rb`.** A small script that builds fake trees for each backup
      attack (correct / full copy / wrong dir / nested `SECONDUSER/SECONDUSER` / forgot one compare-dest /
      taken too late) and asserts which condition fires. Run it after every later change.
- [ ] **F4.** Decide numbering policy: keep 13 `<attack>`s (extra steps become *stages inside* an attack,
      like `integrity_detection`), so `goto N` in the labsheet stays valid.

## Phase 2 — Top to bottom through the labsheet

### Getting started / Tips
- [ ] Labsheet: "which machine am I on?" habit (prompt shows hostname; `hostname` if unsure). 
- [ ] Labsheet: P1 — with `sudo`, always write `YOURUSER@BACKUPIP` (omitting it means `root@`). Explain
      the two password prompts (local sudo, then remote).
- [ ] Labsheet: add a **timeline figure** — epoch → changes A → diff1 → changes B → diff2 → changes C →
      incr1 → changes D → incr2 → attack → restore — showing what each backup should contain and the
      restore order. Refer back to it at each bot task.
- [ ] Labsheet: "if you get stuck: `goto N` re-applies that stage's changes" (once F2/stage items land).
- [ ] Scenario `<description>`: sync tips with labsheet, fix "it's contents".

### Copy + SSH/SCP section
- [ ] Labsheet B6: make lines 127/140/160 use one directory (`ssh_etc_backup/`), and state the scp rule
      learned in B8 (dest exists → `dest/src`; else dest *is* the copy).

### Attack 1 (scp /bin)
- [ ] B8: replace the trailing-slash hint with the scp destination-exists rule (e.g. "create the
      remote directory first: `ssh BACKUPIP mkdir -p …`").
- [ ] Decide source dir (see Open decisions); if changed, update the prompt/check (`bin/ls`, `bin/mkdir`). 🖥
- [ ] Convert check to F1 format; distinct messages for: no dir / contents copied without `bin/` / OK.

### Rsync, deltas, remote copies, `--fake-super`
- [ ] Labsheet B2: fix the `--fake-super` explanation + commands (lines 214, 234, 240, 243, 253, 295,
      321, 335, 337, 383, 399) per the Open decision. 🖥 re-run B2's R3 to confirm the final wording.

### Attack 2 (full backup)
- [ ] F1 check: dir present? nested `SECONDUSER/SECONDUSER`? several original files present (not one
      `.sample`)? Message for each; hint P3 (`sudo`) if files are missing with perms errors.
- [ ] Fix else-message path (`remote-rsync-backup` → `remote-rsync-full-backup`).
- [ ] Labsheet: give the command *shape* with the trailing-slash rule spelled out for this exact path.

### Differential backups section
- [ ] Labsheet B1: replace `~` with `$HOME` in all `--compare-dest=` (lines 292, 318); add a one-line
      "why `~` doesn't work after `=`" note + the rsync warning text from B1
      (confirmed on VM: `--compare-dest arg does not exist: ~/b1/full`; `~` gave a full copy, `$HOME` gave 1 file).
- [ ] Labsheet P2: the `--compare-dest` dir must have the *same layout* as the destination
      (`.../full-backup/` holds `SECONDUSER/…`), and it fails silently otherwise.

### Attack 3 (changes A, hidden flag)
- [ ] F2: write stage-A state exactly (and remove stage B/C/D files) so re-running = rewind to "just
      before diff1". Fixed mtimes. 🖥
- [ ] Message: "now take differential1 *before* saying ready again" (sequencing cue).

### Attack 4 (differential1)
- [ ] F1 check + specific hints: everything copied → compare-dest path/layout (P2); changes A missing →
      backup taken before attack 3 ("say `goto 3`"); nested dir; no dir.

### Incremental backups section
- [ ] Labsheet B1: `$HOME` in lines 380, 396.

### Attack 5 (changes B)
- [ ] F2: stage-B state, `>` not `>>` (write full intended `notes`/`log2` content); remove C/D files. 🖥

### Attack 6 (differential2)
- [ ] B6: prompt path → `.../remote-rsync-differential2/SECONDUSER/`.
- [ ] F1 check + hints: changes A missing → "a differential compares against the *full* backup, not
      diff1"; changes C present → "taken too late — `goto 5`".

### Attack 7 (changes C)
- [ ] F2: stage-C state; remove D files. 🖥

### Attack 8 (incremental1)
- [ ] F1 check + hints: 2nd changes present → "add `--compare-dest` for differential2"; changes D present →
      "taken too late — `goto 7`, redo, then continue".

### Snapshot section
- [ ] Labsheet B1: `$HOME` in `--link-dest` (lines 463, 479); snapshot_2 should link against
      snapshot_1 (the previous snapshot), not the full backup; add a `find -links +1` / `du` check so
      students *see* the hard links (confirmed on VM: `~` → 0 links, 8M = full copy; `$HOME` → ~1000 links,
      1.4M — mention that directories can't be hard-linked, which is why it isn't ~0).

### Attack 9 (changes D)
- [ ] F2: stage-D state. 🖥

### Attack 10 (incremental2 + quiz)
- [ ] Prompt: state the base ("based on the full, differential2 and incremental1"), as attack 8 does.
- [ ] F1 check + hints (as attack 8, one level deeper); B5's "taken too late" case → `goto 7`.
- [ ] Quiz: ask something only the backups can answer (e.g. "what did `notes` say in incremental1?" →
      `Buy eggs <rand2>`), with `<suppress_command_output_feedback/>`.

### Attack 11 (the deletion)
- [ ] Safety gate: run the attack-2/4/6/8/10 checks first; if any fail, **refuse** ("your backups
      wouldn't survive this — fix incremental1 first") and stay on attack 11.
- [ ] Stash: before `rm`, tar `/home/SECONDUSER` to the hackerbot_server (not student-reachable). 🖥
- [ ] Message: what was deleted + "restore order: full → diff2 → incr1 → incr2".

### Attack 12 (full restore)
- [ ] F1 check per item (original files, A, B, C, D, latest `notes`), so the student sees which layer
      is missing; "Close — check the order" when only `notes` is stale.
- [ ] Staged reset: on a failed check, say "say `ready` again to have me wipe and re-delete so you can
      retry"; the next `ready` restores the stash state then re-deletes. (Pattern: `integrity_detection`.) 🖥
- [ ] (If B2 confirmed) optionally check `SECONDUSER` owns their files, with a hint about `-M--fake-super`.
- [ ] Labsheet: restore commands' trailing-slash rule (`.../SECONDUSER/ → /home/SECONDUSER/`), and the
      nested-dir failure mode.

### Attack 13 (earliest `notes`)
- [ ] B7: exact-content check (`[ "$(cat notes)" = "<stage A notes>" ]`), not substring grep.
- [ ] Reword: "Restore `notes` to the **first version that was backed up**" (+ "it wasn't in the full
      backup — which backup first captured it?"). Fix "it's".

## Phase 3 — Wrap-up
- [ ] `hb_check` 0 errors / 0 warnings; F3 regression cases pass.
- [ ] Full deployed play-through: happy path, plus at least: nested dir on 2, full copy on 4, late incr1
      on 8, failed restore + reset on 12. 🖥
- [ ] Update labsheet hints/notes to match final bot messages; renumber nothing (F4).
- [ ] Decide fate of `backups_lab_rework/` (delete, or move `hb_sim.rb` into the hackerbot skill's `scripts/`).
- [ ] **Remove the DEV verify page before merge**: the `verify_walkthrough.html` file resource in
      `modules/utilities/unix/irc_clients/hackerbot_webclient/manifests/config.pp` and
      `modules/utilities/unix/irc_clients/hackerbot_webclient/files/verify_walkthrough.html`.

---

## Open decisions

1. **`--fake-super` in the lab** (after B2): (a) teach `-M--fake-super` both ways (correct, one more
   concept), (b) drop ownership preservation and explain it as a known limitation, or (c) `--rsync-path="rsync --fake-super"`.
   Recommendation: (a), with a sentence on *why* it's needed.
2. **Attack 1 source** (after B8 timing): keep `/bin` (big, symlinked) or switch to something small,
   e.g. `/etc/ssh/` or a planted directory.
3. **Stash location** for attack 11: hackerbot_server (recommended — students have sudo on desktop and
   their own account on backup_server).
4. ~~**Labsheet location**~~ — resolved: `backups_lab.md` is a copy of the remote labsheet repo's file,
   committed on this branch and edited here. Thomas syncs it back to the labsheet repo at the end.
