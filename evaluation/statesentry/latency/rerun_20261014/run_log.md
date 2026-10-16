# Latency re-run on a fresh VM pair: run log

Brief: latency_brief_v2.md (DESSERT 2026 revision). Worktree branch
`dessert_2026_revision`, HEAD 580c77fec. "Original" = 8de3e6c9e, "final" = HEAD.
Not committed. All times are UTC on 16 Oct 2026 unless marked BST. The server's
Logstash and router logs are in BST.

## Results

| File | Run | Window (script Started → Finished) | Detections | md5 |
|---|---|---|---|---|
| `indexed_original.txt` | 1 (repeat, as aaa_admin) | 10:37:11Z → 11:04:09Z | 50 | 244302ec6bf22505ccca6d079b566e76 |
| `indexed_final.txt` | 2 | 11:10:08Z → 11:37:07Z | 50 | 0fa7bef17624b7ecfbdd2c78312b2a02 |
| `filter_original.txt` | 3 | 11:44:29Z → 12:11:28Z | 50 | f7e8420cfe5456c58882003f0e5fbce8 |
| `filter_final.txt` | 4 | 12:18:44Z → 12:45:43Z | 50 | 2b8b61a6d51b904fa8b225d572c8c7ef |
| `indexed_original_run_as_root.txt` | 1, first attempt (broken, kept) | 09:58:30Z → 10:25:29Z | 50 | 74f282bff8be6851b4382700d836f789 |

- **Analysis windows:** `show_runs.py` windows for the indexed files:

  | Run | --window-start | --window-end |
  |---|---|---|
  | run 1, as root | 09:58:33Z | 10:25:58Z |
  | run 1, repeat | 10:37:13Z | 11:04:39Z |
  | run 2 | 11:10:11Z | 11:37:37Z |

  Each indexed file holds every elastalert_status document and router line up to
  its export. So indexed_final.txt contains all three indexed runs and must be
  analysed with its own window, as for the 31 July files.
- **Detections:**
  - Indexed: ElastAlert status documents for readable_shadow inside the window.
  - Filter: lines in the FILTER DETECTIONS section. All 50 in each filter file
    are `/etc/shadow` / `deploy-noekea-01-…-readable_shadow-rf-0`. The detection
    times in each file run from the first attempt to the last (run 3 11:44:31Z …
    12:10:58Z; run 4 12:18:46Z … 12:45:13Z).
- **Router lines:** 50 new `raise_alert_event_received` lines per run.
  - Runs 1 repeat, 2, 3 and 4 each added 100 `action_dispatched` lines, two per
    detection, one for each configured action.
  - The 31 July indexed file has 6,390 dispatch lines, because ElastAlert re-ran
    the alerter on every poll. That repetition did not happen here. I noted this
    and did not investigate it.
- **Raw output and copies:** each script's raw output is copied verbatim from
  /tmp on the server. Server copies, with each run's console log, are in
  /root/latency_v2/results/.

## VM pair

| Role | VMID | Name | IP |
|---|---|---|---|
| server | 732895409 | deploy-noekea-01-experiment-aaa-auto-grading-server | 172.16.0.4 (ens19) |
| victim | 944278675 | deploy-noekea-01-experiment-aaa-hackme-desktop | 172.16.0.3 (ens19) |
| attack | 267127866 | deploy-noekea-01-experiment-aaa-attack-vm | stopped throughout |

- **Build:** project `projects/deploy-noekea-01_SecGen20261014_142834`, ended 14
  Oct 15:04 BST with "ReadSSHInfo: IP not found" on attack_vm only. Server and
  victim ran every Puppet provisioner through `cleanup` with no `Error:` lines.
- **Differences from the 31 July pair:**
  - **Attack VM:** the 31 July pair had its attack VM running. This pair's
    attack VM was stopped for all runs.
  - **Post-build step skipped:** the net0 teardown and reboot never ran because
    of the attack_vm failure. **ens18 (net0) is still attached on both VMs.**
    Since the reboot it has been DOWN with no address.
  - **Services before the reboot:** the server had logstash, elasticsearch,
    elastalert-index and kibana enabled but never started.
