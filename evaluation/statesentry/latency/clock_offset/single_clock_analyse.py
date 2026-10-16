#!/usr/bin/env python3
"""
Analyse the output of single_clock_test.sh: a filter-path latency check timed
on the analysis server's clock alone.

Each line is  attempt|server_before|server_after|filter_detections.log line,
where server_before and server_after (epoch seconds) bracket one
`cat /etc/shadow` run on the victim over an already-open ssh connection, and the
log line is  audit|ls_recv|ls_norm|detection|path|alert  (audit on the victim's
clock, the rest on the server's).

The system call happens between server_before and server_after, so on the
server's clock alone:

    detection - server_after  <=  system call -> detection  <=  detection - server_before

and, because the audit timestamp marks that same system call on the victim's
clock, the victim-minus-server offset lies in

    [audit - server_after, audit - server_before]

which is an independent check on icmp_offset.rb. No clock correction is applied
anywhere. Python 3.6+, no dependencies.

    python3 single_clock_analyse.py single_clock_<time>.txt
"""

import sys
from datetime import datetime, timezone


def iso_ms(s):
    s = s.strip().rstrip("Z")
    if "." in s:
        base, frac = s.split(".", 1)
    else:
        base, frac = s, "0"
    dt = datetime.strptime(base, "%Y-%m-%dT%H:%M:%S").replace(tzinfo=timezone.utc)
    return dt.timestamp() * 1000.0 + float("0." + frac) * 1000.0


def median(v):
    v = sorted(v)
    n = len(v)
    return v[n // 2] if n % 2 else (v[n // 2 - 1] + v[n // 2]) / 2.0


def main(path):
    rows = []
    missing = 0
    for line in open(path):
        if line.startswith("#") or not line.strip():
            continue
        parts = line.rstrip("\n").split("|")
        if len(parts) < 8:
            missing += 1
            continue
        t0, t1 = float(parts[1]) * 1000.0, float(parts[2]) * 1000.0
        audit, det = iso_ms(parts[3]), iso_ms(parts[6])
        rows.append((int(parts[0]), t1 - t0, det - t1, det - t0, audit - t1, audit - t0))
    if not rows:
        sys.exit("no paired attempts in %s" % path)

    print("attempt  ssh_rtt  lower_bound  upper_bound  offset_low  offset_high   (ms)")
    for r in rows:
        print("%7d %8.1f %12.1f %12.1f %11.1f %11.1f" % r)
    print("")
    print("paired attempts: %d, attempts with no detection line: %d" % (len(rows), missing))
    for idx, name in ((1, "ssh round trip (server_after - server_before)"),
                      (2, "lower bound on system call -> detection"),
                      (3, "upper bound on system call -> detection")):
        v = [r[idx] for r in rows]
        print("%-48s min %7.1f  median %7.1f  max %7.1f" % (name, min(v), median(v), max(v)))
    lo = max(r[4] for r in rows)
    hi = min(r[5] for r in rows)
    print("victim - server offset, intersection over attempts: [%.1f, %.1f] ms" % (lo, hi))


if __name__ == "__main__":
    if len(sys.argv) != 2:
        sys.exit(__doc__)
    main(sys.argv[1])
