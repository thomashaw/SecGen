# Backups lab: verification walkthrough (before fixing anything)

Goal: confirm, by hand, each issue raised in the review, so we only fix what is real.
Each check has an ID that the roadmap (`ROADMAP.md`) refers to. Tick the box and jot what you saw
in the **Results** table at the bottom; anything that surprises you changes the fix.

> **On the VMs (dev builds only):** this page is served at **http://hackerbot:8080/verify_walkthrough.html**
> (open it in Firefox on the desktop). Fill in the boxes at the top and every command is rewritten with your
> values; each code block has a **Copy** button. Rebuild the page after editing this file:
> `backups_lab_rework/build_verify_page.sh` (needs `pandoc`), then rebuild the VMs.

Placeholders used below — substitute your build's values:

| Placeholder | Meaning | How to find it |
|---|---|---|
| `YOURUSER` | the first (sudo) account, logged in on desktop | `whoami` on desktop |
| `SECONDUSER` | the second account whose files the bot backs up | `ls /home` on desktop |
| `BACKUPIP` | backup_server IP | from the claim page, or `getent hosts` / the bot's prompt |
| `ROOTPW` | root password on all VMs | the scenario's `spoiler_admin_pass` |

Time needed: ~20 min for the offline checks (B3/B4/B5), ~45–60 min for the VM checks, plus a
normal play-through of the bot if you do section 4.

---

## 0. Setup

- [ ] Build and start the lab (desktop, backup_server, hackerbot_server).
- [ ] On **hackerbot_server** as root, keep a copy of the rendered bot config. It contains the random
      markers, which are handy for knowing what the bot expects:

  ```bash
  cat /opt/hackerbot/config/bot_0.xml        # copy it to your laptop as bot_0.xml for the offline checks
  grep -o 'remote-bin-backup-[0-9a-f]*' /opt/hackerbot/config/bot_0.xml | head -1
  ```

- [ ] An xattr viewer for B2. `getfattr` (package `attr`) may not be installed; this one-liner works
      wherever `python3` is present:

  ```bash
  xa() { for p in "$@"; do python3 -c 'import os,sys; p=sys.argv[1]; print(p, {k: os.getxattr(p,k) for k in os.listxattr(p)})' "$p"; done; }
  ```

  Define it in each shell you use (desktop and an `ssh BACKUPIP` session). Use `sudo` + a root shell
  (`sudo -i`, then define `xa`) when the files aren't yours.

---

## 1. Offline checks (no VMs needed) — B3, B4, B5

These use `backups_lab_rework/hb_sim.rb`, which replays `<condition>` matching exactly as
`hackerbot.rb#check_output_conditions` does (unanchored `=~ /re/m`, in order, first match wins).
Run it **on your laptop** from the repo root (branch `backups-lab-rework`); nothing
goes on the VMs. It needs Ruby + `nokogiri` (already required by `hb_check.rb`).

```bash
# from the repo root of this branch
ruby .claude/skills/secgen-hackerbot/scripts/hb_check.rb \
  modules/generators/structured_content/hackerbot_config/backups \
  --scenario scenarios/labs/response_and_investigation/3_backups_and_recovery.xml --out /tmp/hbout
X=/tmp/hbout/bot.xml        # or the bot_0.xml you copied off the server
ruby backups_lab_rework/hb_sim.rb --xml $X --list
```

### B3 — digits in stderr paths cause false passes / wrong messages

Claim: post_command stderr (`grep: …/personal_secrets/<rand2>: No such file…`) is matched too. `<rand2>`
is 4 random hex chars, so when it happens to be e.g. `1230` the success regex of attack 8
(`[1-9][1-9][1-9]0`) matches the **path**, and a backup that is missing the file is passed (~1% of builds).

