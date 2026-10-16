# Single-clock latency check for the filter path, timed on the server only.
# Keeps one ssh connection to the victim open, records the server time just
# before and after each `cat /etc/shadow`, and pairs it with the detection that
# follows. detection - t_before is an upper bound on system call -> detection
# with no cross-host clock involved.
# Run as root on the analysis server, detached:
#   setsid nohup bash single_clock_test.sh [N] > single_clock.log 2>&1 &
# Every ssh call reads /dev/null so none of them can consume this script.
set -u
N=${1:-20}
D=/root/latency_v2
LOG=/var/log/logstash/filter_detections.log
CM=$D/cm.sock
OUT=$D/single_clock_$(date -u +%Y%m%dT%H%M%SZ).txt
SSH="ssh -o StrictHostKeyChecking=no -o ControlPath=$CM"
if ! sshpass -f "$D/cp" $SSH -o ControlMaster=yes -o ControlPersist=600 -fN challenger@172.16.0.3 < /dev/null > /dev/null 2>&1; then
  echo 'ssh master failed'; rm -f "$D/cp"; exit 1
fi
rm -f "$D/cp"
$SSH challenger@172.16.0.3 true < /dev/null
echo "# attempt|server_before|server_after|log_line" > "$OUT"
for i in $(seq 1 "$N"); do
  before_lines=$(wc -l < "$LOG")
  t0=$(date -u +%s.%N)
  $SSH challenger@172.16.0.3 'cat /etc/shadow > /dev/null' < /dev/null
  t1=$(date -u +%s.%N)
  sleep 5
  line=$(tail -n +"$((before_lines + 1))" "$LOG" | grep '/etc/shadow' | head -n 1)
  echo "$i|$t0|$t1|$line" >> "$OUT"
done
$SSH -O exit challenger@172.16.0.3 < /dev/null 2> /dev/null
echo "DONE output: $OUT"
