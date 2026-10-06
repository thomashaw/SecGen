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

This lab **needs to be completed in order**.

> Tip: You should use the rsync dry run option (by adding `-n` to the command) to test which files are going to be backed up, without making the changes.

You should manually check you have done your backups correctly **before** telling Hackerbot you are ready. It may be a good idea to SSH to your backup server in a separate console tab (but do keep an eye on which system you are running each command on!):

```bash
ssh ==edit: the backup_server's IP address==
```

> Note: Hackerbot's FYI outputs can include error messages that don't indicate that you did something wrong. For example, an error might state that a file doesn't exist -- this only gives you some transparency about what Hackerbot is looking at, that error doesn't really tell you whether or not Hackerbot wanted to see that error.

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

You'll use this second username (==edit: SECONDUSER== in the commands below) later in the lab, when Hackerbot asks you to back up their files.

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

\==action: Backup your /etc/ directory to the backup_server== computer using `scp`:

```bash
sudo scp -pr /etc/ ==edit: YOURUSERNAME==@==edit: BACKUPSERVERIP==:/home/==edit: YOURUSERNAME==/ssh_etc_backup
```

> Tip: You will be prompted for your local password, to confirm the host's fingerprint ("yes"), and the remote password (which is the same). This copy may take some time -- feel free to open another terminal console (Ctrl-T), to read the scp man page while you wait.

Read the scp man page to ==action: determine what the `-p` and `-r` flags do==.

> Hint: `man scp`, press "q" to quit.

Now, let's add a file to /etc, and repeat the backup:

```bash
sudo bash -c 'echo > /etc/hi'
sudo scp -pr /etc/ ==edit: YOURUSERNAME==@==edit: BACKUPSERVERIP==:/home/==edit: YOURUSERNAME==/ssh_backup/
```

Note that the program re-copies all of the files entirely, regardless of whether (or how much) they have changed.

\==action: SSH to your backup_server system==, to look at your backup files:

> Tip: `ssh *username*@*server-ip-address*` will log you in with *username* on the system. Assuming the remote computer has the same user account available (as is the case with the VMs provided), you can omit "username", and just run `ssh *ip-address*`, and you will be prompted to provide authentication for your own account, as configured on their system.

So, that is:

```bash
ssh ==edit: BACKUPSERVERIP==
```

> Note: Enter your password when prompted.

List the files that have been backed up:

```bash
ls -la ssh_backup/
```

\==action: Exit ssh==:

> Tip: `exit` (Or Ctrl-D)
>
> Note, this command will close your bash shell, if you are not logged in via ssh.

#### Hackerbot Attack #1 {#hackerbot-attack-1}

You can skip the bot to here, by saying **goto 1**.

> Hackerbot: Use scp to copy the desktop /bin/ directory to the backup_server: BACKUPSERVERIP:/home/YOURUSERNAME/remote-bin-backup-*(a short suffix Hackerbot gives you in the chat)*/, which should then include the backed up bin/ directory.

When you are ready for the bot to run the attack, ==action: say 'ready'== to Hackerbot.