- [ ] Run:

  ```bash
  ruby backups_lab_rework/hb_sim.rb --xml $X --attack 8 \
    --output 'grep: /home/u/remote-rsync-incremental1/s/personal_secrets/1230: No such file or directory\n1112'
  ```

  Bug is real if: `MATCHED condition 1 … Well done! flag{…}`. (Status `1112` = the new file is missing, which should fail.)
  *Already seen during review: it does award the flag.*

- [ ] Same idea on attack 12 ("didn't restore anything" reported as "restored something"):

  ```bash
  ruby backups_lab_rework/hb_sim.rb --xml $X --attack 12 \
    --output 'grep: /home/s/personal_secrets/0e40: No such file or directory\n222222'
  ```

  Bug is real if: it matches the `0` condition ("You restored something, but not everything") instead of
  `[1-9]{6}` ("You didn't restore anything").

### B4 — `0[1-9]?{2}` matches any output containing a 0

Claim: in Ruby this is `0([1-9]?){2}` and unanchored, so it is just "contains a 0". All non-success,
non-"wrong dir" outcomes on attacks 8 and 10 get the same "wasn't an incremental backup, delete it" text.

- [ ] Run:

  ```bash
  ruby -e 'r=/0[1-9]?{2}/; %w[1101 1110 1011 0111 0000].each{|s| puts "#{s} #{!!(s=~r)}"}'
  ruby backups_lab_rework/hb_sim.rb --xml $X --attack 8 --output '1101'
  ```

  Bug is real if: every value prints `true` and attack 8 says "wasn't an incremental backup".
  `1101` actually means "the `really_not_a_flag` (2nd change set) is in your incremental", i.e. **you forgot
  `--compare-dest` for differential2**, which is a much more useful thing to tell the student.

### B5 — "You didn't backup to the specified remote directory" when the directory *does* exist

Claim: "wrong dir" is inferred from all statuses being non-zero, not from the directory's existence. The
realistic trap: student redoes incremental1 **after** attack 9 ran → incr1 now contains `nothing_much` →
their incr2 (compare-dest incl. incr1) is empty → bot says "wrong directory" forever.

