#!/bin/bash
#
# StateSentry latency benchmark: filter-based path (detection inside
# Logstash, see filter_ls_conf.yml and logstash_filter.rb). Produced
# benchmark_results_2.txt. Run on the analysis server.
#
# As run for the paper, except that the client password is now supplied by
# the environment rather than hard-coded:
#   CLIENT_PASS=... ./benchmark_filter.sh

SERVER_IP="172.16.0.4"
CLIENT_IP="172.16.0.3"
CLIENT_USER="challenger"
CLIENT_PASS="${CLIENT_PASS:?set CLIENT_PASS to the password of CLIENT_USER on the client}"
REPETITIONS=50
DELAY=30
ALERT_ROUTER="/opt/alert_actioner/alert_router.rb"
DETECTION_LOG="/var/log/logstash/filter_detections.log"
OUTPUT_FILE="/tmp/benchmark_filter_results.txt"

echo "StateSentry Filter-Based Detection Benchmark"
echo "Repetitions: $REPETITIONS"
echo "Delay between attempts: ${DELAY}s"
echo "Started: $(date -u +%Y-%m-%dT%H:%M:%SZ)"
echo "========================================"

# Clear the detection log so we only capture this run
> $DETECTION_LOG

for i in $(seq 1 $REPETITIONS); do
  echo "--- Attempt $i of $REPETITIONS ($(date -u +%Y-%m-%dT%H:%M:%SZ)) ---"

  echo "  Resetting alert router..."
  sudo -u aaa_admin ruby $ALERT_ROUTER reset --all

  sleep 2

  echo "  Triggering cat /etc/shadow on client..."
  sshpass -p "$CLIENT_PASS" ssh -o StrictHostKeyChecking=no \
    ${CLIENT_USER}@${CLIENT_IP} "cat /etc/shadow" > /dev/null 2>&1

  echo "  Waiting ${DELAY}s for pipeline to propagate..."
  sleep $DELAY

  echo "  Done."
done

echo ""
echo "========================================"
echo "Loop complete. Collecting benchmark data..."
echo "========================================"

{
  echo "=== FILTER DETECTIONS ==="
  cat $DETECTION_LOG

  echo ""
  echo "=== ALERT ROUTER BENCHMARK LINES ==="
  grep "BENCHMARK" /opt/alert_actioner/alert-router.log

} > $OUTPUT_FILE

echo "Benchmark data written to $OUTPUT_FILE"
echo "Finished: $(date -u +%Y-%m-%dT%H:%M:%SZ)"
