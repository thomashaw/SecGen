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
| `benchmark_results_1.txt` | Indexed path: Elasticsearch `_search` responses (`elastalert_status`, `secgen-events-*`) and alert-router `BENCHMARK` log lines |
| `benchmark_results_2.txt` | Filter-based path: `FILTER DETECTIONS` lines in the format of `/var/log/logstash/filter_detections.log`, plus alert-router `BENCHMARK` log lines |
| `analyse.py` | Per-stage and end-to-end latency, full distributions, and the indexed-vs-filter comparison. Python 3.6+, no dependencies. Run `python3 analyse.py` with no arguments for the modes and the measurement decisions |
| `show_runs.py` | Lists the distinct runs in a results file, to find the analysis window |
| `filter_to_es_json.py` | Converts the pipe-separated filter detections into the layout `analyse.py` reads |

The instrumentation that produced these timestamps is in this branch:
`modules/services/unix/logging/analysis_alert_action_server/files/filter_ls_conf.yml`
(Logstash timing fields and detection output), `logstash_filter.rb` (filter-based
detection), and the `BENCHMARK` lines in `alert_actioner/alert_router.rb`.

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

`describe` gives the same figures with exclusion counts and structural checks.
`compare benchmark_results_1.txt benchmark_results_2_es.txt` gives the reduction
and a bootstrap confidence interval for the difference in medians. Its detection
figures are valid with any window covering both runs. Take dispatch latency from
the per-file runs above, though: a single window spanning both runs attributes
each run's router lines to the other, which the tool warns about.

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
