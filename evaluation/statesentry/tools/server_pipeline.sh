# Read-only: where the analysis server's Logstash pipeline sends events.
# Run with: guest_exec.rb --script server_pipeline.sh <analysis server VMID>
ls /etc/logstash/conf.d/
grep -n -A8 '^output' /etc/logstash/conf.d/*.conf
echo "--- elasticsearch"
curl -s -m 5 'localhost:9200/_cat/indices?v' || echo "no elasticsearch on localhost:9200"
