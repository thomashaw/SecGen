# Probe whether whole-directory sweeps with an allowed reader are credited as
# reads of the goal file. Runs each sweep as the goal user from their home
# directory and prints its UTC window, for collect_detections.sh.
# Run with: guest_exec.rb --timeout 120 --script sweep_probe.sh <victim VMID>
set -u
U=challenger
probe() {
  echo "$1 start $(date -u +%FT%T.%3NZ)"
  su -s /bin/bash "$U" -c "cd ~ && $2" > /dev/null 2>&1
  echo "$1 end   $(date -u +%FT%T.%3NZ)"
  sleep 5
}
probe S01 'cat /etc/*'
probe S02 'find /etc -type f -exec cat {} +'
probe S03 'find /etc -type f -exec cat {} \;'