- [ ] Simulate it with a fake tree where incr2 exists but is empty. **Use a `--root` path with no
      digits in it**, or the digits leak into stderr and confuse the regexes (that's B3 again):

  ```bash
  M=$(grep -o '/home/[a-z_]*/remote-rsync-incremental2/[a-z_]*' $X | head -1)
  rm -rf /tmp/hbfs && mkdir -p /tmp/hbfs$M/personal_secrets
  ruby backups_lab_rework/hb_sim.rb --xml $X --attack 10 --root /tmp/hbfs
  ```

  Bug is real if: `MATCHED condition 2 … You didn't backup to the specified remote directory.`
  *Already seen during review: it does.*

- [ ] (Optional, on the VMs, during section 4) after the bot has run **attack 9**, re-run your incremental1
      rsync command, then delete and redo incremental2 and say `ready`. Expect the same wrong message.

---

## 2. Desktop-only checks — B1, P1, P3

### B1 — `~` after `--compare-dest=` / `--link-dest=` is not expanded

Claim: bash only tilde-expands after `=` in real variable assignments (`NAME=~/x`), not in
`--opt=~/x`. rsync receives a literal `~/backups/...`, treats it as relative to the destination, can't find it,
and makes a **full copy**. Affects labsheet lines 292, 318, 380, 396 (`--compare-dest`) and 463, 479
(`--link-dest`). The remote variants use absolute paths and are fine.

- [ ] See the literal tilde:

  ```bash
  echo --compare-dest=~/backups/rsync_backup/
  ```

  Bug is real if: it prints `--compare-dest=~/backups/rsync_backup/` (not `/home/YOURUSER/...`).

- [ ] Reproduce the effect on a differential:

  ```bash
  sudo rm -rf ~/b1 && mkdir -p ~/b1        # start clean: leftover diff_* dirs from an earlier run skew the counts
  sudo rsync -a /etc ~/b1/full/
  sudo find ~/b1/full/etc -type f | wc -l  # sanity: ~1000; if this errors, stop and fix first
  sudo bash -c 'echo b1 > /etc/b1test'

  sudo rsync -av /etc --compare-dest=~/b1/full/ ~/b1/diff_tilde/ 2>&1 | grep -v '^etc/'
  sudo find ~/b1/diff_tilde -type f | wc -l

  sudo rsync -av /etc --compare-dest=$HOME/b1/full/ ~/b1/diff_home/ 2>&1 | grep -v '^etc/'
  sudo find ~/b1/diff_home -type f | wc -l
  ```

  (`grep -v '^etc/'` hides the per-file list so any warning line stays on screen.)

  Bug is real if: `diff_tilde` has ~1000 files (all of /etc) and rsync printed a warning about the
  compare-dest path (note the exact wording; it can go in the labsheet), while `diff_home` has **1** file
  (`etc/b1test`). If **both** are ~1000 even from a clean start, my explanation is wrong — run
  `sudo rsync -avin /etc --compare-dest=$HOME/b1/full/ ~/b1/x/ | head` (`-i` itemises *why* each file
  would be sent, `-n` = dry run) and send me the output.

- [ ] Same for `--link-dest` (snapshot section):

  ```bash
  sudo rsync -a --link-dest=~/b1/full/      /etc ~/b1/snap_tilde
  sudo rsync -a --link-dest=$HOME/b1/full/  /etc ~/b1/snap_home
  echo "tilde: $(sudo find ~/b1/snap_tilde -type f -links +1 | wc -l) hard-linked files"
  echo "home:  $(sudo find ~/b1/snap_home  -type f -links +1 | wc -l) hard-linked files"
  sudo du -sh ~/b1/full ~/b1/snap_tilde ~/b1/snap_home    # du counts each inode once, in arg order
  ```

  Bug is real if: `snap_tilde` has ~0 hard-linked files and takes as much space as `full`; `snap_home`
  is almost all hard links and has a tiny `du`.

- [ ] Clean up: `sudo rm -rf ~/b1 /etc/b1test`

### P1 — `sudo` + omitting `user@` means `root@`

Claim: the labsheet (line 147) says you can omit the username; with `sudo` that silently becomes `root`,
whose password students don't have.

- [ ] Run `sudo ssh BACKUPIP` (and `sudo rsync -av /etc/hostname BACKUPIP:`). Ctrl-C at the prompt.

  Confirmed if: the prompt is `root@BACKUPIP's password:`. (Also notice: the first prompt is your
  *local* sudo password, the second is the *remote* one — note how confusing that sequence looks.)

### P3 — what a non-sudo rsync of SECONDUSER's home does

Informs whether hints should say "use sudo".

- [ ] Run:

  ```bash
  ls -ld /home/SECONDUSER; ls -lR /home/SECONDUSER | head -30
  rsync -av /home/SECONDUSER /tmp/p3/ 2>&1 | tail -5; rm -rf /tmp/p3
  ```

  Record: home dir mode (e.g. `drwx------` or `drwxr-xr-x`), whether rsync reports `Permission denied`
  / `some files/attrs were not transferred (code 23)`.

---

## 3. Desktop + backup_server checks — B2, B8, B6 (labsheet part), P2

### B2 — `--fake-super` only affects the side it's given on

Claim (rsync man page: "This option only affects the side where the option is used. To affect the remote
side of a remote-shell connection, use the --remote-option (-M) option"):

- Backup `sudo rsync --fake-super /src user@host:dst` → fake-super is on the *local sender*. The non-root
  remote receiver can't chown, and stores nothing → **ownership/mode lost on the server**.
- Restore `sudo rsync --fake-super user@host:src /dst` → fake-super is on the *local root receiver*, which
  then **does not chown**; it writes the ownership into xattrs instead → restored files owned by root.
- Correct: back up **and** restore with `-M--fake-super` (or `--rsync-path="rsync --fake-super"`) and
  no local `--fake-super`.

> **What `-M` is.** An rsync-over-SSH transfer runs *two* rsync processes: yours, and one that rsync starts
> on the other machine via ssh. Normal options configure both where it makes sense, but `--fake-super`
> deliberately applies only to the process it's given to. `-M OPTION` (long form `--remote-option=OPTION`)
> passes `OPTION` to the **remote** rsync only. So `-M--fake-super` means "the backup_server's rsync, which runs
> as YOURUSER and can't chown, should record owner/group/mode in `user.rsync.%stat` xattrs when receiving,
> and read them back when sending". The local side runs as root via `sudo`, so it can chown for real and
> needs no `--fake-super`. Equivalent older spelling: `--rsync-path="rsync --fake-super"`.

Prepare a file owned by SECONDUSER with a distinctive mode, on the **desktop**:

```bash
sudo install -d -o SECONDUSER -g SECONDUSER -m 750 /tmp/b2src
sudo -u SECONDUSER bash -c 'echo hi > /tmp/b2src/f && chmod 640 /tmp/b2src/f'
sudo ls -ln /tmp/b2src            # note SECONDUSER's uid, and mode -rw-r-----
```

- [ ] **Backup A — labsheet style**:

  ```bash
  sudo rsync -av --fake-super /tmp/b2src YOURUSER@BACKUPIP:/home/YOURUSER/b2_labsheet/
  ```

  On **backup_server** (`ssh BACKUPIP`): `ls -ln ~/b2_labsheet/b2src; xa ~/b2_labsheet/b2src/f`

  Bug is real if: owner is YOURUSER's uid and there is **no** `user.rsync.%stat` xattr.

- [ ] **Backup B — `-M` style**:

  ```bash
  sudo rsync -av -M--fake-super /tmp/b2src YOURUSER@BACKUPIP:/home/YOURUSER/b2_remote/
  ```

  On **backup_server**: `ls -ln ~/b2_remote/b2src; xa ~/b2_remote/b2src/f`

  Expected: owner still YOURUSER (the server can't chown), but `user.rsync.%stat` = something like
  `100640 0,0 <SECONDUSER uid>:<gid>`. (If the xattr is missing, check the server fs supports user xattrs.)

- [ ] **Restores** on the **desktop** (each into a fresh dir), then `sudo ls -ln` each, and `sudo -i` + `xa` on
      the `f` files:

  ```bash
  # R1: labsheet-style restore of labsheet-style backup
  sudo rsync -av --fake-super YOURUSER@BACKUPIP:/home/YOURUSER/b2_labsheet/b2src/ /tmp/b2_r1/
  # R2: labsheet-style restore of the -M backup
  sudo rsync -av --fake-super YOURUSER@BACKUPIP:/home/YOURUSER/b2_remote/b2src/   /tmp/b2_r2/
  # R3: -M restore of the -M backup (the proposed correct pair)
  sudo rsync -av -M--fake-super YOURUSER@BACKUPIP:/home/YOURUSER/b2_remote/b2src/ /tmp/b2_r3/
  # R4: plain restore of labsheet-style backup (shows the info was never stored)
  sudo rsync -av YOURUSER@BACKUPIP:/home/YOURUSER/b2_labsheet/b2src/              /tmp/b2_r4/
  sudo ls -ln /tmp/b2_r1 /tmp/b2_r2 /tmp/b2_r3 /tmp/b2_r4
  ```

  Bug is real if roughly:

  | Restore | Expected owner of `f` | Expected mode |
  |---|---|---|
  | R1 | root (0) — with a local `user.rsync.%stat` xattr | probably not 640 |
  | R2 | root (0) | ? |
  | R3 | **SECONDUSER** | **640** ✔ |
  | R4 | YOURUSER | whatever the server had |

  If R3 is correct and R1/R2 are not, the labsheet's `--fake-super` commands (lines 214, 234, 240, 253,
  295, 321, 335, 337, 383, 399) need changing. Note anything different — especially if R1 is fine, then I'm
  wrong about the receiver side.

- [ ] Clean up: `sudo rm -rf /tmp/b2src /tmp/b2_r*` and on the server `rm -rf ~/b2_*`.

### B8 — scp: destination existence matters, trailing slash doesn't; `/bin` is big

Claim: attack 1's hint ("the trailing / changes whether you are copying directories or their contents") is
rsync lore. With scp, `scp -r SRC host:DEST` gives `DEST/SRC` if DEST exists and `DEST` = copy of SRC
otherwise. Also `/bin` → `/usr/bin` on Debian 12 and may be large.

- [ ] Size:

  ```bash
  readlink -f /bin; ls /bin | wc -l; du -shL /bin/
  ```

- [ ] Semantics (on the desktop):

  ```bash
  mkdir -p /tmp/b8/d && echo x > /tmp/b8/d/f
  ssh BACKUPIP 'mkdir -p b8_exists b8_exists_s'
  scp -r /tmp/b8/d  BACKUPIP:/home/YOURUSER/b8_new          # dest missing, no slash
  scp -r /tmp/b8/d  BACKUPIP:/home/YOURUSER/b8_new_slash/   # dest missing, dest slash
  scp -r /tmp/b8/d/ BACKUPIP:/home/YOURUSER/b8_new_srcslash # dest missing, src slash
  scp -r /tmp/b8/d  BACKUPIP:/home/YOURUSER/b8_exists/      # dest exists
  scp -r /tmp/b8/d/ BACKUPIP:/home/YOURUSER/b8_exists_s/    # dest exists, src slash
  ssh BACKUPIP 'find b8_* | sort'
  ```

  Bug is real if: `b8_new/f` and `b8_new_srcslash/f` (no `d/`), but `b8_exists/d/f` and `b8_exists_s/d/f`.
  Record what `b8_new_slash` does (OpenSSH 9.x in SFTP mode may differ from old scp — it may error).

- [ ] (Optional) `time scp -rq /bin BACKUPIP:/home/YOURUSER/b8_bin/` — how long does attack 1 take a student?
- [ ] Clean up: `rm -rf /tmp/b8; ssh BACKUPIP 'rm -rf b8_*'`

### B6 (labsheet part) — scp section writes to two different directories

Claim: line 127 scps to `ssh_etc_backup`, line 140 "repeats" into `ssh_backup/`, line 160 lists `ssh_backup/`.

- [ ] Follow labsheet lines 124–160 exactly as written, then on the server `ls -la ~/ssh_etc_backup ~/ssh_backup | head`.

  Bug is real if: the two dirs differ in layout (one holds `/etc`'s contents directly, the other `etc/`,
  or the second command errors), and the "re-copies everything" point can't be seen.

### P2 — mismatched `--compare-dest` layout is silent

Informs the bot's hint for attacks 4/6/8/10.

- [ ] Run:

  ```bash
  sudo rsync -a /home/SECONDUSER/ YOURUSER@BACKUPIP:/home/YOURUSER/p2_full/SECONDUSER/     # trailing-slash full
  sudo rsync -av /home/SECONDUSER --compare-dest=/home/YOURUSER/p2_full/SECONDUSER/ \
       YOURUSER@BACKUPIP:/home/YOURUSER/p2_diff/ 2>&1 | tail -3                              # mismatched diff
  ssh BACKUPIP 'find p2_diff -type f | wc -l; rm -rf p2_*'
  ```

  Expected: no warning at all (the compare-dest dir exists, just with the wrong layout), and **every** file
  copied. If so, the bot hint for "everything was copied" should say "your --compare-dest must contain
  `SECONDUSER/` at the same relative path as your destination".

---

## 4. Play-through with the bot — B6 (bot part), B7, ownership after restore

Do the bot challenges as a student would, noting each FYI output (it is what the new readable checks will
replace). Some checks below need you to deliberately do it *wrong* first; use `goto N` to retry.

- [ ] **Attack 6 path (B6)**: follow the prompt *literally* — it says `.../remote-rsync-differential2/.`
      with no `SECONDUSER/`:

  ```bash
  sudo rsync -av /home/SECONDUSER/ --compare-dest=/home/YOURUSER/remote-rsync-full-backup/SECONDUSER/ \
       YOURUSER@BACKUPIP:/home/YOURUSER/remote-rsync-differential2/
  ```

  `ready` → Bug is real if: "You didn't backup to the specified remote directory." Then
  `ssh BACKUPIP rm -rf remote-rsync-differential2` and redo it in the `/home/SECONDUSER` (no slash) form.

- [ ] **Attack 2 else-message**: read-only check — `lab.xml.erb:158` says `remote-rsync-backup/` vs the
      task's `remote-rsync-full-backup/`. (Hard to trigger; just confirm by reading.)

- [ ] **After attack 12 (ownership, ties to B2)**: on the desktop

  ```bash
  sudo ls -lnR /home/SECONDUSER | head -20
  sudo -u SECONDUSER touch /home/SECONDUSER/notes && echo "SECONDUSER can write" || echo "SECONDUSER CANNOT write"
  ```

  Record the owner uid you got with whatever restore commands you used.

- [ ] **Attack 13 (B7)**: show that the diff2 copy also passes, and that the epoch has no `notes`:

  ```bash
  ssh BACKUPIP 'ls remote-rsync-full-backup/SECONDUSER/notes; \
                echo --- diff1; cat remote-rsync-differential1/SECONDUSER/notes; \
                echo --- diff2; cat remote-rsync-differential2/SECONDUSER/notes'
  sudo rsync -av YOURUSER@BACKUPIP:/home/YOURUSER/remote-rsync-differential2/SECONDUSER/notes /home/SECONDUSER/notes
  ```

  `ready` → Bug is real if: the full backup has no `notes`, diff2's notes has **two** lines (with the
  attack-3 marker on line 1), and the bot still awards the flag.

- [ ] **Attack 10 quiz is answerable without backups**: `sudo cat /home/SECONDUSER/personal_secrets/nothing_much`
      on the desktop, answer with that → expect the flag.

---

## Results

| ID | What | Confirmed? | Notes / exact messages |
|---|---|---|---|
| B1 | `~` not expanded in `--compare-dest=`/`--link-dest=` | ✅ both (VM) | compare-dest: full=1000, `~` diff=1001 (full copy), `$HOME` diff=1. rsync: `--compare-dest arg does not exist: ~/b1/full`. link-dest: `~` snapshot 0 hard links, du 8M (= full); `$HOME` snapshot ~1000 hard links, du 1.4M (dirs can't be hard-linked) |
| B2 | `--fake-super` wrong side; restores root-owned | | |
| B3 | stderr digits → false pass (attack 8) / wrong msg (12) | ✅ (sim) | |
| B4 | `0[1-9]?{2}` = "contains 0" | ✅ (sim) | |
| B5 | "wrong directory" when dir exists | ✅ (sim) | |
| B6 | path/wording mismatches (attack 6 prompt, attack 2 msg, labsheet ssh_backup) | | |
| B7 | attack 13 accepts diff2 notes; epoch has no notes | | |
| B8 | scp dest-existence semantics; `/bin` size | | |
| P1 | `sudo` + no `user@` = root | ✅ (VM) | `sudo ssh BACKUPIP` prompts `root@…'s password:`; students can't log in. (rsync form: confirm which command "worked") |
| P2 | mismatched compare-dest is silent full copy | | |
| P3 | non-sudo rsync of SECONDUSER home | | |
| P4 | `ls /home` shows 2 other users (scenario's unused 3rd account); sheet says "a second user" | ✅ (code) | record what `ls /home` lists |
| — | ownership after attack 12 restore | | |
