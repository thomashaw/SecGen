# StateSentry detection: issues found in validation, and their fixes

This file records each defect that the pre-run validation of the detection-correctness
test exposed (13 October 2026, see `../correctness/prerun_validation_2026-10-13.md`),
together with the commit that fixed it on branch `dessert_2026_revision` and the evidence
that the fix works. Issues that are a deliberate consequence of the design are listed
separately as limitations, with the reason they are retained.

Both versions of the detection are kept so that either can be reproduced:

| Directory | Contents |
|---|---|
| `before/` | `logstash_filter.rb`, `combined_path.rb` and `rules.rb` from `8de3e6c9e`, the code behind the latency benchmark (`../latency/benchmark_results_2.txt`), and the Auditbeat and ElastAlert rules it generates for the readable_shadow goal |
| `after/` | The same files with the fixes below applied, and the rules they generate (with `read_by` set to `challenger`) |

The deployable copies live in the module tree
(`modules/services/unix/logging/analysis_alert_action_server/files/logstash_filter.rb`,
`modules/services/unix/logging/logstash/files/combined_path.rb`, `lib/helpers/rules.rb`)
and match `after/`. `generate_rules.rb` regenerates either rule set, and
`test_combined_path.rb` tests either version of `combined_path.rb`
(`ruby test_combined_path.rb` or `ruby test_combined_path.rb before`).

## Design constraint

The victim's audit rules watch only the top-level directory of each goal path (for
example `-w /etc/ -p r`), and the analysis server decides which file was read. This is
deliberate: a watch on the goal file itself would name it in the victim's audit
configuration, and a student who reads that configuration would learn the objective (a
watch on an SSH `authorized_keys` file, for instance, would reveal the intended route).
Every fix below therefore works on the analysis server and leaves the victim's audit rules
unchanged; `after/readable_shadow_rules.txt` and `before/readable_shadow_rules.txt` contain
the same Auditbeat rule.

## Issues and fixes

### Detection

