#!/usr/bin/env bash
#
# StateSentry detection correctness test: case runner.
#
# Runs on the monitored (victim) VM, as root. Performs a fixed set of access
# variants against the goal file, each as the appropriate user, and records
# when each case ran and whether it SHOULD register as goal completion.
# Detections are collected separately on the analysis server and matched to
# these case windows by score_correctness.py.
#
#   sudo ./run_correctness_cases.sh                 # run all cases
#   sudo ./run_correctness_cases.sh --dry-run       # print the cases only
#
# Ground truth used throughout:
#   detect     the goal user read the CONTENTS of the real target file on this
#              host, by whatever route; a correct system registers progress.
#   no_detect  anything else (a decoy, a failed access, another account,
#              metadata only, an incidental read during a recursive search);
#              a correct system registers nothing.
#
# Run this in a scenario whose only goal is the read-file goal on TARGET (as
# for the latency benchmark): su/runuser produce PAM session events, which
# would also satisfy any access-account goal on GOAL_USER.

set -u

# ---------------------------------------------------------------- settings --
TARGET="${TARGET:-/etc/shadow}"          # file named in the read-file goal
GOAL_USER="${GOAL_USER:-challenger}"     # account named in the goal
OTHER_USER="${OTHER_USER:-sstest}"       # unprivileged account, created if absent
# Invoking processes the detection rule accepts. Copy this from the generated
# rule; it is used only to attribute misses, never to decide ground truth.
ALLOWED_READERS="${ALLOWED_READERS:-cat less more vim nano kate}"
REPS="${REPS:-3}"                        # repetitions of each case
GAP_S="${GAP_S:-5}"                      # pause between case windows
SETTLE_S="${SETTLE_S:-1}"                # margin added to each window's end
OUT="${OUT:-correctness_cases_$(hostname)_$(date -u +%Y%m%dT%H%M%SZ).csv}"
DRY_RUN=0
[ "${1:-}" = "--dry-run" ] && DRY_RUN=1

TARGET_DIR="$(dirname "$TARGET")"
TARGET_NAME="$(basename "$TARGET")"
GOAL_HOME="$(getent passwd "$GOAL_USER" 2>/dev/null | cut -d: -f6)"
GOAL_HOME="${GOAL_HOME:-/home/$GOAL_USER}"
# Scratch area for links and decoys. It must sit inside a directory Auditbeat
# watches (the goal paths' parent directories), or decoy and symlink cases are
# never logged and pass trivially.
WORK="$GOAL_HOME/.sscorrect"
# TARGET relative to GOAL_HOME, e.g. /home/challenger -> ../../etc/shadow
UP_FROM_HOME="$(printf '%s' "$GOAL_HOME" | sed 's#[^/][^/]*#..#g; s#^/##')"
REL_FROM_HOME="${UP_FROM_HOME}${TARGET}"

# ------------------------------------------------------------------ checks --
if [ "$DRY_RUN" -eq 0 ]; then
    [ "$(id -u)" -eq 0 ] || { echo "run as root" >&2; exit 1; }
    getent passwd "$GOAL_USER" >/dev/null || { echo "no user $GOAL_USER" >&2; exit 1; }
    su -s /bin/sh -c "test -r '$TARGET'" "$GOAL_USER" \
        || { echo "$GOAL_USER cannot read $TARGET; fix the scenario first" >&2; exit 1; }
fi

# ------------------------------------------------------------------- setup --
# Everything created here happens BEFORE the first case window, so any audit
# events it generates fall outside every window.
setup() {
    getent passwd "$OTHER_USER" >/dev/null || useradd -M -s /bin/sh "$OTHER_USER"
    rm -rf "$WORK"
    mkdir -p "$WORK/decoy$TARGET_DIR"
    echo "fake" > "$WORK/decoy$TARGET"                # decoy with the same path suffix
    ln -s "$TARGET" "$WORK/file_link"                 # symlink to the file
    ln -s "$TARGET_DIR" "$WORK/dir_link"              # symlink to its directory
    chown -R "$GOAL_USER" "$WORK"
    chmod -R a+rX "$WORK"
    sleep "$GAP_S"
}

