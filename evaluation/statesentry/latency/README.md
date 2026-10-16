# Pipeline latency characterisation

Raw results and analysis scripts behind Table IV of the DESSERT 2026 paper
(*Automated State-Based Progress Validation for Randomised Cyber Security
Exercises*, Section V.D). They compare the indexed detection path
(Logstash → Elasticsearch → ElastAlert) with the filter-based path (detection
inside Logstash). The benchmark experiment is the set of four runs made on
16 October 2026 on the final code, described in
[Re-run on the final code (16 October 2026)](#re-run-on-the-final-code-16-october-2026),
with its data and analysis in `rerun_20261014/`. The earlier runs of 31 July
2026, made on the code as of 8de3e6c9e in a single-instance replica of the
exercise infrastructure, are kept for reference in `benchmark_results_1.txt`
and `benchmark_results_2.txt`; the sections that follow the file table describe
those runs.

## Files

| File | Contents |
|---|---|
| `benchmark_indexed.sh`, `benchmark_filter.sh` | The scripts that ran each benchmark and exported its results (see below) |
| `rerun_20261014/` | The four runs of 16 October 2026 on the final code, their analysis outputs (`analysis_*.txt`) and the run log; see the last section |
| `benchmark_results_1.txt` | Earlier run, 31 July 2026, code as of 8de3e6c9e, kept for reference. Indexed path: Elasticsearch `_search` responses (`elastalert_status`, `secgen-events-*`) and alert-router `BENCHMARK` log lines |
| `benchmark_results_2.txt` | Earlier run, 31 July 2026, code as of 8de3e6c9e, kept for reference. Filter-based path: `FILTER DETECTIONS` lines in the format of `/var/log/logstash/filter_detections.log`, plus alert-router `BENCHMARK` log lines |
| `analyse.py` | Per-stage and end-to-end latency, full distributions, and the indexed-vs-filter comparison. Python 3.6+, no dependencies. Run `python3 analyse.py` with no arguments for the modes and the measurement decisions |
| `show_runs.py` | Lists the distinct runs in a results file, to find the analysis window |
| `filter_to_es_json.py` | Converts the pipe-separated filter detections into the layout `analyse.py` reads |

The instrumentation that produced these timestamps is in this branch:
`modules/services/unix/logging/analysis_alert_action_server/files/filter_ls_conf.yml`
(Logstash timing fields and detection output), `logstash_filter.rb` (filter-based
detection), and the `BENCHMARK` lines in `alert_actioner/alert_router.rb`.

## How the data were collected

Each script ran on the analysis server and repeated the following 50 times:

1. Reset the alert router, so that its actions fire again.
2. Wait 2 s.
3. Read `/etc/shadow` on the victim as `challenger` over SSH.
4. Wait 30 s, giving about 32.4 s between repetitions.

The scripts then exported the results:

- **`benchmark_indexed.sh`** produced `benchmark_results_1.txt`. It queries Elasticsearch for every ElastAlert status document and greps the alert-router log. Its `SECGEN EVENTS` query (a `terms` query on `combined_path`) returned no documents, so that section is empty. This does not affect any figure: each ElastAlert status document carries the full matched event, including every pipeline timestamp, in `match_body`, and `analyse.py` reads them from there.
- **`benchmark_filter.sh`** produced `benchmark_results_2.txt`. It clears `/var/log/logstash/filter_detections.log` before the loop, so the file holds only that run, and exports the log after it.

Both export steps grep the whole alert-router log, without a time limit. That is why the router lines span the whole day.

The scripts are as run, except that the client password is now read from `CLIENT_PASS` instead of being hard-coded.

## Analysing the 31 July runs

These commands reproduce the figures from the earlier 31 July runs. Each file must be analysed with **its own** time window:

```
# Indexed path (n=50)
python3 analyse.py dist benchmark_results_1.txt \
    --window-start 2026-07-31T21:21:17Z --window-end 2026-07-31T21:48:43Z

# Filter-based path (n=50)
python3 filter_to_es_json.py benchmark_results_2.txt benchmark_results_2_es.txt
python3 analyse.py dist benchmark_results_2_es.txt \
    --window-start 2026-07-31T23:32:10Z --window-end 2026-07-31T23:59:37Z
```

Besides the per-stage rows, each run reports three **response-time** rows,
computed per event (measurement decision 7 in `analyse.py`):

| Row | From | To | Indexed median | Filter median |
|---|---|---|---|---|
| Detection → alert router receives alert | detection (`alert_time` / `filter_detection_time`) | router `raise_alert_event_received` | 135.0 ms | 148.6 ms |
| Detection → first action dispatched | detection | first router `action_dispatched` | 256.5 ms | 298.4 ms |
| End-to-end (system call → first action dispatched) | audit event `@timestamp` | first router `action_dispatched` | 4401.8 ms | 806.7 ms |

These rows use the same adjacency pairing as the router row, anchored to each
detection. Rows that start at the system call compare the monitored VM's clock
with the analysis server's. "Dispatched" is logged just before the action runs,
so the last row is a lower bound on what a participant sees.

`describe` gives the same figures with exclusion counts and structural checks.
`compare benchmark_results_1.txt benchmark_results_2_es.txt` gives the reduction
and a bootstrap confidence interval for the difference in medians. Its detection
and per-event response figures are valid with any window covering both runs. Take
the window-based router row ("Alert router: receipt → first action dispatched")
from the per-file runs, though: a single window spanning both runs attributes each
run's router lines to the other, which the tool warns about.

## Notes on the data

- **Setup runs.** Runs recorded before 21:21 UTC on 31 July 2026 were made while
  the benchmark environment was being set up and are excluded from the reported
  figures. These include a complete indexed run beginning 15:55 UTC, which showed
  intermittent collection-stage delays of up to 17 s. `python3 show_runs.py
  benchmark_results_1.txt` lists every run in the file. The router log lines in
  both files cover the whole day, because the collection was not limited by time,
  and are filtered by window during analysis.
- **Exclusion policy.** This is applied identically to both files: the goal path
  `/etc/shadow` only, and a 32 s ceiling on any single stage (the repetition
  spacing).
- **Percentiles** use linear interpolation (Hyndman–Fan type 7, as in NumPy's
  default). With n=50, the 99th percentile is an order statistic rather than a
  population estimate, so the paper reports the maximum instead.
- **Dispatch latency** (detection → first action dispatched) is indicative.
  Router log lines carry no correlation identifier, so each detection is paired
  with the next dispatch by adjacency.
- **Repeated `action_dispatched` lines in the indexed run.** ElastAlert re-invokes
  the alerter on every poll (about every 3.25 s; the rules set `realert: 0`). The
  router logs each call, but its per-action state means an action is carried out
  only once. These lines are no-ops, which is why the indexed run has many more
  dispatch lines than detections. The filter path has no polling and shows exactly
  two dispatches per detection, one for each configured action.
- `benchmark_results_2.txt` was converted from an RTF export to plain text. Its
  `FILTER DETECTIONS` section has no header row. The columns are
  `@timestamp|logstash_receive_time|logstash_normalised_time|filter_detection_time|combined_path|filter_alert_name`,
  as set by the output format in `filter_ls_conf.yml`.

## Re-run on the final code (16 October 2026)

This section describes the benchmark experiment: four runs, made one after
another on 16 October 2026 (UTC) on a single freshly built VM pair, which
measure each detection path on the code as of 8de3e6c9e ("original") and on
the code at 580c77fec on branch `dessert_2026_revision` ("final"). Its inputs
and outputs are in `rerun_20261014/`, and `run_log.md` there is the full record
of the setup, checksums, clock readings and changes between runs, from which
this section is drawn.

### Setup and VMs

The pair was built for the `experiment_aaa` auto-grading scenario as project
`deploy-noekea-01_SecGen20261014_142834` on Proxmox:

| Role | VMID | Name | IP |
|---|---|---|---|
| Analysis server | 732895409 | deploy-noekea-01-experiment-aaa-auto-grading-server | 172.16.0.4 |
| Victim | 944278675 | deploy-noekea-01-experiment-aaa-hackme-desktop | 172.16.0.3 |
| Attack VM | 267127866 | deploy-noekea-01-experiment-aaa-attack-vm | stopped throughout |

The build ended with "ReadSSHInfo: IP not found" on the attack VM only; the
server and victim completed every Puppet provisioner without errors. Because of
that failure the post-build step never ran, so the provisioning interface
(ens18, net0) is still attached to both VMs, although it has been down with no
address since the reboot. Both VMs were rebooted by hand at about 09:31 UTC,
and no VM resources were changed. The setup differs from the 31 July pair in two
respects: the attack VM was stopped rather than running, and the net0 teardown
was not performed.

### Runs and analysis windows

Each run repeated the loop described in
[How the data were collected](#how-the-data-were-collected) 50 times, and each
produced 50 detections. The script times are those of the script's own
"Started" and "Finished" lines; the analysis windows are those given by
`show_runs.py` for each file.

| Run | Path, code | File | Script ran | Analysis window |
|---|---|---|---|---|
| 1 | Indexed, original | `indexed_original.txt` | 10:37:11Z to 11:04:09Z | 10:37:13Z to 11:04:39Z |
| 2 | Indexed, final | `indexed_final.txt` | 11:10:08Z to 11:37:07Z | 11:10:11Z to 11:37:37Z |
| 3 | Filter, original | `filter_original.txt` | 11:44:29Z to 12:11:28Z | 11:44:31Z to 12:11:58Z |
| 4 | Filter, final | `filter_final.txt` | 12:18:44Z to 12:45:43Z | 12:18:46Z to 12:46:13Z |

Each indexed file holds every ElastAlert status document and router line
recorded up to its export, so `indexed_final.txt` also contains run 1 and the
broken first attempt at run 1 (described below), and must be analysed with its
own window. The filter files hold only their own run's detections, but, as on
31 July, their router lines cover the whole day. `filter_original_es.txt` and
`filter_final_es.txt` are the outputs of `filter_to_es_json.py`.

### Files deployed, with sources and checksums

The detection files installed for each run were taken from the two commits as
follows (md5 of the source, and of the deployed copy where it differs).

| File | Source | Source md5 | Deployed md5 |
|---|---|---|---|
| `combined_path.rb`, original | 8de3e6c9e `modules/services/unix/logging/logstash/files/combined_path.rb` | f7788454abc9e7439f0157c32a58e8d8 | same |
| `combined_path.rb`, final | 580c77fec, same path | b3997988df3e25d6b52bc9cb3e364ff5 | same |
| `filter_detection.rb`, original | 8de3e6c9e `modules/services/unix/logging/analysis_alert_action_server/files/logstash_filter.rb` | c2f80a0e38f169dbb1ec23dc468ad03d | bad77ea1a56850adb4b547a4da214177 |
| `filter_detection.rb`, final | 580c77fec, same path | b797fddbf531e2c4214360e11ab4729f | 2c1b05bb0cbe4247fa303178fad5b76a |
| `readable_shadow` rule, original | `generate_rules.rb` on 8de3e6c9e `lib/helpers/rules.rb` | ec744dbe6e18bca6729249278f473587 | 8926fd1ac5f5554fcb722636621bd87b |
| `readable_shadow` rule, final | `generate_rules.rb` on 580c77fec `lib/helpers/rules.rb`, read_by `challenger` | 2698f21d0e19125a970d93181f0064a1 | 617744d9553cd3ed09196ba7eeca9aef |
| Logstash conf, indexed | the build's own conf (md5 49a218f88ede9636d8edfde89fb20a63, the same at both commits) plus the two timing filters from `filter_ls_conf.yml` | | b3393ea6d10b69d10f1305f55f02fa5a |
| Logstash conf, filter | `…/analysis_alert_action_server/files/filter_ls_conf.yml`, the same at both commits | 61ce3cc787c2e4123401573576b0957f | same |
| `alert_router.rb` | `…/files/alert_actioner/alert_router.rb`, the same at both commits | 9cc64b00241df788e67eef5a9a5b966f | same |
| `benchmark_indexed.sh`, `benchmark_filter.sh` | this directory, unchanged | 3a772c69b2af9c279f7c6c0e9cdbcb63, 92376d102a0f9b5e2c7951032a9a9955 | same |

The repository's `logstash_filter.rb` and `generate_rules.rb` hard-code the
victim hostname of the 31 July pair (`tom-noekea-2-experiment-aaa-hackme-desktop`).
Where the deployed md5 differs, the deployed copy differs from its source only
in that hostname, changed to `deploy-noekea-01-experiment-aaa-hackme-desktop`
so that the new server's alert router recognises the alert names: line 5 of
each `filter_detection.rb`, and the `name:` line and `--alert-name` argument of
each rule. The original rule is identical to the rule still deployed on the
31 July server, and the filter conf differs from the one deployed there in
whitespace only.

The md5 of each raw results file is:

| File | md5 |
|---|---|
| `indexed_original.txt` | 244302ec6bf22505ccca6d079b566e76 |
| `indexed_final.txt` | 0fa7bef17624b7ecfbdd2c78312b2a02 |
| `filter_original.txt` | f7e8420cfe5456c58882003f0e5fbce8 |
| `filter_final.txt` | 2b8b61a6d51b904fa8b225d572c8c7ef |
| `indexed_original_run_as_root.txt` | 74f282bff8be6851b4382700d836f789 |

### Manual setup steps

Four steps were carried out by hand; each reproduces something that was also
done by hand on the 31 July server.

1. **Ingest pipeline `add_ingest_timestamp`.** Nothing in the repository
   creates it, and without it the fresh Elasticsearch rejected Logstash's
   writes with HTTP 400. It was created at 09:49Z with the 31 July definition,
   `{"description":"Adds ingest timestamp","processors":[{"set":{"field":"_source.ingest_timestamp","value":"{{_ingest.timestamp}}"}}]}`,
   which reads back byte-identical to the definition on the old server; no
   400s occurred afterwards.
2. **`/etc/sudoers.d/logstash-alertrouter`**, through which the filter
   detection calls the router, containing
   `logstash ALL=(aaa_admin) NOPASSWD: /usr/bin/ruby /opt/alert_actioner/alert_router.rb raise *`
   (root:root, mode 0440, md5 fd1f83c556a44ae88c4d3d2b7c93d4a1).
3. **An empty `/var/log/logstash/filter_detections.log`**, owned by `logstash`
   with mode 644, created before run 3. The file did not exist; had
   `benchmark_filter.sh` created it with `>`, it would have been owned by root
   and Logstash could not have written to it.
4. **`benchmark_indexed.sh` run as `aaa_admin`.** The script calls
   `ruby alert_router.rb reset --all` without sudo, and PostgreSQL on the
   server has only an `aaa_admin` role, so the reset works only as that user.
   The unchanged script was run with `sudo -u aaa_admin -H`, with the client
   password read from a mode-600 file. `benchmark_filter.sh` ran as root, as
   written, since it calls `sudo -u aaa_admin … reset` itself.

### Clocks

Neither guest's clock was NTP-synchronised during any of the runs. The offset
between victim and server was therefore read before and after every run by
taking the server's time, the victim's time over ssh, and the server's time
again, three times in succession. Across all ten readings, the bounds on victim
minus server lie within −11.2 ms to +193.2 ms and the midpoint estimates within
75.0 ms to 106.2 ms, with no drift visible over the day. Rows that start at the
system call (the collection stage and both end-to-end rows) take the audit
timestamp from the victim's clock and the later boundary from the server's, so
they carry this offset of roughly 80 to 100 ms; the other rows are measured on
the server alone. Readings through the Proxmox guest agent bound each guest's
offset from the host only to about ±5 s, which is too coarse to use here.

### What was switched between runs

Each deployment backed up every live file before replacing it and checked the
staged files by md5 after upload. Restarting `elastalert` also restarts
`alert_router`.

| Run | Files installed | Services |
|---|---|---|
| 1 | original `combined_path.rb`, indexed conf, original rule | elastalert restarted 10:34:34Z, logstash 10:34:42Z |
| 2 | final `combined_path.rb`, indexed conf (unchanged), final rule | elastalert restarted 11:07:30Z, logstash 11:07:37Z |
| 3 | original `combined_path.rb`, filter conf, original `filter_detection.rb` | Elasticsearch stopped 11:41:56Z, logstash restarted 11:42:04Z |
| 4 | final `combined_path.rb`, filter conf (unchanged), final `filter_detection.rb`; `filter_detections.log` emptied by the script | Elasticsearch already stopped, logstash restarted 12:16:17Z |

In the filter runs only Elasticsearch was stopped; elastalert and the router
stayed up. The final rule remained installed during runs 3 and 4 but was not
read, since Elasticsearch was down. The `elastalert_status` count before runs 1
and 2 was 50 and 100 respectively, well below the 900 limit.

### The repeated run 1

The first attempt at run 1 (09:58:30Z to 10:25:29Z) was launched as root. All
50 router resets failed with `FATAL: role "root" does not exist`, so the router
dispatched its actions only once in the whole run, although detection and
router receipt were unaffected. Run 1 was repeated as `aaa_admin` because the
invoking user was wrong, not because of its figures, and the repeat's log shows
no role errors. The first attempt is kept as `indexed_original_run_as_root.txt`
and is not analysed.

### Router dispatch lines

Each of the four runs added 50 `raise_alert_event_received` lines and 100
`action_dispatched` lines to the router log, two per detection (one for each
configured action), on both paths. The 31 July indexed file has 6,390 dispatch
lines, which [Notes on the data](#notes-on-the-data) attributes to ElastAlert
re-invoking the alerter on every poll. That repetition did not occur in these
runs, and its cause was not investigated.

### Results

The analysis outputs are `rerun_20261014/analysis_dist_<run>.txt` and
`analysis_describe_<run>.txt` for each run, each with its own window, and
`analysis_compare_<A>_vs_<B>.txt` for the three comparisons. `compare` labels
its columns "Indexed" and "Filter"; read these as A and B.

Medians in ms, from the `dist` outputs (n=50 in every cell):

| Stage | Run 1, indexed original | Run 2, indexed final | Run 3, filter original | Run 4, filter final |
|---|---|---|---|---|
| Audit event generation → Logstash ingestion | 2504.3 | 1748.5 | 16.0 | 15.9 |
| Logstash output → ES searchable | 35.5 | 28.9 | n/a | n/a |
| ES searchable → detector match | 2513.8 | 2266.4 | n/a | n/a |
| End-to-end (system call → detection) | 5645.0 | 5025.5 | 16.3 | 16.2 |
| Detection → alert router receives alert | 143.1 | 148.6 | 149.2 | 151.2 |
| Detection → first action dispatched | 278.1 | 250.7 | 303.0 | 283.3 |
| End-to-end (system call → first action dispatched) | 5922.4 | 5337.9 | 320.9 | 302.0 |
| Alert router: receipt → first action dispatched | 135.3 | 108.8 | 154.2 | 132.5 |

The bootstrap 95% confidence intervals for the difference in median detection
latency (B − A, 10,000 resamples) are:

| Comparison | Output | Difference | 95% CI |
|---|---|---|---|
| Filter path, original (A) vs final (B) | `analysis_compare_filter_original_vs_final.txt` | −0.3 ms | [−3.2, 1.4] |
| Indexed final (A) vs filter final (B), for Table IV | `analysis_compare_indexed_final_vs_filter_final.txt` | −5009.6 ms | [−6272.4, −4014.7] |
| Indexed path, original (A) vs final (B) | `analysis_compare_indexed_original_vs_final.txt` | not valid, see below | |

The following limits apply to these comparisons:

- **Indexed original vs final.** `compare` applies one window to both files,
  and `indexed_final.txt` also contains run 1's documents, so a window covering
  runs 1 and 2 puts 100 events (runs 1 and 2 together) in the B column of
  `analysis_compare_indexed_original_vs_final.txt`. Its B column, deltas and
  confidence interval are therefore not valid, and the output is kept only as a
  record. The per-run medians in the table above are the valid figures for this
  comparison, and no confidence interval is given for it.
- **Router row.** In all three `compare` outputs the window spans both runs, so
  the window-based "Alert router: receipt → first action dispatched" row mixes
  the two runs' router lines (B is paired over 100 or 150 router events rather
  than 50); that row should be taken from the per-run outputs above. The
  per-event rows are anchored to each detection and are unaffected.
- **Clock offset.** Rows that start at the system call include the victim to
  server offset of roughly 80 to 100 ms described under Clocks.
- **Collection stage.** The collection stage (audit event → Logstash
  ingestion) has a median of 1.7 to 2.5 s in the indexed runs against 16 ms in
  the filter runs, on the same victim, which was not changed between runs; on
  the server only the Logstash conf and the state of Elasticsearch differ. This
  accounts for part of the end-to-end gap between the paths and has not been
  explained.

The warnings `analyse.py` printed are recorded in the output files. Every
`dist` and `describe` output warns that at n=50 the p99 is an order statistic
rather than a population estimate. In the filter original vs final and indexed
final vs filter final comparisons it also warns that the per-event router
pairing (n=50, median 132.5 ms) disagrees with the window-based pairing (n=100,
median 139.5 ms, and n=150, median 119.5 ms, respectively). All three compares
warn that 50 router identifiers appear in both files, which is consistent with
each file carrying the same whole-day router log.