> Note: Hackerbot will tell you the exact directory name (including the random suffix) in the chat when it runs this attack. Remember that a trailing "/" changes whether you are copying directories or their contents.

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
sudo rsync -avzh --fake-super /etc ==edit: YOURUSERNAME==@==edit: BACKUPSERVERIP==:/home/==edit: YOURUSERNAME==/remote-rsync-backup/
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
sudo rsync -avzh --fake-super /etc ==edit: YOURUSERNAME==@==edit: BACKUPSERVERIP==:/home/==edit: YOURUSERNAME==/remote-rsync-backup/
```

To recover the file, you can simply ==action: retrieve the backup:==

```bash
sudo rsync -avz --fake-super ==edit: YOURUSERNAME==@==edit: BACKUPSERVERIP==:/home/==edit: YOURUSERNAME==/remote-rsync-backup/etc/hello /etc/
```

> Note: The `--fake-super` option is used to ensure the recovered file is still owned by root (even though it is not associated with root on the backup_server). This avoids requiring SSH root access to the remote machine (for security reasons this is not usually done) to retain ownership and so on.

\==action: Read the man page entry for `--fake-super`==

> Hint: `man rsync`, then press '/' followed by '--fake-super$', and enter.

\==action: Delete the file locally, and sync the changes== *including deletions* to the server so that it is also deleted there:

```bash
sudo rm /etc/hello
sudo rsync -avzh --fake-super --delete /etc ==edit: YOURUSERNAME==@==edit: BACKUPSERVERIP==:/home/==edit: YOURUSERNAME==/remote-rsync-backup/
```

> Note the added **`--delete`**

\==action: Confirm that the file has been deleted== from the backup stored on the server.

> Hint: login via SSH and view the backups

> Log Book Question: Compare the file access/modification times of the scp and rsync backups, are they the same/similar? If not, why?

#### Hackerbot Attack #2 {#hackerbot-attack-2}

You can skip the bot to here, by saying **goto 2**.

> Hackerbot: It's your job to set up remote backups for ==edit: SECONDUSER== (a user on your system). Use rsync to create a full (epoch) remote backup of /home/==edit: SECONDUSER== from your desktop system to the backup_server: BACKUPSERVERIP:/home/YOURUSERNAME/remote-rsync-full-backup/SECONDUSER.

When you are ready for the bot to run the attack, ==action: say 'ready'== to Hackerbot.

> Note: Remember that the trailing "/" changes whether you are copying directories or their contents.

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
sudo rsync -av /etc --compare-dest=~/backups/rsync_backup/ ~/backups/rsync_backup_week1/

# remote
sudo rsync -avzh --fake-super /etc --compare-dest=/home/==edit: YOURUSERNAME==/remote-rsync-backup/ ==edit: YOURUSERNAME==@==edit: BACKUPSERVERIP==:/home/==edit: YOURUSERNAME==/remote-rsync-backup-week1/
```

> Note: The `--compare-dest` flag tells rsync to search these backup copies, and only copy files if they have changed since a backup. Refer to the man page for further explanation.

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
sudo rsync -av /etc --compare-dest=~/backups/rsync_backup/ ~/backups/rsync_backup_week2/

# remote
sudo rsync -avzh --fake-super /etc --compare-dest=/home/==edit: YOURUSERNAME==/remote-rsync-backup/ ==edit: YOURUSERNAME==@==edit: BACKUPSERVERIP==:/home/==edit: YOURUSERNAME==/remote-rsync-backup-week2/
```

\==action: Look at the contents== of your new backup. You will find it now contains your two new files. That is, all of the changes since the full backup.

\==action: Delete a non-essential existing file== in /etc/, and our test hello file:

```bash
sudo rm /etc/wgetrc /etc/hello
```

Now ==action: restore from your backups== by first restoring from the full backup, then the latest differential backup ("week2"). The advantage of a differential backup, is you only need to use two commands to restore your system.

```bash
sudo rsync -avz --fake-super ==edit: YOURUSERNAME==@==edit: BACKUPSERVERIP==:/home/==edit: YOURUSERNAME==/remote-rsync-backup/etc/ /etc/

