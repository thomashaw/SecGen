# Backups lab: manual test of the dependent challenges (2–13)

A guided play-through of Hackerbot tasks 2–13 that makes the mistakes students make — wrong paths, wrong
`--compare-dest`, backups taken too early/late, skipping ahead, restoring in the wrong order — so you can judge
whether each **hint actually gets a student unstuck**. Each step: run the command(s), say what's shown to
Hackerbot, check the reply against **Expect**, then tick **hint clear?** if a student could act on it.

Use a **fresh build**. Run everything on the **desktop**, in one terminal (the variables below stay set). Fill
in the boxes at the top of the page first — the commands use your values.

> Every bot reply should start with `FYI:` lines (what it checked: `OK …`, `MISSING …`, `UNEXPECTED …`) and then a
> one-line verdict. The FYI should make the verdict obvious.

## 0. Setup

```bash
U=YOURUSER; S=SECONDUSER; IP=BACKUPIP; M=-M--fake-super
B=/home/$U          # where backups live on the backup_server
look() { ssh $U@$IP "cd $B && find remote-rsync-* -maxdepth 3 | sort"; }   # what's on the server
```

Optional — stop typing passwords (only for this test; students won't do this):

```bash
ssh-copy-id $U@$IP
sudo bash -c '[ -f /root/.ssh/id_ed25519 ] || ssh-keygen -q -t ed25519 -N "" -f /root/.ssh/id_ed25519'
sudo ssh-copy-id -i /root/.ssh/id_ed25519.pub $U@$IP
```

In the Hackerbot chat say `hello`, then `goto 2`.

## 1. Full backup (task 2)

- [ ] **1a — trailing slash on the source** (copies the contents, not the directory)

  ```bash
  sudo rsync -avzh $M /home/$S/ $U@$IP:$B/remote-rsync-full-backup/
  ```

  Say `ready`. **Expect:** "You copied the *contents* of /home/S straight into remote-rsync-full-backup/…"
  - [ ] hint clear?

  ```bash
  ssh $U@$IP rm -rf remote-rsync-full-backup
  ```

- [ ] **1b — no sudo** (S's files are 0600)

  ```bash
  rsync -avzh $M /home/$S $U@$IP:$B/remote-rsync-full-backup/
  ```

  Rsync should report `Permission denied` lines. Say `ready`. **Expect:** "Some of S's files aren't in your backup.
  Did you run rsync with sudo?…"
  - [ ] hint clear?

  ```bash
  ssh $U@$IP rm -rf remote-rsync-full-backup
  ```

- [ ] **1c — destination names S too** (parent dir doesn't exist yet)

  ```bash
  sudo rsync -avzh $M /home/$S $U@$IP:$B/remote-rsync-full-backup/$S
  ```

  Rsync fails: `mkdir "…/remote-rsync-full-backup/S" failed: No such file or directory`. Say `ready`.
  **Expect:** "I can't find …/remote-rsync-full-backup/S … (If rsync said 'mkdir … failed' … rsync only creates
  the last directory …)"
  - [ ] hint clear?

- [ ] **1d — nested** (student "fixes" 1c by creating the directory)

  ```bash
  ssh $U@$IP mkdir -p remote-rsync-full-backup/$S
  sudo rsync -avzh $M /home/$S $U@$IP:$B/remote-rsync-full-backup/$S
  ```

  Say `ready`. **Expect:** "Your backup is nested one level too deep … Give rsync the *parent* directory…"
  - [ ] hint clear?

  ```bash
  ssh $U@$IP rm -rf remote-rsync-full-backup
  ```

- [ ] **1e — correct**

  ```bash
  sudo rsync -avzh $M /home/$S $U@$IP:$B/remote-rsync-full-backup/
  ```

  Say `ready`. **Expect:** flag, and the bot moves on to task 3.

## 2. Skipping step 3, then differential1 (tasks 3–4)

- [ ] **2a — skip the change step** (back up before S has changed anything). The bot is on task 3; say `goto 4`.

  ```bash
  sudo rsync -avzh $M /home/$S --compare-dest=$B/remote-rsync-full-backup/ $U@$IP:$B/remote-rsync-differential1/
  ```

  Say `ready`. **Expect:** "Your differential doesn't contain the changes S made at step 3 … Say 'goto 3' then
  'ready'…"
  - [ ] hint clear?

  ```bash
  ssh $U@$IP rm -rf remote-rsync-differential1
  ```

  Say `goto 3`, then `ready`. **Expect:** "Ok, S has made their changes… (Hint: Keep an eye out for a flag...)".
  The hidden flag: `sudo cat /home/$S/personal_secrets/flag`

- [ ] **2b — `~` in `--compare-dest`** (the shell doesn't expand it; on the server it's a literal `~` dir)

  ```bash
  sudo rsync -avzh $M /home/$S --compare-dest=~/remote-rsync-full-backup/ $U@$IP:$B/remote-rsync-differential1/
  ```

  Note rsync's `--compare-dest arg does not exist` warning in the output. Say `ready`. **Expect:** "Your
  differential backup also contains files that haven't changed since the full backup … absolute path ($HOME works,
  ~ does not)…"
  - [ ] hint clear?

  ```bash
  ssh $U@$IP rm -rf remote-rsync-differential1
  ```

- [ ] **2c — `--compare-dest` one level too deep** (no warning at all from rsync)

  ```bash
  sudo rsync -avzh $M /home/$S --compare-dest=$B/remote-rsync-full-backup/$S/ $U@$IP:$B/remote-rsync-differential1/
  ```

  Say `ready`. **Expect:** the same "also contains files that haven't changed … the directory that *contains* S/"
  message.
  - [ ] hint clear? (does it make the layout problem obvious enough?)

  ```bash
  ssh $U@$IP rm -rf remote-rsync-differential1
  ```

- [ ] **2d — correct**

  ```bash
  sudo rsync -avzh $M /home/$S --compare-dest=$B/remote-rsync-full-backup/ $U@$IP:$B/remote-rsync-differential1/
  ```

  Say `ready`. **Expect:** flag; moves to task 5.

## 3. Taking differential1 too late, and rewinding (tasks 4–5)

- [ ] **3a** — say `ready` (task 5: S makes more changes). Now redo differential1, i.e. *too late*:

  ```bash
  ssh $U@$IP rm -rf remote-rsync-differential1
  sudo rsync -avzh $M /home/$S --compare-dest=$B/remote-rsync-full-backup/ $U@$IP:$B/remote-rsync-differential1/
  ```

  Say `goto 4`, then `ready`. **Expect:** "Your backup contains step 5's changes -- it was taken too late … Say
  'goto 3' then 'ready' … delete remote-rsync-differential1, back up again, then carry on from step 5."
  - [ ] hint clear?

- [ ] **3b — follow that hint exactly**: say `goto 3`, `ready` (check `sudo ls /home/$S/personal_secrets/`:
  `really_not_a_flag` should be **gone**), then:

  ```bash
  ssh $U@$IP rm -rf remote-rsync-differential1
  sudo rsync -avzh $M /home/$S --compare-dest=$B/remote-rsync-full-backup/ $U@$IP:$B/remote-rsync-differential1/
  ```

  Say `goto 4`, `ready` → flag. Then `ready` again on task 5 (`really_not_a_flag` is back).
  - [ ] the rewind made sense?

## 4. Differential2 (task 6)

- [ ] **4a — incremental instead of differential** (also compared against differential1)

  ```bash
  sudo rsync -avzh $M /home/$S --compare-dest=$B/remote-rsync-full-backup/ --compare-dest=$B/remote-rsync-differential1/ $U@$IP:$B/remote-rsync-differential2/
  ```

  Say `ready`. **Expect:** "A differential backup holds *all* the changes since the full backup -- including step
  3's. Compare against the full backup only (not differential1)…"
  - [ ] hint clear?

  ```bash
  ssh $U@$IP rm -rf remote-rsync-differential2
  ```

- [ ] **4b — correct**

  ```bash
  sudo rsync -avzh $M /home/$S --compare-dest=$B/remote-rsync-full-backup/ $U@$IP:$B/remote-rsync-differential2/
  ```

  Say `ready` → flag. Then `ready` on task 7 (S's changes).

## 5. Skipping differential2, forgetting a `--compare-dest` (task 8)

- [ ] **5a — skip ahead: differential2 missing**

  ```bash
  ssh $U@$IP mv remote-rsync-differential2 diff2-aside
  ```

  Say `ready` (on task 8). **Expect:** "This incremental only holds the changes since your last backup,
  differential2 -- but differential2 … isn't on the backup_server. Say 'goto 5' then 'ready' … take differential2
  (task 6), then 'goto 7', 'ready', and take this incremental."
  - [ ] hint clear?

  ```bash
  ssh $U@$IP mv diff2-aside remote-rsync-differential2
  ```

- [ ] **5b — forgot differential2's `--compare-dest`**

  ```bash
  sudo rsync -avzh $M /home/$S --compare-dest=$B/remote-rsync-full-backup/ $U@$IP:$B/remote-rsync-incremental1/
  ```

  Say `ready`. **Expect:** "Your incremental backup contains changes that are already in differential2 … add a
  second --compare-dest=…/remote-rsync-differential2/…"
  - [ ] hint clear?

  ```bash
  ssh $U@$IP rm -rf remote-rsync-incremental1
  ```

- [ ] **5c — correct**

  ```bash
  sudo rsync -avzh $M /home/$S --compare-dest=$B/remote-rsync-full-backup/ --compare-dest=$B/remote-rsync-differential2/ $U@$IP:$B/remote-rsync-incremental1/
  ```

  Say `ready` → flag. Then `ready` on task 9.

## 6. Redoing incremental1 after step 9 — the classic dead end (task 10)

- [ ] **6a** — redo incremental1 *now* (after step 9), then take incremental2:

  ```bash
  ssh $U@$IP rm -rf remote-rsync-incremental1
  sudo rsync -avzh $M /home/$S --compare-dest=$B/remote-rsync-full-backup/ --compare-dest=$B/remote-rsync-differential2/ $U@$IP:$B/remote-rsync-incremental1/
  sudo rsync -avzh $M /home/$S --compare-dest=$B/remote-rsync-full-backup/ --compare-dest=$B/remote-rsync-differential2/ --compare-dest=$B/remote-rsync-incremental1/ $U@$IP:$B/remote-rsync-incremental2/
  look
  ```

  incremental2 has no files. Say `ready` (task 10). **Expect:** "Your backup doesn't contain step 9's changes.
  Either you backed up before saying 'ready' at step 9 …, or your incremental1 already contains them because it
  was redone after step 9 … 'goto 7', 'ready', redo incremental1, then 'goto 9', 'ready', and redo incremental2."
  - [ ] hint clear? (would a student work out which of the two cases applies?)

- [ ] **6b — follow the hint**: say `goto 7`, `ready`, then

  ```bash
  ssh $U@$IP rm -rf remote-rsync-incremental1 remote-rsync-incremental2
  sudo rsync -avzh $M /home/$S --compare-dest=$B/remote-rsync-full-backup/ --compare-dest=$B/remote-rsync-differential2/ $U@$IP:$B/remote-rsync-incremental1/
  ```

  Say `goto 8`, `ready` → flag. Say `ready` (task 9), then

  ```bash
  sudo rsync -avzh $M /home/$S --compare-dest=$B/remote-rsync-full-backup/ --compare-dest=$B/remote-rsync-differential2/ --compare-dest=$B/remote-rsync-incremental1/ $U@$IP:$B/remote-rsync-incremental2/
  ```

  Say `ready` (task 10) → flag and the quiz question.
  - [ ] the recovery path made sense?

- [ ] **6c — quiz**: answer with the *desktop's* notes first (`sudo cat /home/$S/notes`, then `answer <that>`).
  **Expect:** "Incorrect". Then `ssh $U@$IP cat remote-rsync-incremental1/$S/notes` and answer with that →
  "Correct" + flag.

## 7. The attack's safety gate (task 11)

- [ ] **7a — a backup is missing**

  ```bash
  ssh $U@$IP mv remote-rsync-differential1 diff1-aside
  ```

  Say `ready`. **Expect:** FYI lines per backup, incl. `[differential1] NODIR …`; "Not yet! I only attack
  systems whose backups would survive it…". Check `sudo ls /home/$S` — files still there.
  - [ ] hint clear? (is it obvious *which* backup is the problem?)

  ```bash
  ssh $U@$IP mv diff1-aside remote-rsync-differential1
  ```

- [ ] **7b** — say `ready`. **Expect:** "I just deleted all S's files!" and task 12.

## 8. Restore (task 12)

- [ ] **8a — nothing restored yet**: say `ready`. **Expect:** "S's original files aren't back yet: start by
  restoring the full backup. If your files are in a mess, say 'goto 11'…"
  - [ ] hint clear?

- [ ] **8b — no trailing slash on the source** (nests the directory)

  ```bash
  sudo rsync -av $M $U@$IP:$B/remote-rsync-full-backup/$S /home/$S/
  ```

  Say `ready`. **Expect:** "You restored into /home/S/S/. Put a trailing slash on the source…"
  - [ ] hint clear?

- [ ] **8c — reset**: say `goto 11`, `ready` (the gate passes, files deleted again). `sudo ls -A /home/$S` →
  only `.ssh`, `.vim`… Say `goto 12`.
  - [ ] reset made sense?

- [ ] **8d — wrong order** (incremental2 before incremental1)

  ```bash
  for b in full-backup differential2 incremental2 incremental1; do sudo rsync -av $M $U@$IP:$B/remote-rsync-$b/$S/ /home/$S/; done
  ```

  Say `ready`. **Expect:** "Close... … Check the order you did your restore commands in: notes isn't the latest
  version."
  - [ ] hint clear?

- [ ] **8e — right order but without `-M--fake-super`**: say `goto 11`, `ready`, `goto 12`, then

  ```bash
  for b in full-backup differential2 incremental1 incremental2; do sudo rsync -av $U@$IP:$B/remote-rsync-$b/$S/ /home/$S/; done
  ls -l /home/$S
  ```

  Files are owned by you, not S. Say `ready`. **Expect:** "All the files are back, but some aren't owned by S any
  more … Back up and restore with -M--fake-super … or … sudo chown -R S: /home/S"
  - [ ] hint clear?

- [ ] **8f — correct**: say `goto 11`, `ready`, `goto 12`, then

  ```bash
  for b in full-backup differential2 incremental1 incremental2; do sudo rsync -av $M $U@$IP:$B/remote-rsync-$b/$S/ /home/$S/; done
  ls -l /home/$S
  ```

  Say `ready` → flag; files owned by S.

## 9. Earliest notes (task 13)

- [ ] **9a — the wrong "earliest"**: restore notes from differential2

  ```bash
  sudo rsync -av $M $U@$IP:$B/remote-rsync-differential2/$S/notes /home/$S/notes
  ```

  Say `ready`. **Expect:** "Close: that's the version from differential2 (step 5). notes wasn't in the full backup
  -- which backup captured it first?"
  - [ ] hint clear?

- [ ] **9b — from the full backup** (a student might try this first)

  ```bash
  sudo rsync -av $M $U@$IP:$B/remote-rsync-full-backup/$S/notes /home/$S/notes
  ```

  rsync errors (no such file) — that's the clue. 
  - [ ] is rsync's error + the task wording enough of a hint?

- [ ] **9c — correct**: from differential1

  ```bash
  sudo rsync -av $M $U@$IP:$B/remote-rsync-differential1/$S/notes /home/$S/notes
  ```

  Say `ready` → flag.

## Notes for Claude

For each unticked "hint clear?", note what the bot said and what a student would still be confused about.
