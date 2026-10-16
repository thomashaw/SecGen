# Read-only: progress of a correctness run on the victim. $1 = label.
# Run with: guest_exec.rb --script run_progress.sh <victim VMID> <label>
L=/root/sscorrect/run_$1.log
tail -2 "$L"
grep -c '^rep ' "$L"
grep -q '^wrote ' "$L" && echo RUN_FINISHED