sudo rsync -avz --fake-super ==edit: YOURUSERNAME==@==edit: BACKUPSERVERIP==:/home/==edit: YOURUSERNAME==/remote-rsync-backup-week2/etc/ /etc/
```

> Tip: This example restores from the remote copy. ==action: Try restoring from the local copy==.

#### Hackerbot Attack #3 {#hackerbot-attack-3}

You can skip the bot to here, by saying **goto 3**.

> Hackerbot: The SECONDUSER user is about to create some files...

When you are ready for the bot to run the attack, ==action: say 'ready'== to Hackerbot.

> Note: This step just has Hackerbot create some files as SECONDUSER, ready for the next backup. (Hint: Keep an eye out for a flag...)

Don't forget to ==action: save and submit any flags!==

#### Hackerbot Attack #4 {#hackerbot-attack-4}

You can skip the bot to here, by saying **goto 4**.

> Hackerbot: Create a differential backup of SECONDUSER's home directory to the backup_server: BACKUPSERVERIP:/home/YOURUSERNAME/remote-rsync-differential1/SECONDUSER/.

When you are ready for the bot to run the attack, ==action: say 'ready'== to Hackerbot.

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
sudo rsync -av /etc --compare-dest=~/backups/rsync_backup/ --compare-dest=~/backups/rsync_backup_week2/ ~/backups/rsync_backup_monday/

# remote
sudo rsync -avzh --fake-super /etc --compare-dest=/home/==edit: YOURUSERNAME==/remote-rsync-backup/ --compare-dest=/home/==edit: YOURUSERNAME==/remote-rsync-backup-week2/ ==edit: YOURUSERNAME==@==edit: BACKUPSERVERIP==:/home/==edit: YOURUSERNAME==/remote-rsync-backup-monday/
```

\==action: Another change== to /etc:

```bash
sudo bash -c 'echo "Another test change" > /etc/test2'
```

Now ==action: create an incremental backup based on the last differential backup and the last incremental backup:==

```bash
# local
sudo rsync -av /etc --compare-dest=~/backups/rsync_backup/ --compare-dest=~/backups/rsync_backup_week2/ --compare-dest=~/backups/rsync_backup_monday/ ~/backups/rsync_backup_tuesday/

# remote
sudo rsync -avzh --fake-super /etc --compare-dest=/home/==edit: YOURUSERNAME==/remote-rsync-backup/ --compare-dest=/home/==edit: YOURUSERNAME==/remote-rsync-backup-week2/ --compare-dest=/home/==edit: YOURUSERNAME==/remote-rsync-backup-monday/ ==edit: YOURUSERNAME==@==edit: BACKUPSERVERIP==:/home/==edit: YOURUSERNAME==/remote-rsync-backup-tuesday/
```

Now ==action: delete a number of files:==

```bash
sudo rm /etc/wgetrc /etc/hello /etc/test1 /etc/test2
```

\==action: Restore /etc== by restoring from the full backup, then the last differential backup, then the first incremental backup, then the second incremental backup.

#### Hackerbot Attack #5 {#hackerbot-attack-5}

You can skip the bot to here, by saying **goto 5**.

> Hackerbot: The SECONDUSER user is about to create some more files...

When you are ready for the bot to run the attack, ==action: say 'ready'== to Hackerbot.

> Note: Ok, good... No flag this time, carry on...

Don't forget to ==action: save and submit any flags!==

#### Hackerbot Attack #6 {#hackerbot-attack-6}

You can skip the bot to here, by saying **goto 6**.

> Hackerbot: Create another differential backup of SECONDUSER's home directory to the backup_server: BACKUPSERVERIP:/home/YOURUSERNAME/remote-rsync-differential2/.

When you are ready for the bot to run the attack, ==action: say 'ready'== to Hackerbot.

> Note: Your differential backup should include all changes since the full backup (including the first set of changes), but not the original files.

Don't forget to ==action: save and submit any flags!==

#### Hackerbot Attack #7 {#hackerbot-attack-7}

You can skip the bot to here, by saying **goto 7**.

> Hackerbot: The SECONDUSER user is about to create even more files...

When you are ready for the bot to run the attack, ==action: say 'ready'== to Hackerbot.

Don't forget to ==action: save and submit any flags!==

#### Hackerbot Attack #8 {#hackerbot-attack-8}

You can skip the bot to here, by saying **goto 8**.

> Hackerbot: Create an incremental backup of SECONDUSER's home directory to the backup_server: BACKUPSERVERIP:/home/YOURUSERNAME/remote-rsync-incremental1/SECONDUSER/. Base it on the epoch and also differential2.

When you are ready for the bot to run the attack, ==action: say 'ready'== to Hackerbot.

Don't forget to ==action: save and submit any flags!==

