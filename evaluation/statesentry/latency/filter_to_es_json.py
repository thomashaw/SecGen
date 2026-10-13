#!/usr/bin/env python3
"""Convert a filter-based results file into the layout analyse.py reads.

benchmark.sh writes the FILTER DETECTIONS section as pipe-separated lines:

  @timestamp|logstash_receive_time|logstash_normalised_time|filter_detection_time|combined_path|filter_alert_name

analyse.py parses every non-router section as an Elasticsearch _search
response, so it finds no events in that layout. This script rewrites the
section as one _search-shaped JSON document, using field names already listed
in analyse.FIELD_ALIASES, and copies every other section through unchanged.
The column order is the output format set in filter_ls_conf.yml.

  python3 filter_to_es_json.py benchmark_results_2.txt benchmark_results_2_es.txt
"""
import json
import sys

COLUMNS = ["@timestamp", "logstash_receive_time", "logstash_normalised_time",
           "filter_detect_time", "combined_path", "filter_alert_name"]


def convert(lines):
    hits, bad = [], []
    for n, line in enumerate(lines, 1):
        if not line.strip():
            continue
        parts = line.strip().split("|")
        if len(parts) != len(COLUMNS):
            bad.append((n, line))
            continue
        hits.append({"_id": "filter-%d" % len(hits),
                     "_source": dict(zip(COLUMNS, parts))})
    return {"hits": {"total": len(hits), "hits": hits}}, bad


def main(src, dst):
    text = open(src, encoding="utf-8").read()
    out, section, buf = [], None, []

    def flush():
        if section is None:
            out.extend(buf)
        elif "FILTER DETECTION" in section.upper():
            doc, bad = convert(buf)
            out.append(json.dumps(doc))
            print("%s: %d detection line(s) converted, %d skipped"
                  % (section, len(doc["hits"]["hits"]), len(bad)))
            for n, line in bad:
                print("  skipped line %d of section: %r" % (n, line[:100]))
        else:
            out.extend(buf)

    for line in text.splitlines():
        if line.startswith("=== ") and line.rstrip().endswith(" ==="):
            flush()
            out.append(line)
            section, buf = line.strip("= \n"), []
        else:
            buf.append(line)
    flush()
    open(dst, "w", encoding="utf-8").write("\n".join(out) + "\n")


if __name__ == "__main__":
    if len(sys.argv) != 3:
        sys.exit(__doc__)
    main(sys.argv[1], sys.argv[2])
