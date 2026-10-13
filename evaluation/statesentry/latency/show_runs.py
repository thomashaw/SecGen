#!/usr/bin/env python3
"""Print audit-event timestamps bucketed by 10 minutes, to reveal how many
distinct benchmark runs a results file contains.

  python3 show_runs.py benchmark_results_1.txt
"""
import sys, datetime, collections
import analyse as A

for path in sys.argv[1:]:
    data = A.load(path)
    docs = data["alerts"] or data["events"]
    stamps = []
    for _i, d in docs:
        ms = A.to_epoch_ms(A.pick(d, "audit"))
        instr = A.pick(d, "ls_recv") is not None
        if ms:
            stamps.append((ms, instr))
    stamps.sort()
    print("")
    print("%s: %d document(s) with an audit timestamp" % (path, len(stamps)))
    if not stamps:
        continue

    buckets = collections.OrderedDict()
    for ms, instr in stamps:
        key = int(ms // 600000) * 600000
        b = buckets.setdefault(key, [0, 0])
        b[0] += 1
        if instr:
            b[1] += 1
    print("  %-22s %6s %14s" % ("10-MIN BUCKET (UTC)", "DOCS", "INSTRUMENTED"))
    for key in sorted(buckets):
        t = datetime.datetime.fromtimestamp(key / 1000.0, datetime.timezone.utc)
        n, i = buckets[key]
        print("  %-22s %6d %14d %s"
              % (t.strftime("%Y-%m-%d %H:%M"), n, i, "#" * min(n, 40)))

    # gaps longer than 5 minutes suggest a run boundary
    print("")
    print("  Gaps over 5 minutes between consecutive events (run boundaries):")
    prev = None
    runs = [[stamps[0][0], stamps[0][0], 1]]
    for ms, _instr in stamps[1:]:
        if prev is not None and ms - prev > 300000:
            t0 = datetime.datetime.fromtimestamp(prev / 1000.0, datetime.timezone.utc)
            t1 = datetime.datetime.fromtimestamp(ms / 1000.0, datetime.timezone.utc)
            print("    %s -> %s  (%.1f min)"
                  % (t0.strftime("%H:%M:%S"), t1.strftime("%H:%M:%S"),
                     (ms - prev) / 60000.0))
            runs.append([ms, ms, 1])
        else:
            runs[-1][1] = ms
            runs[-1][2] += 1
        prev = ms

    print("")
    print("  Candidate runs:")
    for start, end, n in runs:
        t0 = datetime.datetime.fromtimestamp(start / 1000.0, datetime.timezone.utc)
        t1 = datetime.datetime.fromtimestamp(end / 1000.0, datetime.timezone.utc)
        print("    %s .. %s  n=%d  (%.1f min)  spacing %.1f s"
              % (t0.strftime("%Y-%m-%dT%H:%M:%SZ"), t1.strftime("%H:%M:%SZ"),
                 n, (end - start) / 60000.0,
                 (end - start) / 1000.0 / max(n - 1, 1)))
        print("      --window-start %s --window-end %s"
              % (t0.strftime("%Y-%m-%dT%H:%M:%SZ"),
                 datetime.datetime.fromtimestamp((end + 60000) / 1000.0,
                                                 datetime.timezone.utc)
                 .strftime("%Y-%m-%dT%H:%M:%SZ")))