# ------------------------------------------------------------------- cases --
# id | category | ground truth | run as | reader | description | command
# The reader column names the process that opens the file, for attributing
# misses to the invoking-process restriction; "-" where no read happens.
read -r -d '' CASES <<EOF || true
P01|path|detect|$GOAL_USER|cat|absolute path|cat $TARGET
P02|path|detect|$GOAL_USER|cat|relative path from the file's directory|cd $TARGET_DIR && cat $TARGET_NAME
P03|path|detect|$GOAL_USER|cat|parent traversal from home|cd $GOAL_HOME && cat $REL_FROM_HOME
P04|path|detect|$GOAL_USER|cat|dot segment|cat $TARGET_DIR/./$TARGET_NAME
P05|path|detect|$GOAL_USER|cat|repeated slashes|cat /$TARGET_DIR//$TARGET_NAME
P06|path|detect|$GOAL_USER|cat|traversal within absolute path|cat $TARGET_DIR/../$(basename "$TARGET_DIR")/$TARGET_NAME
P07|path|detect|$GOAL_USER|cat|symlink to the file|cat $WORK/file_link
P08|path|detect|$GOAL_USER|cat|working directory reached through a symlink|cd $WORK/dir_link && cat $TARGET_NAME
P09|path|detect|$GOAL_USER|bash|shell input redirection (shell opens the file)|cat < $TARGET
R01|reader|detect|$GOAL_USER|less|less|less $TARGET
R02|reader|detect|$GOAL_USER|more|more|more $TARGET
R03|reader|detect|$GOAL_USER|head|head|head $TARGET
R04|reader|detect|$GOAL_USER|tail|tail|tail $TARGET
R05|reader|detect|$GOAL_USER|tac|tac|tac $TARGET
R06|reader|detect|$GOAL_USER|nl|nl|nl $TARGET
R07|reader|detect|$GOAL_USER|sort|sort|sort $TARGET
R08|reader|detect|$GOAL_USER|grep|grep (single file)|grep -h : $TARGET
R09|reader|detect|$GOAL_USER|awk|awk|awk 1 $TARGET
R10|reader|detect|$GOAL_USER|sed|sed|sed -n p $TARGET
R11|reader|detect|$GOAL_USER|cut|cut|cut -d: -f1,2 $TARGET
R12|reader|detect|$GOAL_USER|strings|strings|strings $TARGET
R13|reader|detect|$GOAL_USER|base64|base64|base64 $TARGET
R14|reader|detect|$GOAL_USER|od|od|od -c $TARGET
R15|reader|detect|$GOAL_USER|xxd|xxd|xxd $TARGET
R16|reader|detect|$GOAL_USER|dd|dd|dd if=$TARGET status=none
R17|reader|detect|$GOAL_USER|cp|cp to a readable copy|cp $TARGET $WORK/copy_\$\$ && rm -f $WORK/copy_\$\$
R18|reader|detect|$GOAL_USER|python3|python3|python3 -c 'print(open("$TARGET").read())'
R19|reader|detect|$GOAL_USER|perl|perl|perl -ne print $TARGET
R20|reader|detect|$GOAL_USER|vim|vim (batch mode)|vim -es -c 'q!' $TARGET
N01|negative|no_detect|$GOAL_USER|cat|decoy, absolute path|cat $WORK/decoy$TARGET
N02|negative|no_detect|$GOAL_USER|cat|decoy, relative path from decoy directory|cd $WORK/decoy$TARGET_DIR && cat $TARGET_NAME
N03|negative|no_detect|$GOAL_USER|cat|decoy, relative path with directory|cd $WORK/decoy && cat ${TARGET_DIR#/}/$TARGET_NAME
N04|negative|no_detect|$OTHER_USER|cat|failed access, account without permission|cat $TARGET
N05|negative|no_detect|root|cat|successful access by a different account (root)|cat $TARGET
N06|negative|no_detect|$GOAL_USER|-|metadata only (stat)|stat $TARGET
N07|negative|no_detect|$GOAL_USER|-|metadata only (ls -l)|ls -l $TARGET
N08|negative|no_detect|$GOAL_USER|-|filesystem search by name (find)|find $TARGET_DIR -name $TARGET_NAME
N09|negative|no_detect|$GOAL_USER|grep|incidental read during recursive search|grep -rs zzzz_no_match $TARGET_DIR
N10|negative|no_detect|$GOAL_USER|cat|different file in the same directory|cat $TARGET_DIR/passwd
EOF

now_ms() { date -u +%Y-%m-%dT%H:%M:%S.%3NZ; }

listed() {
    case " $ALLOWED_READERS " in *" $1 "*) echo yes ;; *) echo no ;; esac
}

run_as() {  # user, command
    if [ "$1" = "root" ]; then
        bash -c "$2"
    else
        su -s /bin/bash -c "$2" "$1"
    fi
}

csv_field() { printf '"%s"' "$(printf '%s' "$1" | sed 's/"/""/g')"; }

# --------------------------------------------------------------------- run --
if [ "$DRY_RUN" -eq 1 ]; then
    printf '%s\n' "$CASES" | while IFS='|' read -r id cat truth user reader desc cmd; do
        printf '%-4s %-9s %-10s %-10s listed=%-3s  %s\n' \
            "$id" "$cat" "$truth" "$user" "$( [ "$reader" = "-" ] && echo n/a || listed "$reader")" "$cmd"
    done
    exit 0
fi

echo "case_id,category,ground_truth,run_as,reader,reader_listed,description,command,rep,exit_code,skipped,start_utc,end_utc" > "$OUT"
setup

total=$(printf '%s\n' "$CASES" | wc -l)
for rep in $(seq 1 "$REPS"); do
    n=0
    printf '%s\n' "$CASES" | while IFS='|' read -r id cat truth user reader desc cmd; do
        n=$((n + 1))
        tool=${cmd%% *}
        skipped=no
        [ "$tool" = "cd" ] && tool=$(printf '%s' "$cmd" | sed 's/.*&& *//; s/ .*//')
        command -v "$tool" >/dev/null 2>&1 || skipped=yes
        lst=$( [ "$reader" = "-" ] && echo n/a || listed "$reader")
        if [ "$skipped" = yes ]; then
            start=$(now_ms); end=$start; rc=-1
        else
            start=$(now_ms)
            run_as "$user" "$cmd" </dev/null >/dev/null 2>&1
            rc=$?
            sleep "$SETTLE_S"
            end=$(now_ms)
        fi
        printf '%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n' \
            "$id" "$cat" "$truth" "$user" "$reader" "$lst" \
            "$(csv_field "$desc")" "$(csv_field "$cmd")" \
            "$rep" "$rc" "$skipped" "$start" "$end" >> "$OUT"
        printf 'rep %s/%s  case %2s/%s  %-4s rc=%-3s %s\n' "$rep" "$REPS" "$n" "$total" "$id" "$rc" "$desc"
        sleep "$GAP_S"
    done
done

rm -rf "$WORK"
echo "wrote $OUT"
echo "Now collect the filter detections covering this run (same method as"
echo "benchmark_filter.sh's FILTER DETECTIONS section) and run score_correctness.py."
