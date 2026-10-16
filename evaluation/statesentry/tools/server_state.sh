# Read-only: report which detection code the analysis server is running.
# Run with: guest_exec.rb --script server_state.sh <analysis server VMID>
md5sum /etc/logstash/combined_path.rb /etc/logstash/filter_detection.rb
grep -rln 'combined_path.rb\|filter_detection.rb' /etc/logstash/ 2>/dev/null
systemctl is-active logstash
wc -l /var/log/logstash/filter_detections.log
tail -1 /var/log/logstash/filter_detections.log
date -u +%FT%TZ
