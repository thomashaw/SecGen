#!/bin/bash
#
# StateSentry latency benchmark: indexed path (Logstash -> Elasticsearch ->
# ElastAlert). Produced benchmark_results_1.txt. Run on the analysis server.
#
# As run for the paper, except that the client password is now supplied by
# the environment rather than hard-coded:
#   CLIENT_PASS=... ./benchmark_indexed.sh

SERVER_IP="172.16.0.4"
CLIENT_IP="172.16.0.3"
CLIENT_USER="challenger"
CLIENT_PASS="${CLIENT_PASS:?set CLIENT_PASS to the password of CLIENT_USER on the client}"
REPETITIONS=50
DELAY=30
ALERT_ROUTER="/opt/alert_actioner/alert_router.rb"
OUTPUT_FILE="/tmp/benchmark_results.txt"

echo "StateSentry Pipeline Benchmark"
echo "Repetitions: $REPETITIONS"
echo "Delay between attempts: ${DELAY}s"
echo "Started: $(date -u +%Y-%m-%dT%H:%M:%SZ)"
echo "========================================"

for i in $(seq 1 $REPETITIONS); do
  echo "--- Attempt $i of $REPETITIONS ($(date -u +%Y-%m-%dT%H:%M:%SZ)) ---"

  # Reset the alert router so it will fire again
  echo "  Resetting alert router..."
  ruby $ALERT_ROUTER reset --all

  # Small pause to ensure reset is committed
  sleep 2

  # Trigger cat /etc/shadow on the client via sshpass
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
  echo "=== ELASTALERT STATUS (all goal alerts) ==="
  curl -s "http://${SERVER_IP}:9200/elastalert_status/_search?size=1000" \
    -H "Content-Type: application/json" \
    -d '{"sort": [{"@timestamp": "asc"}], "query": {"match_all": {}}}'

  echo ""
  echo "=== SECGEN EVENTS (goal paths only) ==="
  curl -s "http://${SERVER_IP}:9200/secgen-events-*/_search?size=1000" \
    -H "Content-Type: application/json" \
    -d '{
      "sort": [{"@timestamp": "asc"}],
      "query": {
        "terms": {
          "combined_path": ["/etc/shadow", "/home/challenger/.top_secret_file"]
        }
      }
    }'

  echo ""
  echo "=== ALERT ROUTER BENCHMARK LINES ==="
  grep "BENCHMARK" /opt/alert_actioner/alert-router.log

} > $OUTPUT_FILE

echo "Benchmark data written to $OUTPUT_FILE"
echo "Finished: $(date -u +%Y-%m-%dT%H:%M:%SZ)"
