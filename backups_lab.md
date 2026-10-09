---
title: "Backing Up and Recovering from Disaster: SSH/SCP, Deltas, and Rsync"
author: ["Z. Cliffe Schreuders"]
license: "CC BY-SA 4.0"
description: "Learn to back up and restore data using scp, and full, differential, and incremental rsync backups, while Hackerbot puts your backups to the test."
overview: |
  This lab focuses on the critical aspect of contingency planning in the context of cyber security and data management. It highlights the importance of maintaining data availability and system reliability, especially when dealing with potential disasters and security incidents. The lab covers practical strategies for creating reliable backups, understanding recovery procedures, and implementing backup solutions using the powerful rsync command.

  In this lab, you will learn how to use SSH/SCP for secure file transfer, create full, differential, and incremental backups using the rsync tool, and use backups for efficient data protection. You will also explore the concept of snapshot backups, which allow for efficient storage of data by using hard links to unchanged files. Throughout the lab, you will engage in hands-on tasks such as copying directories, performing backups, restoring files, and setting up remote backups.

  This is a Hackerbot lab. Hackerbot will task you with performing backups and will attack your system; you must have the right backups in place to recover.
tags: ["backups", "rsync", "scp", "ssh", "disaster-recovery", "differential-backup", "incremental-backup", "hackerbot"]
categories: ["response_and_investigation"]
type: ["ctf-lab", "hackerbot-lab", "lab-sheet"]
difficulty: "intermediate"
cybok:
  - ka: "SOIM"
    topic: "Execute: Mitigation and Countermeasures"
    keywords: ["Recover data and services after an incident", "BACKUP - DIFFERENTIAL", "BACKUP - INCREMENTAL"]
---

## Getting started {#getting-started}

### Tips for completing this lab {#tips-for-completing-this-lab}

This lab **needs to be completed in order**. Between your backups, Hackerbot has the second user change their files, so each backup needs to be taken at the right moment in that sequence.

> Tip: You should use the rsync dry run option (by adding `-n` to the command) to test which files are going to be backed up, without making the changes.

You should manually check you have done your backups correctly **before** telling Hackerbot you are ready. It may be a good idea to SSH to your backup server in a separate console tab (but do keep an eye on which system you are running each command on; your prompt shows the hostname, or you can run `hostname` if unsure!):

```bash
ssh ==edit: YOURUSERNAME==@==edit: BACKUPSERVERIP==
```

> Note: Hackerbot's FYI output shows what it has checked: one line for each thing it looked for (`OK ...`, `MISSING ...` or `UNEXPECTED ...`), followed by `RESULT PASS` or `RESULT FAIL`. The `MISSING` and `UNEXPECTED` lines describe what is wrong with your backup, so read these first.

> Tip: If a backup goes wrong because the files have already moved on, **you don't need to start the lab again**. The steps where the second user changes their files (Hackerbot attacks 3, 5, 7 and 9) can be repeated: say `goto 3` (or 5, 7 or 9) followed by `ready`, and Hackerbot returns their files to exactly the state they were in at that step. You can then delete the bad backup on the backup_server, take it again, and carry on.

> Warning: When you use `sudo` with `ssh`, `scp` or `rsync`, **always** write your username and `@` before the backup_server's IP address (`==edit: YOURUSERNAME==@==edit: BACKUPSERVERIP==`). Since `sudo` makes you root locally, leaving out the username means the connection is attempted as `root`, and you don't have the backup_server's root password. You will also be asked for two passwords: first your local password (for `sudo`), and then your password on the backup_server.

### VMs in this lab {#vms-in-this-lab}

