# Read-only: print the filter detections whose audit timestamp (column 1, the
# victim's clock) falls in [$1, $2], in the format of benchmark_filter.sh's
# FILTER DETECTIONS section. Unlike benchmark_filter.sh, the log is not
# truncated. ISO-8601 UTC strings compare correctly as text.
# Run with: guest_exec.rb --script collect_detections.sh <server VMID> START END
echo "=== FILTER DETECTIONS ==="
awk -F'|' -v s="$1" -v e="$2" '$1 >= s && $1 <= e' /var/log/logstash/filter_detections.log
