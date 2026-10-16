# Clock offset readings, 16 October 2026

The offsets are victim − analysis server, measured with `../icmp_offset.rb` (ICMP
timestamp requests, 30 samples), run as root on each analysis server against its
victim (172.16.0.3).

The error is half the best sample's round trip plus the 1 ms resolution of ICMP
timestamps. Neither pair's clocks are synchronised by NTP (`timedatectl`:
`NTPSynchronized=no` on both servers).

| Pair | Server → victim | Time (UTC) | Best-sample offset | All samples min / median / max | Best RTT |
|---|---|---|---|---|---|
| New pair (re-run, 16 Oct) | 732895409 → 944278675 | about 14:17 | −5.9 ± 1.3 ms | −6.3 / −5.9 / −5.4 ms | 0.598 ms |
| New pair | 732895409 → 944278675 | 15:05:24 | −5.6 ± 1.3 ms | −6.3 / −5.7 / −5.3 ms | 0.682 ms |
| Old pair (31 July runs) | 869458117 → 326121316 | about 14:18 | −488.7 ± 1.3 ms | −489.3 / −488.8 / −488.3 ms | 0.632 ms |
| Old pair | 869458117 → 326121316 | 15:05:45 | −489.2 ± 1.4 ms | −489.8 / −488.8 / −488.3 ms | 0.814 ms |

The two earlier readings were taken before the readings were logged, so their
times come from the upload time of the script. Negative means the victim's clock
is behind. An interval that starts at the victim's audit timestamp therefore
reads that much too long.

The corrected analyses use −5.9 ms for the new pair and −488.7 ms for the old
pair, the first reading on each. The readings on each pair agree to within
0.5 ms, which is less than their error.

The old pair's offset was measured 77 days after the 31 July runs, so applying
it to those runs assumes it has not changed. Two things support that:
- Its 31 July collection-stage spread (IQR 7.2 ms) matches the new pair's
  (IQR 6.2 ms), so the 31 July figures look shifted rather than delayed.
- Once corrected, the 31 July filter run gives a median of 12.3 ms from
  system call to detection, against 10.3 ms on the new pair.

## Independent check on one clock (`single_clock_<time>.txt`)

`single_clock_test.sh` (kept on the server in `/root/latency_v2/`; the copy here
is the one that ran) times the filter path on the server's clock alone:
- one ssh connection to the victim stays open, as `challenger`;
- the server records its time just before and just after each
  `cat /etc/shadow`;
- each attempt is paired with the detection that follows.

`single_clock_analyse.py` turns that into an upper and a lower bound on the
time from system call to detection, with no cross-host clock involved. It also
bounds the victim − server offset from the audit timestamps.

Result, 20 attempts (all detected), 15:02 UTC, filter path with the final code
deployed:
- upper bound on system call → detection: min 17.3, median 25.7, max 41.9 ms;
  this includes the ssh round trip (min 18.8, median 23.9, max 40.8 ms), since
  the `cat` runs somewhere inside it;
- victim − server offset from the audit timestamps: between −10.6 and +4.3 ms,
  which agrees with the ICMP reading of −5.9 ms.