### Rsync snapshot backups {#rsync-snapshot-backups}

Another approach to keeping backups is to keep a snapshot of all of the files, but wherever the files have not changed, a hard link is used to point at a previously backed up copy. If you are unfamiliar with hard links, read more about them online. This approach gives users a snapshot of how the system was on a particular date, without having to have redundant full copies of files.

These snapshots can be achieved using the `--link-dest` flag. Open the Rsync man page, and read about `--link-dest`. Let's see it in action.

\==action: Make an rsync snapshot== containing hard links to files that have not changed, with copies for files that have changed:

```bash
sudo rsync -av --delete --link-dest=~/backups/rsync_backup/ /etc ~/backups/rsync_backup_snapshot_1
```

Rsync reports not having copied any new files, yet look at what is contained in rsync_backup_snapshot_1. It looks like a complete copy, yet is **taking up almost no extra storage space**.

\==action: Create other changes== to /etc:

```bash
sudo bash -c 'echo "Another test change" > /etc/test3'

sudo bash -c 'echo "Another test change" > /etc/test4'
```

And ==action: make a new rsync snapshot==, with copies of files that have changed:

```bash
sudo rsync -av --delete --link-dest=~/backups/rsync_backup/ /etc ~/backups/rsync_backup_snapshot_2
```

\==action: Delete some files==, and ==action: make a new differential rsync snapshot==. Although Rsync does not report a deletion, the deleted files will be absent from the new snapshot.

\==action: Recover a file from a previous snapshot.==

#### Hackerbot Attack #9 {#hackerbot-attack-9}

You can skip the bot to here, by saying **goto 9**.

> Hackerbot: Again, the SECONDUSER user is about to create even more files...

When you are ready for the bot to run the attack, ==action: say 'ready'== to Hackerbot.

Don't forget to ==action: save and submit any flags!==

#### Hackerbot Attack #10 {#hackerbot-attack-10}

You can skip the bot to here, by saying **goto 10**.

> Hackerbot: Create another incremental backup of SECONDUSER's home directory to the backup_server: BACKUPSERVERIP:/home/YOURUSERNAME/remote-rsync-incremental2/SECONDUSER/.

When you are ready for the bot to run the attack, ==action: say 'ready'== to Hackerbot.

> Note: Your backup should include just the changes since the last backup.

> Hackerbot quiz: Access the backups via SSH. What's the contents of SECONDUSER/personal_secrets/nothing_much?

\==action: answer *YOURANSWER*== to Hackerbot with the file's contents, to get another flag.

Don't forget to ==action: save and submit any flags!==

#### Hackerbot Attack #11 {#hackerbot-attack-11}

You can skip the bot to here, by saying **goto 11**.

> Hackerbot: I am going to attack you now!

When you are ready for the bot to run the attack, ==action: say 'ready'== to Hackerbot.

> Warning: Hackerbot will delete all of the second user's files! Make sure your backups are in place first.

Don't forget to ==action: save and submit any flags!==

#### Hackerbot Attack #12 {#hackerbot-attack-12}

You can skip the bot to here, by saying **goto 12**.

> Hackerbot: Use all the backups (including differential and incremental) to restore all of SECONDUSER's files on the desktop system.

When you are ready for the bot to run the attack, ==action: say 'ready'== to Hackerbot.

> Note: Restore from the full backup, then apply the differential backups and incremental backups, in the correct order, to end up with all of the files restored.

Don't forget to ==action: save and submit any flags!==

#### Hackerbot Attack #13 {#hackerbot-attack-13}

You can skip the bot to here, by saying **goto 13**.

> Hackerbot: Restore SECONDUSER's notes file to its earliest state.

When you are ready for the bot to run the attack, ==action: say 'ready'== to Hackerbot.

> Hint: Think about which of your backups holds the very first version of the notes file, and restore just that file from there.

Don't forget to ==action: save and submit any flags!==

## Resources {#resources}

http://webgnuru.com/linux/rsync_incremental.php

http://everythinglinux.org/rsync/
