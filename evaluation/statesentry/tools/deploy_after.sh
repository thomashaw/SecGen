# Deploy the 'after' detection to the analysis server. The current files are
# copied to *.before_20261013 first (to restore: copy them back and restart).
# Expects /root/sscorrect_after/{combined_path.rb,logstash_filter.rb}, uploaded
# with guest_exec.rb --upload.
# Run with: guest_exec.rb --timeout 180 --script deploy_after.sh <server VMID>
set -eu
cd /etc/logstash
[ -e combined_path.rb.before_20261013 ] || cp -p combined_path.rb combined_path.rb.before_20261013
[ -e filter_detection.rb.before_20261013 ] || cp -p filter_detection.rb filter_detection.rb.before_20261013
install -m 644 -o root -g root /root/sscorrect_after/combined_path.rb combined_path.rb
install -m 644 -o root -g root /root/sscorrect_after/logstash_filter.rb filter_detection.rb
md5sum combined_path.rb filter_detection.rb *.before_20261013
LOG=/var/log/logstash/logstash-plain.log
FROM=$(( $(wc -l < "$LOG") + 1 ))
systemctl restart logstash
for i in $(seq 1 30); do
  sleep 5
  if ss -ltn | grep -q ':5044 '; then echo "logstash listening on 5044 after $((i * 5))s"; break; fi
done
systemctl is-active logstash
echo "--- log lines since restart mentioning errors:"
tail -n +"$FROM" "$LOG" | grep -iE 'error|exception' | tail -5 || true