| # | Issue | How validation found it | Fix | Commit | Evidence |
|---|---|---|---|---|---|
| D1 | `combined_path` was always built as `cwd + "/" + file.path`, even for an absolute path, so a process whose working directory is `/` produced `//etc/shadow` and was never detected | Static analysis of `combined_path.rb`; simulation with `combined_path_sim.rb` | An absolute `file.path` is used as it is; a relative one is joined to the working directory | `6cb518ffe` | `test_combined_path.rb`: "P01 from cwd /" fails on `before`, passes on `after` |
| D2 | `remove_duplicates` kept only the text between the first and second `//`, so `//etc//shadow` became `//etc` (missed) | Simulation (case P05) | The path is rebuilt segment by segment; empty segments are dropped, so `//` can never survive | `6cb518ffe` | Test "P05 repeated slashes" fails on `before`, passes on `after`; every `after` test asserts no `//` in the result |
| D3 | `.` segments were not removed, so `/etc/./shadow` was missed | Simulation (case P04) | `.` segments are dropped | `6cb518ffe` | Test "P04 dot segment" |
| D4 | Decoy reads whose path contained `//` normalised to the goal path: from inside a decoy tree, `cat .//etc/shadow` and `cat x//etc/shadow` were predicted false detections | Simulation; confirmed locally that the kernel opens the decoy in both | Fixed by D2's normalisation; the decoy path now normalises to its real location | `6cb518ffe` | Tests "N11 decoy .//" and "N12 decoy x//" fail on `before`, pass on `after`; new correctness cases N11 and N12 (`df0586a49`) |
| D5 | `..` above the root was mishandled, and no `combined_path` was set for an absolute `file.path` without a working directory | Unit tests written for D1 to D4 | `..` at the root stays at the root; an absolute path needs no working directory | `6cb518ffe` | Tests ".. above root" and "absolute path without cwd" |
| D6 | The reader allow-list named one full path per program, but auditd records the resolved executable, which differs by base. On the Debian 10 victim `less` is `/bin/less` while the list had `/usr/bin/less`, so reads with `less` were never detected | `readlink -f` of each reader on the victim | Each reader is accepted under both `/bin` and `/usr/bin`, in the Logstash filter and in the generated ElastAlert rule (from one list, `Rules::READ_EXECUTABLES`) | `bcdf1885d` | `diff before/readable_shadow_rules.txt after/readable_shadow_rules.txt`; correctness case R01 |
| D7 | The generated ElastAlert query scoped only its first reader to `process.executable` (`process.executable: "/bin/cat" OR "/usr/bin/vim.basic" ...`), so the other paths matched any field | Static analysis of `rules.rb` | The terms are grouped: `process.executable: ("/bin/cat" OR ...)` | `bcdf1885d` | Same diff |
| D8 | A `read_file` goal named only the file, so any account reading it registered progress for the student: root reading `/etc/shadow` would complete the goal | Static analysis (no user condition in the filter or the rule); correctness case N05 | `read_file` goals take an optional `read_by` (mirroring `modify_file`'s `written_by`); the ElastAlert rule then also requires `user.name` to match, and experiment_aaa's filter requires `challenger` for readable_shadow. Goals without `read_by` behave as before | `9f7c0297d` | All 442 module and 366 scenario XML files validate against the changed XSDs; `after/readable_shadow_rules.txt`; correctness case N05 |

### Correctness test

| # | Issue | Fix | Commit |
|---|---|---|---|
| T1 | The runner's reader list (`cat less more head tail`) did not match the rule's | Default changed to `cat less more vim nano kate` | `6bde64d89` |
| T2 | Decoys and links were created in `/tmp`, which is not audited, so the decoy cases would have passed trivially | Scratch area moved to `$GOAL_HOME/.sscorrect` (under the watched `/home/`) | `6bde64d89` |
| T3 | Cases inherited the runner's working directory (`/` under the QEMU guest agent), which, through D1, would have turned every absolute-path read into a miss; the latency benchmark had read the file over SSH, from `/home/challenger` | Each case runs from its user's home directory, recorded in the cases CSV | `cc831e90f` |
| T4 | Decoy forms that defeat the original normalisation were not in the suite | Cases N11 to N13 added (42 cases in all) | `df0586a49` |
| T5 | The scorer counted any alert in a case window, including alerts for the scenario's other goals | `--goal` option counts only the goal under test | `df0586a49` |
| T6 | Documentation described the audit watch as the goal's parent directory (it is the top-level directory) and named `benchmark.sh` (the script is `benchmark_filter.sh`) | Corrected | `cc831e90f`, `df0586a49` |
| T7 | Case N04 is labelled "failed access, account without permission", but the goal's vulnerability makes `/etc/shadow` mode 644, so the read by `sstest` succeeds (exit 0, `auditd.result` success) and was detected by `before`. On this scenario N04 tests a successful read by a second unprivileged account, as N05 does for root; a failed read of the goal file cannot be produced while the vulnerability is present | Not changed, so that the before and after runs use the same runner; the label is to be read as above, and a failed-access case needs a goal file the test account cannot read | none |

## Limitations retained by design

| # | Limitation | Why it is retained |
|---|---|---|
| L1 | Paths are normalised lexically, so symbolic links are not resolved. A read through a link to the goal file is missed (case P07). A decoy reached through a link followed by `..` normalises to the goal path and is credited (case N13, detected in every repetition): a student can create a link and a file of their own and have a read of that file recorded as a read of the goal, which is a way of gaining credit without reading the goal file. Not resolving links is therefore not a defence against symlink attacks; the read through a link that it misses was permitted by the kernel anyway | Resolving links needs the monitored host's file system, which the analysis server cannot see, and a watch on the goal file's inode would name it in the victim's audit rules, which the design constraint above rules out. A possible fix that keeps the directory watch, not yet tested: the audit record of each opened file already carries its device and inode, so the analysis server could compare them with the goal file's, recorded at provisioning and kept on the server. The goal's inode changes when the file is replaced (a password change rewrites `/etc/shadow`), so it would have to be refreshed |
| L2 | Only the listed reader programs count, so reads with other programs (`head`, `python3`, `cp`, shell redirection, ...) are missed (P09, R03 to R19) | The restriction avoids crediting incidental reads by system processes and tools such as recursive `grep`; its cost is reported as a false-negative exposure |
| L3 | What is audited depends on the scenario's whole goal set, because each goal adds a watch on its top-level directory | Follows from the design constraint |

## Measured results

The correctness test (42 cases, 3 repetitions each, `/etc/shadow` read as `challenger` on
`tom-noekea-2-experiment-aaa-hackme-desktop`) was run on 13 October 2026 with `before`
deployed on the analysis server, and again with `after` deployed. The runner was the same
in both runs (md5 `ba554634ff2e6d624b0c15c2509fefb8`). Cases CSVs, filter detections, the
scored CSVs and the scorer's reports are in `../correctness/results/`
(`*_before_20261013T091827Z*`, `*_after_20261013T093759Z*`). R12 was skipped in both runs
(`strings` is not installed on the victim), so 41 cases were scored. No case gave
different outcomes across its repetitions in either run.

| | `before` | `after` |
|---|---|---|
| Should detect (28 cases): detected | 7 | 10 |
| Missed, reader not in the allow-list (P09, R03 to R19 except R12) | 17 | 17 |
| Missed, other | 4: P04, P05, P07, R01 | 1: P07 |
| Should not detect (13 cases): false detections | 5: N04, N05, N11, N12, N13 | 1: N13 |

Per issue:

| # | Case(s) | `before` | `after` |
|---|---|---|---|
| D2 | P05 | missed 0/3 | detected 3/3 |
| D3 | P04 | missed 0/3 | detected 3/3 |
| D4 | N11, N12 | false detection 3/3 each | silent 0/3 each |
| D6 | R01 (`less`, `/bin/less`) | missed 0/3 | detected 3/3 |
| D8 | N05 (root), N04 (`sstest`) | false detection 3/3 each | silent 0/3 each |
| D8 | P01 to P06, P08, R01, R02, R20 (`challenger` via `su`) | | detected 3/3 each |
| L1 | P07, N13 | missed, false detection | unchanged, as expected |

D1 is not exercised by the runner since T3 (cases run from the home directory); it is
covered by `test_combined_path.rb`. D5 and D7 are covered by the unit tests and the rule
diff respectively.

## Open items

- **`read_by` field (resolved by the after run).** `read_by` is matched against
  `user.name`. In the after run every read by `challenger` was detected, and reads by root
  and `sstest` were not. The runner starts each case with `su` from a process whose login
  uid is not `challenger`, so a match on the login uid could not have fired: `user.name`
  carries the uid of the reading process, as assumed. This was inferred from detections;
  no raw event was stored (the analysis server keeps only the detections file).
- **`read_by` for module goals.** A scenario cannot yet set `read_by` on a goal that a
  module declares (readable_shadow's goal comes from its metadata). experiment_aaa's
  filter is written by hand, so the evaluation is unaffected, but generated ElastAlert
  rules for module goals have no `read_by` until such a mechanism exists.
