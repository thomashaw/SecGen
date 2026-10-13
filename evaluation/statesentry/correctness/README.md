# Detection correctness test

A scripted, repeatable test of whether StateSentry's read-file detection registers
progress when it should, and only then. It measures detection accuracy over a fixed
set of access variants, as reported in Section V.B of the paper.

## What it tests

There are 42 cases. Each one has a **ground truth** that does not depend on how the
detection rule is written:

- **detect**: the goal user read the *contents* of the real target file on the
  monitored host, by whatever route. A correct system registers progress.
- **no_detect**: anything else. A correct system registers nothing.

| Group | Cases | Ground truth | What they probe |
|---|---|---|---|
| Path variants (P01–P09) | absolute, relative, `../` traversal, `./`, `//`, symlink to the file, working directory reached through a symlink, shell redirection | detect | path normalisation (`combined_path`) |
| Readers (R01–R20) | `less`, `head`, `tail`, `tac`, `sort`, `grep`, `awk`, `sed`, `base64`, `dd`, `cp`, `python3`, `perl`, `vim`, … | detect | the invoking-process restriction |
| Negatives (N01–N13) | decoy file with the same path suffix (absolute and two relative forms), failed access, another account (root), `stat`, `ls -l`, `find`, recursive `grep`, a different file, and three decoy paths added after pre-run validation (`.//etc/shadow`, `x//etc/shadow`, and a symbolic link followed by `..`) | no_detect | false detections, including the decoy-path attack |

Misses on readers the rule does not accept are **expected**: they are the known
false-negative exposure of the invoking-process restriction (Section V.B). The
scorer attributes each miss using `ALLOWED_READERS`, and flags any miss or false
detection that the restriction does not explain.

## Running it

1. **Prepare a scenario** whose only goal is the read-file goal on the target, for
   example the latency benchmark scenario. `su` creates PAM session events, which
   would also satisfy any access-account goal for the same user.
2. **On the monitored VM, as root**, set `ALLOWED_READERS` to the process list in the
   generated detection rule, then run the cases:

   ```
   sudo ALLOWED_READERS="cat less more vim nano kate" \
        TARGET=/etc/shadow GOAL_USER=challenger \
        ./run_correctness_cases.sh
   ```

   The scratch area for decoys and symlinks is `$GOAL_HOME/.sscorrect`. It must sit
   inside a directory Auditbeat watches (the top-level directory of each goal path;
   `/home/` is watched in experiment_aaa because of its `/home/challenger` goal), or
   the decoy and symlink cases are never logged and pass trivially.

   Each case starts in its user's home directory, as the SSH-triggered latency
   benchmark did. The detection's `combined_path` is built from the process cwd, so
   an inherited cwd of `/` (for example under the QEMU guest agent) would turn
   `/etc/shadow` into `//etc/shadow`, and every absolute-path case would be missed.
   The cwd is recorded in the CSV.

   `--dry-run` prints the cases without running anything. Each case runs `REPS`
   times (default 3), with a `GAP_S`-second pause (default 5). The full run takes
   about 12 minutes and writes `correctness_cases_<host>_<time>.csv`.
3. **On the analysis server**, collect the filter detections covering the run.
   Use the same method as the `FILTER DETECTIONS` section of `../latency/benchmark_filter.sh`, which
   gives one `audit|recv|norm|detect|path|host` line per detection.
4. **Score it** anywhere with Python 3.6 or later:

   ```
   python3 score_correctness.py correctness_cases_<host>_<time>.csv detections.txt \
       --goal readable_shadow-rf-0 --out results.csv
   ```

   The scorer prints a per-case table, a summary and a draft sentence for the paper.
   It exits non-zero if anything needs investigating: a false detection, a miss not
   explained by the reader restriction, or a case that behaved inconsistently across
   repetitions.

## How cases are matched to detections

The runner records each case's start and end time on the monitored VM. Each
detection carries the audit timestamp of its system call, taken from the same
clock. A detection belongs to the case whose window, plus 200 ms either side,
contains that timestamp. Detections outside the run are ignored. Detections inside
the run but outside every window are reported, since they usually come from setup
activity.

## Files

- `run_correctness_cases.sh`: case runner (bash, run on the monitored VM)
- `score_correctness.py`: scorer (no third-party dependencies)
- `correctness_cases_*.csv`, `detections.txt`, `results.csv`: raw inputs and
  outputs of the reported run, published alongside the scripts
