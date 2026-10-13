# Start the correctness run detached on the victim, so it outlives the guest
# agent's wait. $1 = label for the output files (before / after).
# Run with: guest_exec.rb --script start_run.sh <victim VMID> <label>
set -eu
D=/root/sscorrect
LABEL="$1"
cd "$D"
md5sum run_correctness_cases.sh
OUT="$D/correctness_cases_${LABEL}_$(hostname)_$(date -u +%Y%m%dT%H%M%SZ).csv"
setsid nohup env TARGET=/etc/shadow GOAL_USER=challenger OUT="$OUT" \
    bash "$D/run_correctness_cases.sh" > "$D/run_${LABEL}.log" 2>&1 < /dev/null &
echo "started pid $! at $(date -u +%FT%TZ)"
echo "cases csv: $OUT"
