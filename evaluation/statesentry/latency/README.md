# Pipeline latency characterisation

Raw results and analysis scripts behind Table IV of the DESSERT 2026 paper
(*Automated State-Based Progress Validation for Randomised Cyber Security
Exercises*, Section V.D). They compare the indexed detection path
(Logstash → Elasticsearch → ElastAlert) with the filter-based path (detection
inside Logstash). Both were run on 31 July 2026 in a single-instance replica of
the exercise infrastructure.

## Files

| File | Contents |
|---|---|
| `benchmark_indexed.sh`, `benchmark_filter.sh` | The scripts that ran each benchmark and exported its results (see below) |
| `benchmark_results_1.txt` | Indexed path: Elasticsearch `_search` responses (`elastalert_status`, `secgen-events-*`) and alert-router `BENCHMARK` log lines |
| `benchmark_results_2.txt` | Filter-based path: `FILTER DETECTIONS` lines in the format of `/var/log/logstash/filter_detections.log`, plus alert-router `BENCHMARK` log lines |
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

## Reproducing Table IV

Each file must be analysed with **its own** time window:

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