- **Reboot:** **the user rebooted both VMs, at about 10:31 BST on 16 Oct** (the
  agent's own reboot attempts were refused by the permission system). `who -b`
  shows server 10:31 and victim 10:30 BST.
- **VM resources:** none were changed.

## Checks after the reboot (09:39Z)

- **Server services active:** logstash, elasticsearch, kibana, elastalert,
  alert_router, postgresql. Ports 5044, 9200 and 5601 are listening.
- **elastalert-index:** `failed` (start-limit-hit, 10:32:44 BST), but `/ea` was
  created at 10:32:28 BST and the elastalert_status indices exist. The old server
  shows the same state (failed, with /ea present).
- **ES:** yellow, 1 node, 5 unassigned replica shards. It listens on 172.16.0.4
  only.
- **Victim services active:** auditbeat and filebeat (7.17.29, output
  172.16.0.4:5044), ssh. The audit rules include `-w /etc/ -p r -k etc`.
  /etc/shadow is mode 644.
- **ssh:** an ssh login as challenger from the server succeeded, run as root and
  later as aaa_admin.

## Manual setup steps

All of these were also done by hand on the 31 July server.

1. **Ingest pipeline `add_ingest_timestamp`** (09:49:09Z). It was missing on the
   fresh ES, and nothing in the repo creates it, so Logstash rejected every event
   with HTTP 400 (5,072 failures up to 10:49:01 BST).
   - The coordinator read the 31 July definition from the old server, starting
     and then stopping its ES. The old server's /root/confs_and_logs.txt shows
     the same "pipeline does not exist" errors at 15:13 BST on 31 July.
   - I created it with a PUT of
     `{"description":"Adds ingest timestamp","processors":[{"set":{"field":"_source.ingest_timestamp","value":"{{_ingest.timestamp}}"}}]}`.
     The GET back is byte-identical to the 31 July definition.
   - There were no 400s afterwards. New auditbeat-* and secgen-events-*
     documents carry `ingest_timestamp`.
2. **`/etc/sudoers.d/logstash-alertrouter`**, installed **by the user** at 11:32
   BST on 16 Oct with tmp/s/sudoers_install.sh.
   - root:root, mode 0440, 93 bytes, md5 fd1f83c556a44ae88c4d3d2b7c93d4a1.
   - Content: `logstash ALL=(aaa_admin) NOPASSWD: /usr/bin/ruby /opt/alert_actioner/alert_router.rb raise *`.
   - `visudo -cf` reported parsed OK, and `sudo -l -U logstash` shows the rule.
   - On 31 July the same file was created at 1 Aug 00:30:11 BST, just before
     the filter run. The filter detection calls the router through it.
3. **Empty `/var/log/logstash/filter_detections.log`**, owned by logstash, mode
   644, created before run 3 (11:42Z).
   - The file did not exist. If benchmark_filter.sh's `> file` had created it,
     the file would have been owned by root and Logstash could not have written
     to it.
   - On 31 July it already existed from setup runs.
4. **Running benchmark_indexed.sh as aaa_admin.** This is how the 31 July
   indexed run must have been run. The old server has no `root` PostgreSQL role
   either, yet its results have 6,390 dispatch lines and no role errors.
   - The unchanged script (md5 3a772c69…) was copied to /var/tmp/lat_aa/ (owner
     aaa_admin, mode 700).
   - It was started with `sudo -u aaa_admin -H bash -c '…CLIENT_PASS="$(cat
     /var/tmp/lat_aa/cp)"…'`. The cp file was owned by aaa_admin with mode 600.
   - benchmark_filter.sh ran as root, as written: it calls
     `sudo -u aaa_admin … reset` itself.

## Detection files and their sources

- **Hostname changes in deployed copies:** the repo's logstash_filter.rb and
  generate_rules.rb hard-code the victim hostname
  `tom-noekea-2-experiment-aaa-hackme-desktop`. The deployed rules and filter
  detection files are copies that differ from the git or generated text **only**
  in that hostname, changed to `deploy-noekea-01-experiment-aaa-hackme-desktop`,
  so that the fresh server's alert router recognises the alert names.
  - In each filter_detection.rb the only diff is line 5 (`@hostname = …`).
  - In each rule the only diffs are the `name:` line and the `--alert-name` in
    `command:`.

| File | Source | Source md5 | Deployed md5 |
|---|---|---|---|
| combined_path.rb (original) | 8de3e6c9e `modules/services/unix/logging/logstash/files/combined_path.rb` | f7788454abc9e7439f0157c32a58e8d8 | same |
| combined_path.rb (final) | HEAD, same path (= after_readers/combined_path.rb) | b3997988df3e25d6b52bc9cb3e364ff5 | same (also what the build installed) |
| filter_detection.rb (original) | 8de3e6c9e `modules/services/unix/logging/analysis_alert_action_server/files/logstash_filter.rb` | c2f80a0e38f169dbb1ec23dc468ad03d | bad77ea1a56850adb4b547a4da214177 (hostname only) |
| filter_detection.rb (final) | HEAD, same path (= after_readers/logstash_filter.rb) | b797fddbf531e2c4214360e11ab4729f | 2c1b05bb0cbe4247fa303178fad5b76a (hostname only) |
| readable_shadow rule (original) | `generate_rules.rb` run on 8de3e6c9e `lib/helpers/rules.rb` (1a356bcc…) | ec744dbe6e18bca6729249278f473587 | 8926fd1ac5f5554fcb722636621bd87b (hostname only) |
| readable_shadow rule (final) | `generate_rules.rb` on HEAD `lib/helpers/rules.rb` (4900fcd3…) with read_by `challenger`; full output = after_readers/readable_shadow_rules.txt (70fedf81…) | 2698f21d0e19125a970d93181f0064a1 | 617744d9553cd3ed09196ba7eeca9aef (hostname only) |
| Logstash conf, indexed | the build's own conf (module `configfile-template.erb`, same at both commits, md5 49a218f88ede9636d8edfde89fb20a63), plus the two timing ruby filters from filter_ls_conf.yml | — | b3393ea6d10b69d10f1305f55f02fa5a |
| Logstash conf, filter | `modules/services/unix/logging/analysis_alert_action_server/files/filter_ls_conf.yml`, same at both commits | 61ce3cc787c2e4123401573576b0957f | same |
| alert_router.rb | `…/files/alert_actioner/alert_router.rb`, same at 8de3e6c9e and HEAD | 9cc64b00241df788e67eef5a9a5b966f | same, as installed |
| benchmark_indexed.sh / benchmark_filter.sh | HEAD `evaluation/statesentry/latency/`, unchanged | 3a772c69b2af9c279f7c6c0e9cdbcb63 / 92376d102a0f9b5e2c7951032a9a9955 | same |

- **Original rule against 31 July:** the original generated rule is identical to
  the 31 July rule still deployed on the old server 869458117 (ec744dbe…). There
  is no difference.
- **Indexed conf:** apart from the two timing filters it differs from the
  build's file only by a trailing newline.
- **Filter conf:** filter_ls_conf.yml differs from the 31 July deployed conf
  (de52bbba…) in whitespace only (`diff -w` exit 0).
- **Old server's alert_router.rb:** 663f0106…; per the coordinator it differs
  from git only by a trailing newline.

## What was switched, run by run

- **Deployment:** every deployment used `s/deploy_latency_v4.sh MODE LABEL`
  (in /home/deploy/.claude/jobs/44eff3ec/tmp). Staged files went into
  /root/latency_v2/stage/ and were checked by md5 after upload. Each live file
  was copied to `/root/latency_v2/backup/LABEL/` before it was replaced, so
  nothing was deleted.
- **elastalert and alert_router:** a restart of elastalert also restarts
  alert_router, through `Requires=elastalert.service`. In filter mode only
  elasticsearch was stopped, and elastalert and alert_router stayed up.

| Run | Files installed | Services |
|---|---|---|
| 1 (as root) | combined_path.rb original, indexed conf, rule original. Backup `run1_indexed_original/` holds the build's own combined_path.rb (HEAD), my_ls_config.conf (49a218f8) and rule (the HEAD rule without read_by, 2f98b806) | ES already up (yellow). elastalert restarted 09:55:31Z, logstash 09:55:39Z |
| 1 repeat | same three files again (no change). Backup `run1_indexed_original_repeat/` | elastalert restarted 10:34:34Z, logstash 10:34:42Z |
| 2 | combined_path.rb final, indexed conf (unchanged), rule final. Backup `run2_indexed_final/` | elastalert restarted 11:07:30Z, logstash 11:07:37Z |
| 3 | combined_path.rb original, filter conf, filter_detection.rb original (new file). The final rule stays in place, but nothing reads it while ES is down. Backup `run3_filter_original/` | ES stopped 11:41:56Z, logstash restarted 11:42:04Z |
| 4 | combined_path.rb final, filter conf (unchanged), filter_detection.rb final. Backup `run4_filter_final/`. filter_detections.log (run 3's 50 lines) was copied to `backup/filter_detections.log.before_run4_filter_final` before the script emptied it. /tmp/benchmark_filter_results.txt from run 3 was moved to `.before_run4_filter_final` | ES stopped 12:16:10Z (already stopped), logstash restarted 12:16:17Z |

- **Restarts:** after every Logstash restart the beats input was listening on
  5044 within 25 s, and the Ruby script test runs passed.
- **Before run 1 repeat:** run 1's /tmp/benchmark_results.txt (root-owned) was
  renamed to `.run1_as_root` so that aaa_admin could write the new one.

- **Final state, left running:**
  - The run 4 configuration is still deployed.
  - ES is stopped. logstash, elastalert, alert_router and kibana are active.
  - The ingest pipeline and the sudoers file are still in place.

## elastalert_status `_count` before each indexed run

| Run | `_count` | Before |
|---|---|---|
| 1 (as root) | 0 | 09:57:48Z |
| 1 repeat | 50 | 10:35:28Z |
| 2 | 100 | 11:08:26Z |

All are well below the 900 limit.

## Run 1 first attempt: broken, kept as `indexed_original_run_as_root.txt`

- **What went wrong:** it was launched as root (`s/run_bench.sh`).
  benchmark_indexed.sh calls `ruby alert_router.rb reset --all` without sudo,
  and all 50 resets failed with `FATAL: role "root" does not exist`, because
  PostgreSQL has only `aaa_admin`.
- **Effect:** the router actions were dispatched only once (2 `action_dispatched`
  lines, 09:58:38Z). The detection stages and router receipt were unaffected (50
  and 50).
- **Why it was repeated:** it was repeated because the invoking user was wrong,
  not because of its numbers. The repeat ran as aaa_admin, and its log has no
  role errors.

## Clock readings

- **Neither guest synchronised:** neither guest's clock is NTP-synchronised
  (systemd-timesyncd active, "System clock synchronized: no", no reachable
  server, no chrony or ntpq) in every reading below.
- **Server-to-victim readings (`s/sv_clock.sh`):** run on the server; date, ssh
  to the victim as challenger for `date -u +%s.%N`, date, three times. The ssh
  round trip is about 180–230 ms, most of it before the remote `date`.
  - **Bound:** the intersection over the three readings of [victim −
    server_after, victim − server_before].
  - **Midpoint diffs:** victim − (server_before + server_after)/2.
- **Host-mediated readings (`clock2.sh`):** host / guest-agent / host. The
  agent round trip is about 10.2 s, so these only bound each guest's offset
  from the Proxmox host to about ±5 s.

| Reading | Time | victim − server bound (ms) | midpoint diffs (ms) | guest − host midpoint (agent, s) victim / server |
|---|---|---|---|---|
| run 1 before | 09:58:11Z | −8.3 … +166.3 | 92.5 / 84.5 / 78.9 | 1.73–1.76 / 1.69–1.75 |
| run 1 after | 10:26:02Z | −10.7 … +175.7 | 93.5 / 85.4 / 82.5 | 1.70–1.81 / 1.68–1.69 |
| run 1 repeat before | 10:37:00Z | −8.0 … +181.5 | 97.4 / 86.5 / 86.8 | 1.71–1.72 / 1.71–1.72 |
| run 1 repeat after | 11:04:39Z | −11.2 … +171.6 | 91.7 / 80.5 / 80.2 | 1.68–1.69 / 1.11–1.66 |
| run 2 before | 11:09:58Z | −8.5 … +173.3 | 88.4 / 78.1 / 82.4 | 1.64–1.74 / 1.64–1.70 |
| run 2 after | 11:39:04Z | −10.5 … +179.8 | 99.4 / 84.6 / 86.5 | 1.66–1.67 / 1.65–1.69 |
| run 3 before | 11:44:18Z | −8.8 … +164.9 | 93.3 / 75.0 / 80.4 | 0.66–1.66 / 0.35–1.64 |
| run 3 after | 12:13:20Z | −10.7 … +179.4 | 96.6 / 106.2 / 83.3 | 1.19–1.63 / 1.18–1.65 |
| run 4 before | 12:18:33Z | −10.2 … +176.7 | 89.3 / 83.3 / 82.7 | 0.53–1.69 / 1.58–1.62 |
| run 4 after | 12:47:39Z | −9.0 … +193.2 | 100.6 / 95.2 / 91.8 | 1.62–1.74 / 1.60–1.65 |

The scattered agent readings (for example 0.35 s) came with shorter or longer
agent round trips (7–12 s) and reflect that method's ±5 s uncertainty, not clock
steps. The ssh-based bounds stay the same throughout. Raw readings are in
/home/deploy/.claude/jobs/44eff3ec/tmp: `clock_<run>_{before,after}.txt` and
`svclock_<run>_{before,after}.txt`, where run1r is the repeat of run 1.

## Credentials

- The challenger password (the account generator's literal value in
  scenarios/examples/auto_grading/experiment_aaa.xml) only reached the server as
  mode-600 files: /root/latency_v2/cp (root) and /var/tmp/lat_aa/cp
  (aaa_admin).
- It was read by `CLIENT_PASS="$(cat file)"` and never printed or placed on a
  command line.
- Both files were deleted after run 4 (12:49Z). /root/latency_v2/cp had also
  been deleted during the pause after the first run 1 and uploaded again later.

## Deviations from the brief

- **Run 1 was repeated:** see above.
- **Manual setup steps 1–4:** each reproduces the 31 July setup.
- **Hostname-only changes** in the deployed rules and filter detection files.
- **Post-build state:** the post-build net0 teardown was skipped, so ens18 is
  still attached (DOWN). The reboot was done by the user, not by the post-build
  step.
- **Attack VM:** stopped, where on 31 July it was running.