\==action: Start these VMs== (if you haven't already):

- hackerbot_server (leave it running, you don't log into this)
- backup_server (==edit: its IP address, given to you when you claimed the VMs==)
- desktop

All of these VMs need to be running to complete the lab.

### Your login details for the "desktop" and "backup_server" VMs {#your-login-details-for-the-desktop-and-backup_server-vms}

\==VM: On the desktop and backup_server VMs==, log in using:

- User: ==edit: your username, given to you when you claimed the VMs==
- Password: `tiaspbiqe2r` (**t**his **i**s **a** **s**ecure **p**assword **b**ut **i**s **q**uite **e**asy **2** **r**emember)

You won't log in to the hackerbot_server, but the VM needs to be running to complete the lab.

You don't need to log in to the backup_server directly, but you will connect to it via SSH later in the lab.

There is also a second user account on the desktop VM. ==action: List the users on the system==, to find the second user's username:

```bash
ls /home
```

You will see your own username, `vagrant` (an account that is part of the VM image, which you can ignore), and the second user. You'll use this second username (==edit: SECONDUSER== in the commands below) later in the lab, when Hackerbot asks you to back up their files; Hackerbot also tells you their name when it reaches that task.

{% include hackerbot-intro.md role="task you to perform backups and will attack your system" chat="hello" %}

## Availability and recovery {#availability-and-recovery}

As you will recall, availability is a common security goal. This includes data availability, and systems and services availability. Preparing for when things go wrong, and having procedures in place to respond and recover is a task known as contingency planning. This includes business continuity planning (BCP), which has a wide scope covering many kinds of problems, disaster recovery planning, which aims to recover ICT after a major disaster, and incident response (IR) planning, which aims to detect and respond to security incidents.

Business impact analysis involves determining which business processes are mission critical, and what the recovery requirements are. This includes Recovery Point Objectives (RPO), that is, which data and services are acceptable to lose and how often backups are necessary, and Recovery Time Objectives (RTO), which is how long it should take to recover, and the amount of downtime that is allowed for.

Having reliable backups and redundancy that can be used to recover data and/or services is a basic security maintenance requirement.

## Uptime {#uptime}

At a console, ==action: run:==

```bash
uptime
```

```
15:32:38 up 4 days, 23:50,  4 users,  load average: 1.01, 1.24, 1.17
```

A common goal is to aim for "five nines" availability (99.999%). If you only have one server, that means keeping it running constantly, other than for scheduled maintenance.

> Log Book Question: List a few legitimate security reasons for performing off-line maintenance.

## Copy {#copy}

The simplest of Unix copy commands, is `cp`. `cp` takes a local source and destination, and can recursively copy contents from one file or directory to another.

Make a directory to store your backups. ==action: Run:==

```bash
mkdir ~/backups/
```

\==action: Make a backup copy of your /etc/passwd file:==

```bash
cp /etc/passwd ~/backups/
```

We have made a backup of a source file (/etc/passwd), to our destination directory (`~/backups/`). Note that we lost the metadata associated with the file, including file ownership and permissions:

```bash
ls -la ~/backups/passwd
ls -la /etc/passwd
```

Note and take the time to ==action: understand the differences in the output== from these two commands. Notably the backup file is now owned by you (and also belongs to your primary group).

## SSH (secure shell) and SCP (secure copy) {#ssh-secure-shell-and-scp-secure-copy}

Using SSH (secure shell), `scp` (secure copy) can transfer files securely (encrypted) over a network.

> Note: This replaces the old insecure rcp command, which sends files over the network in the clear (not encrypted). Rcp should never be used.

In this section we will back up your SSH configuration directory, `/etc/ssh/`, to the backup_server. First, ==action: create a directory on the backup_server to hold the backup== (as shown below, `ssh` can also run a single command on the remote computer):

```bash
ssh ==edit: YOURUSERNAME==@==edit: BACKUPSERVERIP== mkdir -p scp_backup
```

> Tip: If this is the first time you have connected to the backup_server, you will be asked to confirm the host's fingerprint ("yes"); you will then be asked for your password on the backup_server, which is the same as on the desktop.

\==action: Back up /etc/ssh to the backup_server== using `scp`:

```bash
sudo scp -pr /etc/ssh ==edit: YOURUSERNAME==@==edit: BACKUPSERVERIP==:/home/==edit: YOURUSERNAME==/scp_backup/
```

> Tip: You will be prompted for your local password (`sudo` is required because some of these files are only readable by root), followed by the remote password.

Read the scp man page to ==action: determine what the `-p` and `-r` flags do==.

> Hint: Run `man scp`, and press "q" to quit.

> Note: Where scp puts a directory depends on whether the destination already exists. If it does (as `scp_backup` does), scp copies the directory *into* it, resulting in `scp_backup/ssh/`. If, however, the destination does not exist, scp creates it *as* the copy, so the contents of `ssh` would end up directly in the new directory. Furthermore, with current versions of OpenSSH, a destination ending in `/` that does not yet exist is treated as an error, which is why we created `scp_backup` first.

Now, let's change a file in /etc/ssh, and repeat the backup:

```bash
sudo bash -c 'echo "# backup test" >> /etc/ssh/ssh_config'
sudo scp -pr /etc/ssh ==edit: YOURUSERNAME==@==edit: BACKUPSERVERIP==:/home/==edit: YOURUSERNAME==/scp_backup/
```

Note that the program re-copies all of the files entirely, regardless of whether (or how much) they have changed.

\==action: SSH to your backup_server system==, to look at your backup files:

> Tip: Running `ssh *username*@*server-ip-address*` will log you in with *username* on the system. Assuming the remote computer has the same user account available (as is the case with the VMs provided), you can omit "username", and just run `ssh *ip-address*`, and you will be prompted to provide authentication for your own account, as configured on their system. However, this does **not** apply when you use `sudo`, as described in the warning at the start of this lab.

So, that is:

```bash
ssh ==edit: BACKUPSERVERIP==
```

> Note: Enter your password when prompted.

List the files that have been backed up:

```bash
ls -la scp_backup/ssh/
```

\==action: Exit ssh==:

> Tip: Type `exit` (or press Ctrl-D).
>
> Note, this command will close your bash shell, if you are not logged in via ssh.

#### Hackerbot Attack #1 {#hackerbot-attack-1}

You can skip the bot to here, by saying **goto 1**.

> Hackerbot: Use scp to back up the desktop's `/usr/bin` directory to the backup_server, into the directory that Hackerbot names in the chat.

When you are ready for the bot to run the attack, ==action: say 'ready'== to Hackerbot.

> Note: Hackerbot will tell you the exact directory name (including the random suffix) in the chat when it runs this attack. As described above, scp needs the destination to exist, so create the `remote-bin-backup-...` directory first and then copy `/usr/bin` into it. If scp reports `realpath ...: No such file` or `path canonicalization failed`, the destination directory does not exist yet.

> Tip: Why `/usr/bin` rather than `/bin`? On current Debian (and most modern Linux distributions), `/bin` is simply a symbolic link to `/usr/bin`, which you can confirm by running `ls -ld /bin`. Since `/usr/bin` is where the programs are actually stored, it is the directory worth backing up.

> Note: Expect scp to finish with `scp: local "/usr/bin/X11" is not a regular file` ... `failed to upload directory /usr/bin ...`; everything else *has* been copied. `/usr/bin/X11` is a legacy compatibility symbolic link that points back to `/usr/bin` itself (`ls -l /usr/bin/X11` shows `X11 -> .`), so following it would recurse indefinitely. Scp does not follow symbolic links to directories, so it skips this one and reports the failure. This illustrates a limitation of scp, which only copies regular files and directories; rsync, which you will use next, copies symbolic links *as* symbolic links.

Don't forget to ==action: save and submit any flags!==

## Rsync, deltas and epoch backups {#rsync-deltas-and-epoch-backups}

Rsync is a popular tool for copying files locally, or over a network. Rsync can use delta encoding (only sending *differences* over the network rather than whole files) to reduce the amount of data that needs to be transferred. Many commercial backup systems provide a managed frontend for `rsync`.

> Note: Make sure you exited SSH above, and are now running commands on your local system.

Let's start by doing a simple ==action: copy of your /etc/ directory== to a local copy:

```bash
sudo rsync -av /etc ~/backups/rsync_backup/
```

> Note: The presence of a trailing "/" changes the behaviour, so be careful when constructing rsync commands. In this case we are copying the directory (not just its contents), into the directory rsync_backup. See the man page for more information.

Rsync reports the amount of data "sent".

Read the Rsync man page (`man rsync`), to ==action: understand the flags we have used== (`-a` and `-v`). As you will see, Rsync has a great deal of options.

Now, let's ==action: add a file to /etc, and repeat the backup:==

```bash
sudo bash -c 'echo hello > /etc/hello'

sudo rsync -av /etc ~/backups/rsync_backup/
```

Note that only the new file was transferred to update our epoch (full) backup of /etc/.

## Rsync remote copies via SSH with compression {#rsync-remote-copies-via-ssh-with-compression}

Rsync can act as a server, listening on a TCP port. It can also be used via SSH, as you will see. ==action: Copy your /etc/ directory to your backup_server== system using Rsync via SSH:

```bash
sudo rsync -avzh -M--fake-super /etc ==edit: YOURUSERNAME==@==edit: BACKUPSERVERIP==:/home/==edit: YOURUSERNAME==/remote-rsync-backup/
```

> Tip: this is all one line

Note that this initial copy will have used less network traffic compared to `scp`, due to the `-z` flag, which tells `rsync` to use compression. ==action: Compare the amount of data sent== (as reported by Rsync in the previous command -- the `-h` told Rsync to use human readable sizes) to the size of the data that was sent:

```bash
sudo du -sh /etc
```

Now, if you were to ==action: delete a local file== that had been backed up:

```bash
sudo rm /etc/hello
```

Even if you ==action: re-sync your local changes== to the backup_server, the file will not be deleted from the server:

```bash
sudo rsync -avzh -M--fake-super /etc ==edit: YOURUSERNAME==@==edit: BACKUPSERVERIP==:/home/==edit: YOURUSERNAME==/remote-rsync-backup/
```

To recover the file, you can simply ==action: retrieve the backup:==

```bash
sudo rsync -avz -M--fake-super ==edit: YOURUSERNAME==@==edit: BACKUPSERVERIP==:/home/==edit: YOURUSERNAME==/remote-rsync-backup/etc/hello /etc/
```

> Note: The `-M--fake-super` option preserves the original ownership and permissions of your files. Since rsync on the backup_server runs as your user, it cannot set the real owners of the backed up files; instead, this option has it record them alongside each backup copy, so that they can be put back when you restore. Use it both when backing up and when restoring; this also avoids the need for root SSH access to the backup_server, which, for security reasons, is not usually permitted.

\==action: Delete the file locally, and sync the changes== *including deletions* to the server so that it is also deleted there:

```bash
sudo rm /etc/hello
sudo rsync -avzh -M--fake-super --delete /etc ==edit: YOURUSERNAME==@==edit: BACKUPSERVERIP==:/home/==edit: YOURUSERNAME==/remote-rsync-backup/
```

> Note the added **`--delete`**

\==action: Confirm that the file has been deleted== from the backup stored on the server.

> Hint: login via SSH and view the backups

> Log Book Question: Compare the file access/modification times of the scp and rsync backups, are they the same/similar? If not, why?

#### Hackerbot Attack #2 {#hackerbot-attack-2}

You can skip the bot to here, by saying **goto 2**.

> Hackerbot: Use rsync to take a full (epoch) backup of the second user's home directory on the backup_server, keeping the ownership of their files.

When you are ready for the bot to run the attack, ==action: say 'ready'== to Hackerbot.

> Note: Remember that the trailing "/" changes whether you are copying directories or their contents. For example, `rsync ... /home/==edit: SECONDUSER== ...:/home/==edit: YOURUSERNAME==/remote-rsync-full-backup/` copies the *directory* itself, so it ends up as `remote-rsync-full-backup/==edit: SECONDUSER==`. You should use this same structure for every backup in the remainder of this lab.

> Note: You will need to use `sudo`, since a number of ==edit: SECONDUSER=='s files (such as their `.ssh` directory) are private to them; without it, rsync reports `Permission denied` and leaves those files out. Additionally, if rsync reports `mkdir "..." failed: No such file or directory`, the *parent* of your destination does not exist, as rsync only creates the final directory in the destination path.

Don't forget to ==action: save and submit any flags!==

## Rsync incremental/differential backups {#rsync-incremental-differential-backups}

If you need to keep daily backups, it would be an inefficient use of disk space (and network traffic and/or disk usage) to simply save separate full copies of your entire backup each day. Therefore, it often makes sense to copy only the files that have changed for our daily backup. This can either be comparisons to the last backup (incremental), or last full backup (differential).

### Differential backups {#differential-backups}

\==action: Create a new file== in /etc:

```bash
sudo bash -c 'echo "hello there" > /etc/hello'
```

And now let's ==action: create differential backups== of our changes to /etc (both local and remote backup copies):

```bash
# local
sudo rsync -av /etc --compare-dest=$HOME/backups/rsync_backup/ ~/backups/rsync_backup_week1/

# remote
sudo rsync -avzh -M--fake-super /etc --compare-dest=/home/==edit: YOURUSERNAME==/remote-rsync-backup/ ==edit: YOURUSERNAME==@==edit: BACKUPSERVERIP==:/home/==edit: YOURUSERNAME==/remote-rsync-backup-week1/
```

> Note: The `--compare-dest` flag tells rsync to search these backup copies, and only copy files if they have changed since a backup. Refer to the man page for further explanation.

> Warning: There are three common mistakes that cause `--compare-dest` to silently copy *everything*, so that your "differential" is in fact another full backup:
>
> - **Using `~` in the path, for a local backup.** The shell does not expand `~` after the `=` in an option such as `--compare-dest=~/...`, so rsync receives a literal `~`; it warns `--compare-dest arg does not exist: ~/backups/rsync_backup`, but this is easily missed amongst the list of files. Use `$HOME/...` or the full path instead, as shown above. For a *remote* backup, `~` happens to work, since rsync passes the path to the backup_server as a separate word and the backup_server's shell expands it to your home directory there; however, a full path works in both cases.
> - **Using a relative path.** A relative path such as `--compare-dest=remote-rsync-backup/` (without a leading `/`) is interpreted relative to the *destination directory*, so rsync cannot find it, warns `--compare-dest arg does not exist`, and copies everything.
> - **Pointing at the wrong level of directory.** The `--compare-dest` directory must have the same layout as your destination. Since we copy `/etc` (without a trailing slash), the destination contains an `etc/` directory, and `rsync_backup/` likewise contains `etc/`. If you point it one level too deep (for example, `.../rsync_backup/etc/`), nothing matches, and rsync does not warn you at all.
>
> A dry run (`-n`) shows what would be copied; if it lists every file, check your `--compare-dest`.

Look at what is contained in the differential update:

```bash
ls -la ~/backups/rsync_backup_week1/etc
```

Note that there are lots of empty directories, with only the files that have actually changed (in this case /etc/hello).

Now ==action: create another change== to /etc:

```bash
sudo bash -c 'echo "hello there!" > /etc/hi'
```

To ==action: make another differential backup== (saving changes since the last full backup), we just repeat the previous command(s), with a new destination directory:

```bash
# local
sudo rsync -av /etc --compare-dest=$HOME/backups/rsync_backup/ ~/backups/rsync_backup_week2/

# remote
sudo rsync -avzh -M--fake-super /etc --compare-dest=/home/==edit: YOURUSERNAME==/remote-rsync-backup/ ==edit: YOURUSERNAME==@==edit: BACKUPSERVERIP==:/home/==edit: YOURUSERNAME==/remote-rsync-backup-week2/
```

\==action: Look at the contents== of your new backup. You will find it now contains your two new files. That is, all of the changes since the full backup.

\==action: Delete a non-essential existing file== in /etc/, and our test hello file:

```bash
sudo rm /etc/wgetrc /etc/hello
```

Now ==action: restore from your backups== by first restoring from the full backup, then the latest differential backup ("week2"). The advantage of a differential backup, is you only need to use two commands to restore your system.

```bash
sudo rsync -avz -M--fake-super ==edit: YOURUSERNAME==@==edit: BACKUPSERVERIP==:/home/==edit: YOURUSERNAME==/remote-rsync-backup/etc/ /etc/

sudo rsync -avz -M--fake-super ==edit: YOURUSERNAME==@==edit: BACKUPSERVERIP==:/home/==edit: YOURUSERNAME==/remote-rsync-backup-week2/etc/ /etc/
```

> Tip: This example restores from the remote copy. ==action: Try restoring from the local copy==.

#### How the Hackerbot backup tasks fit together {#how-the-hackerbot-backup-tasks-fit-together}

From this point onwards, Hackerbot alternates between having ==edit: SECONDUSER== change their files and asking you to back them up. Consequently, each backup must be taken **after** the change that precedes it, and **before** the change that follows it, as summarised in the table below:

| Hackerbot step | What happens | Your backup should contain |
|---|---|---|
| 2 | *(you)* full backup -> `remote-rsync-full-backup/` | all of SECONDUSER's files |
| 3 | SECONDUSER makes changes (A) | |
| 4 | *(you)* differential -> `remote-rsync-differential1/` | A |
| 5 | SECONDUSER makes changes (B) | |
| 6 | *(you)* differential -> `remote-rsync-differential2/` | A + B (everything since the full backup) |
| 7 | SECONDUSER makes changes (C) | |
| 8 | *(you)* incremental -> `remote-rsync-incremental1/` | C only (compare with full + differential2) |
| 9 | SECONDUSER makes changes (D) | |
| 10 | *(you)* incremental -> `remote-rsync-incremental2/` | D only (compare with full + differential2 + incremental1) |
| 11 | Hackerbot checks all five backups above, then deletes SECONDUSER's files | |
| 12 | *(you)* restore: full -> differential2 -> incremental1 -> incremental2 | |

If you take a backup at the wrong moment, Hackerbot will tell you which changes are missing, or which should not be there; as described in the tips at the start of this lab, you can then use `goto` (to step 3, 5, 7 or 9) followed by `ready` to return SECONDUSER's files to the state they were in at that step, delete the bad backup, and take it again.

Since every backup builds on the ones before it, **you cannot skip any of them**. If you jump ahead (for example, straight to the incremental backup at step 8 without having taken differential2), Hackerbot tells you which earlier backup is missing, and which `goto` returns you to the point at which it should be taken.

#### Hackerbot Attack #3 {#hackerbot-attack-3}

You can skip the bot to here, by saying **goto 3**.

> Hackerbot: The second user is about to change some of their files.

When you are ready for the bot to run the attack, ==action: say 'ready'== to Hackerbot.

> Note: In this step, Hackerbot simply changes a number of SECONDUSER's files in preparation for the next backup. (Hint: Keep an eye out for a flag...)

Don't forget to ==action: save and submit any flags!==

#### Hackerbot Attack #4 {#hackerbot-attack-4}

You can skip the bot to here, by saying **goto 4**.

> Hackerbot: Take a differential backup of the second user's home directory, containing only the changes since the full backup.

When you are ready for the bot to run the attack, ==action: say 'ready'== to Hackerbot.

> Note: This backup uses the same structure as your full backup (the source is `/home/==edit: SECONDUSER==`, without a trailing slash, and the destination is `.../remote-rsync-differential1/`), with the addition of `--compare-dest=/home/==edit: YOURUSERNAME==/remote-rsync-full-backup/`, which is the directory that *contains* `==edit: SECONDUSER==/`. Do a dry run (`-n`) first; it should list only the files that were changed at step 3.

Don't forget to ==action: save and submit any flags!==

### Incremental backups {#incremental-backups}

The disadvantage of the above differential approach to backups, is that your daily backup gets bigger and bigger for every backup until you do another full backup. With *incremental backups* you *only store the changes since the last backup*.

Now ==action: create another change== to /etc:

```bash
sudo bash -c 'echo "Another test change" > /etc/test1'

sudo bash -c 'echo "Another test change" > /etc/hello'
```

Now ==action: create an incremental backup== based on the last differential backup:

```bash
# local
sudo rsync -av /etc --compare-dest=$HOME/backups/rsync_backup/ --compare-dest=$HOME/backups/rsync_backup_week2/ ~/backups/rsync_backup_monday/

# remote
sudo rsync -avzh -M--fake-super /etc --compare-dest=/home/==edit: YOURUSERNAME==/remote-rsync-backup/ --compare-dest=/home/==edit: YOURUSERNAME==/remote-rsync-backup-week2/ ==edit: YOURUSERNAME==@==edit: BACKUPSERVERIP==:/home/==edit: YOURUSERNAME==/remote-rsync-backup-monday/
```

\==action: Another change== to /etc:

```bash
sudo bash -c 'echo "Another test change" > /etc/test2'
```

Now ==action: create an incremental backup based on the last differential backup and the last incremental backup:==

```bash
# local
sudo rsync -av /etc --compare-dest=$HOME/backups/rsync_backup/ --compare-dest=$HOME/backups/rsync_backup_week2/ --compare-dest=$HOME/backups/rsync_backup_monday/ ~/backups/rsync_backup_tuesday/

# remote
sudo rsync -avzh -M--fake-super /etc --compare-dest=/home/==edit: YOURUSERNAME==/remote-rsync-backup/ --compare-dest=/home/==edit: YOURUSERNAME==/remote-rsync-backup-week2/ --compare-dest=/home/==edit: YOURUSERNAME==/remote-rsync-backup-monday/ ==edit: YOURUSERNAME==@==edit: BACKUPSERVERIP==:/home/==edit: YOURUSERNAME==/remote-rsync-backup-tuesday/
```

Now ==action: delete a number of files:==

```bash
sudo rm /etc/wgetrc /etc/hello /etc/test1 /etc/test2
```

\==action: Restore /etc== by restoring from the full backup, then the last differential backup, then the first incremental backup, then the second incremental backup.

#### Hackerbot Attack #5 {#hackerbot-attack-5}

You can skip the bot to here, by saying **goto 5**.

> Hackerbot: The second user is about to make some more changes.

When you are ready for the bot to run the attack, ==action: say 'ready'== to Hackerbot.

> Note: There is no flag this time; this step simply changes SECONDUSER's files in preparation for your next backup.

Don't forget to ==action: save and submit any flags!==

#### Hackerbot Attack #6 {#hackerbot-attack-6}

You can skip the bot to here, by saying **goto 6**.

> Hackerbot: Take a second differential backup, containing all of the changes since the full backup.

When you are ready for the bot to run the attack, ==action: say 'ready'== to Hackerbot.

> Note: Your differential backup should include all of the changes since the full backup (including the first set of changes, from step 3), but not the original files. You should therefore compare against the full backup only, rather than differential1.

Don't forget to ==action: save and submit any flags!==

#### Hackerbot Attack #7 {#hackerbot-attack-7}

You can skip the bot to here, by saying **goto 7**.

> Hackerbot: The second user is about to make even more changes.

When you are ready for the bot to run the attack, ==action: say 'ready'== to Hackerbot.

Don't forget to ==action: save and submit any flags!==

#### Hackerbot Attack #8 {#hackerbot-attack-8}

You can skip the bot to here, by saying **goto 8**.

> Hackerbot: Take an incremental backup, containing only the changes since your last backup (differential2).

When you are ready for the bot to run the attack, ==action: say 'ready'== to Hackerbot.

> Note: This requires two `--compare-dest` options: one for `remote-rsync-full-backup/`, and one for `remote-rsync-differential2/`.

Don't forget to ==action: save and submit any flags!==

### Rsync snapshot backups {#rsync-snapshot-backups}

Another approach to keeping backups is to keep a snapshot of all of the files, but wherever the files have not changed, a hard link is used to point at a previously backed up copy. If you are unfamiliar with hard links, read more about them online. This approach gives users a snapshot of how the system was on a particular date, without having to have redundant full copies of files.

These snapshots can be achieved using the `--link-dest` flag. Open the Rsync man page, and read about `--link-dest`. Let's see it in action.

\==action: Make an rsync snapshot== containing hard links to files that have not changed, with copies for files that have changed:

```bash
sudo rsync -av --delete --link-dest=$HOME/backups/rsync_backup/ /etc ~/backups/rsync_backup_snapshot_1
```

Rsync reports not having copied any new files, yet look at what is contained in rsync_backup_snapshot_1. It looks like a complete copy, yet is **taking up almost no extra storage space**. ==action: Check this== by counting the files that are hard links (that is, files with more than one name for the same data), and comparing the space used by each backup (`du` counts each hard-linked file only once):

```bash
sudo find ~/backups/rsync_backup_snapshot_1 -type f -links +1 | wc -l
sudo du -sh ~/backups/rsync_backup ~/backups/rsync_backup_snapshot_1
```

> Note: The snapshot does still take up a small amount of space, since directories cannot be hard linked, and so every directory in the snapshot is newly created.

\==action: Create other changes== to /etc:

```bash
sudo bash -c 'echo "Another test change" > /etc/test3'

sudo bash -c 'echo "Another test change" > /etc/test4'
```

And ==action: make a new rsync snapshot==, with copies of the files that have changed, and hard links to the previous snapshot for everything else:

```bash
sudo rsync -av --delete --link-dest=$HOME/backups/rsync_backup_snapshot_1/ /etc ~/backups/rsync_backup_snapshot_2
```

\==action: Delete some files==, and ==action: make a new differential rsync snapshot==. Although Rsync does not report a deletion, the deleted files will be absent from the new snapshot.

\==action: Recover a file from a previous snapshot.==

#### Hackerbot Attack #9 {#hackerbot-attack-9}

You can skip the bot to here, by saying **goto 9**.

> Hackerbot: The second user is about to make one more set of changes.

When you are ready for the bot to run the attack, ==action: say 'ready'== to Hackerbot.

Don't forget to ==action: save and submit any flags!==

#### Hackerbot Attack #10 {#hackerbot-attack-10}

You can skip the bot to here, by saying **goto 10**.

> Hackerbot: Take a second incremental backup, containing only the changes since incremental1.

When you are ready for the bot to run the attack, ==action: say 'ready'== to Hackerbot.

> Note: Your backup should include only the changes since the last backup, which this time requires three `--compare-dest` options.

> Hackerbot quiz: A question about the contents of one of your earlier backups, which you will need to look up on the backup_server.

\==action: answer *YOURANSWER*== to Hackerbot with what the file said, to get another flag. Since the desktop's copy has changed since then, you will need to use your backup.

Don't forget to ==action: save and submit any flags!==

#### Hackerbot Attack #11 {#hackerbot-attack-11}

You can skip the bot to here, by saying **goto 11**.

> Hackerbot: Hackerbot checks your backups, and then deletes the second user's files.

When you are ready for the bot to run the attack, ==action: say 'ready'== to Hackerbot.

> Warning: Hackerbot will delete all of the second user's files! However, it checks your backups first, and will not attack until all five (full, differential1, differential2, incremental1 and incremental2) are correct, since the restore requires four of them and the final task requires differential1. If it refuses, its FYI output shows which backup is wrong, and why.

Don't forget to ==action: save and submit any flags!==

#### Hackerbot Attack #12 {#hackerbot-attack-12}

You can skip the bot to here, by saying **goto 12**.

> Hackerbot: Use your backups to restore all of the second user's files on the desktop, with their original ownership.

When you are ready for the bot to run the attack, ==action: say 'ready'== to Hackerbot.

> Note: Restore from the full backup, then apply the differential and incremental backups in the correct order (as shown in the table above), to end up with all of the files restored. Each restore copies the *contents* of a backup's `==edit: SECONDUSER==/` directory into `/home/==edit: SECONDUSER==/`, so the source requires a trailing slash, for example: `sudo rsync -av -M--fake-super ==edit: YOURUSERNAME==@==edit: BACKUPSERVERIP==:/home/==edit: YOURUSERNAME==/remote-rsync-full-backup/==edit: SECONDUSER==/ /home/==edit: SECONDUSER==/` (and the same for the others).

> Tip: If the restore goes wrong and SECONDUSER's home directory is left in a mess, say `goto 11` followed by `ready`; Hackerbot deletes their files again, so that you can restore from scratch.

Don't forget to ==action: save and submit any flags!==

#### Hackerbot Attack #13 {#hackerbot-attack-13}

You can skip the bot to here, by saying **goto 13**.

> Hackerbot: Restore one of the second user's files to the first version of it that was backed up.

When you are ready for the bot to run the attack, ==action: say 'ready'== to Hackerbot.

> Hint: The notes file did not exist when you took the full backup. Think about which of your backups holds the very first version of it, and restore just that file from there.

Don't forget to ==action: save and submit any flags!==

## Resources {#resources}

http://webgnuru.com/linux/rsync_incremental.php

http://everythinglinux.org/rsync/
