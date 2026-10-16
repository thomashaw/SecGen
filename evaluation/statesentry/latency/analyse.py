#!/usr/bin/env python3
"""
StateSentry pipeline latency characterisation.

Derives per-hop and end-to-end detection latency for the StateSentry goal
detection pipeline from the output of benchmark.sh, which is a file of
'=== HEADER ===' sections containing (i) an Elasticsearch _search response for
the elastalert_status index, (ii) an Elasticsearch _search response for
secgen-events-*, and (iii) BENCHMARK lines grepped from the alert router log.

Runs on Python 3.6 and later. No third-party dependencies.

Modes:

  python3 analyse.py keys FILE [FILE...]
        Show the sections found, the fields present in each, and which map to
        pipeline stages. Run this first on any new results file; if a stage
        reports no matching field, add the real name to FIELD_ALIASES below or
        supply it with --map.

  python3 analyse.py describe FILE
        Full report for one file: exclusions, per-hop boundary increments,
        end-to-end latency, and structural checks.

  python3 analyse.py compare INDEXED_FILE FILTER_FILE
        Both files side by side, with the reduction and a bootstrap confidence
        interval for the difference in medians.

  python3 analyse.py dist FILE
        Full distribution per stage (minimum, median, interquartile range,
        ninetieth and ninety-fifth percentiles, maximum), a LaTeX booktabs
        table, and empirical CDF coordinates for plotting.

Options:
  --window-start ISO8601   Ignore events whose audit timestamp is earlier.
  --window-end   ISO8601   Ignore events whose audit timestamp is later.
                           A window is effectively required: both the
                           Elasticsearch queries and the router log grep in
                           benchmark.sh are unbounded in time, so a results
                           file contains every run ever made rather than only
                           the run of interest. The companion script
                           show_runs.py identifies the run boundaries.
  --path PATH              Restrict to this goal path. Repeatable. Default:
                           /etc/shadow, because that is what the benchmark
                           loop triggers, whilst the query in benchmark.sh
                           matches two paths. Use --path any to disable.
  --max-hop-s FLOAT        Ceiling on any single increment. Default 32, being
                           the benchmark's own inter-repetition spacing:
                           anything larger is cross-contamination between
                           repetitions rather than one slow event.
  --router-max-gap-s N     Maximum raise-to-dispatch gap when pairing router
                           log lines by adjacency. Default 10.
  --join-tolerance-ms N    Tolerance when joining an alert to its event by the
                           event's own timestamp. Default 1.
  --map STAGE=FIELD        Override a field name. Repeatable. Stages: audit,
                           ls_recv, ls_norm, indexed, alert, path, host.
  --dump-csv PREFIX        Write per-record increments, and in dist mode every
                           observation, for independent checking.
  --victim-offset-ms MS    Measured clock offset of the monitored VM, as
                           victim minus analysis server, in milliseconds
                           (negative when the victim's clock is behind). It
                           is subtracted from each audit timestamp before any
                           interval is computed. Give it once to apply to
                           every file, or once per file in file order (for
                           compare, the first value is for the first file).
                           Default 0, meaning the clocks are assumed to agree.
                           Measure it with icmp_offset.rb (decision 8).

Measurement decisions, recorded here because each one materially affects the
figures produced:

  1. Every hop is the interval between two adjacent stage boundaries. The
     storage and query stage is alert_time minus ingest_timestamp. The
     match_time field is NOT a stage boundary: ElastAlert populates it from
     the matched document's own @timestamp, so it equals the audit time, and
     any interval derived from it silently reproduces end-to-end latency under
     a per-hop label.
  2. Pipeline timestamps are read from match_body within each
     elastalert_status document, which carries the whole event. The document's
     own top-level @timestamp records when the alert was written and is some
     seconds later; the two must not be conflated, and the flattener therefore
     exposes fully qualified field names only.
  3. End-to-end latency is measured directly from the originating system call
     to detection, never summed from the hops. Medians of component intervals
     do not in general sum to the median of their total, so a per-record
     tiling residual is computed and reported as a check instead.
  4. A single exclusion policy is applied identically to every input file.
  5. Router raise and dispatch lines carry no correlation identifier, so they
     are paired by adjacency within a bounded gap, and the pair count, orphan
     raises and unmatched dispatches are all reported. This is an assumption
     rather than a measurement and is labelled as such; adding an identifier
     to both log lines in alert_router.rb would remove the need for it.
  6. Structural checks run before any figure is printed: identical-series
     detection, negative and over-ceiling increments, the tiling residual, and
     the reliability of any reported upper quantile given the sample size.
  7. Response time is extended past detection PER EVENT, never by adding
     medians. For each usable event, ordered by detection time, the first
     unused router 'raise_alert_event_received' line no earlier than the
     detection and within --router-max-gap-s is taken as that event's router
     receipt, and the first unused 'action_dispatched' line after that receipt
     (same gap) as its first dispatch. Three intervals follow: router launch
     (detection -> receipt), detection -> first dispatch, and system call ->
     first dispatch. Being anchored to each detection, this pairing does not
     depend on the window. It is still adjacency rather than an identifier,
     and is labelled as such. The 'raise -> dispatch' row printed by earlier
     versions as 'Detector match -> action dispatch' starts at router receipt,
     not detection; it is relabelled accordingly and its figures are unchanged.
     The audit timestamp comes from the monitored VM's clock and every later
     boundary from the analysis server's, so intervals that start at the
     system call assume the two clocks agree unless --victim-offset-ms
     corrects them (decision 8); router launch and dispatch are measured on
     one host. 'Dispatched' is logged immediately before an action
     executes, so system call -> first dispatch is a lower bound on the time
     at which a participant sees the response.
  8. Clock offset between the monitored VM and the analysis server. The audit
     timestamp is on the monitored VM's clock and every other boundary on the
     server's, so each interval that starts at the system call (the
     collection stage and both end-to-end rows) includes the difference
     between the two clocks. Neither VM in the benchmark set is synchronised
     by NTP. The offset is therefore measured separately with
     icmp_offset.rb (ICMP timestamp requests from the server, as clockdiff
     does, with an error of about +/- 1.3 ms) and supplied with
     --victim-offset-ms, which subtracts it from the audit timestamp after the
     alert join and router pairing and before any interval is computed. The
     window and the match_time check use the audit timestamp as recorded.
     Intervals measured on the server alone are unaffected.
"""

import argparse
import csv
import json
import math
import random
import re
import sys
from collections import Counter, defaultdict

BOOTSTRAP_REPS = 10000
BOOTSTRAP_SEED = 20260806
DEFAULT_PATHS = ["/etc/shadow"]

# --------------------------------------------------------------------------
# Timestamps
# --------------------------------------------------------------------------

_ISO_RE = re.compile(
    r"^(\d{4})-(\d{2})-(\d{2})[T ](\d{2}):(\d{2}):(\d{2})"
    r"(?:[.,](\d+))?"
    r"\s*(Z|UTC|[+-]\d{2}:?\d{2})?$"
)


def _days_from_civil(y, m, d):
    y -= m <= 2
    era = (y if y >= 0 else y - 399) // 400
    yoe = y - era * 400
    doy = (153 * (m + (-3 if m > 2 else 9)) + 2) // 5 + d - 1
    doe = yoe * 365 + yoe // 4 - yoe // 100 + doy
    return era * 146097 + doe - 719468


def to_epoch_ms(value):
    """Coerce a timestamp of unknown flavour to epoch ms, or None."""
    if value is None or isinstance(value, bool):
        return None
    if isinstance(value, (int, float)):
        n = float(value)
        if n <= 0:
            return None
        if n < 4e9:
            return n * 1000.0
        if n < 4e12:
            return n
        if n < 4e15:
            return n / 1000.0
        return n / 1e6
    s = str(value).strip()
    if not s:
        return None
    m = _ISO_RE.match(s)
    if m:
        year, mon, day, hh, mm, ss, frac, off = m.groups()
        days = _days_from_civil(int(year), int(mon), int(day))
        ms = (days * 86400 + int(hh) * 3600 + int(mm) * 60 + int(ss)) * 1000.0
        if frac:
            ms += float("0." + frac) * 1000.0
        if off and off not in ("Z", "UTC"):
            sign = 1 if off[0] == "+" else -1
            body = off[1:].replace(":", "")
            ms -= sign * (int(body[:2]) * 3600 + int(body[2:4])) * 1000.0
        return ms
    try:
        return to_epoch_ms(float(s))
    except ValueError:
        return None


# --------------------------------------------------------------------------
# Field aliases. Extend, or override with --map.
# --------------------------------------------------------------------------

# match_body.* is listed FIRST for every event-side stage: in an
# elastalert_status document the event lives under match_body, and the
# document's own top-level fields describe the alert, not the event.
FIELD_ALIASES = {
    "audit":   ["match_body.@timestamp", "@timestamp", "audit_time",
                "event_time", "auditd_timestamp"],
    "ls_recv": ["match_body.logstash_receive_time", "logstash_receive_time",
                "logstash_receive", "beat_received"],
    "ls_norm": ["match_body.logstash_normalised_time",
                "logstash_normalised_time", "match_body.logstash_output_time",
                "logstash_output_time", "logstash_normalized_time"],
    "indexed": ["match_body.ingest_timestamp", "ingest_timestamp",
                "es_indexed_time", "indexed_time"],
    "alert":   ["alert_time", "filter_detect_time", "detection_time"],
    "match_copy": ["match_time"],
    "path":    ["match_body.combined_path", "combined_path",
                "match_body.file.path", "file_path"],
    "host":    ["match_body.agent.hostname", "match_body.host.name",
                "hostname", "agent.hostname"],
    "rule":    ["rule_name"],
    "repetition": ["repetition", "rep", "iteration", "attempt"],
}

SECGEN_STAGES = ["audit", "ls_recv", "ls_norm", "indexed"]

HOPS = [
    ("h1", "Audit event generation -> Logstash ingestion", "audit", "ls_recv"),
    ("h2", "Logstash normalisation", "ls_recv", "ls_norm"),
    ("h3", "Logstash output -> ES searchable", "ls_norm", "indexed"),
    ("h4", "ES searchable -> detector match", "indexed", "alert"),
]


def flatten(obj, prefix=""):
    """Flatten with fully-qualified keys only.

    Bare leaf names are deliberately NOT exposed. An elastalert_status document
    has its own top-level @timestamp (when the alert record was written) and a
    match_body.@timestamp (the audit event). Collapsing leaves to bare names
    makes those two indistinguishable, and picking the wrong one shifts every
    hop by the detection latency itself.
    """
    out = {}
    for k, v in obj.items():
        key = prefix + k
        if isinstance(v, dict):
            out.update(flatten(v, prefix=key + "."))
        else:
            out[key] = v
    return out


def pick(raw, logical):
    for name in FIELD_ALIASES.get(logical, []):
        if name in raw and raw[name] not in (None, "", "-", []):
            return raw[name]
    lowered = {}
    for k, v in raw.items():
        lowered.setdefault(k.lower(), v)
    for name in FIELD_ALIASES.get(logical, []):
        v = lowered.get(name.lower())
        if v not in (None, "", "-", [], None):
            return v
    return None


# --------------------------------------------------------------------------
# Sectioned file parsing
# --------------------------------------------------------------------------

SECTION_RE = re.compile(r"^\s*={2,}\s*(.+?)\s*={2,}\s*$")


def split_sections(text):
    """Return [(header, body_text), ...]. Text before the first header is
    returned under the header '(preamble)'."""
    sections = []
    header = "(preamble)"
    buf = []
    for line in text.splitlines():
        m = SECTION_RE.match(line)
        if m:
            if buf:
                sections.append((header, "\n".join(buf)))
            header = m.group(1)
            buf = []
        else:
            buf.append(line)
    if buf:
        sections.append((header, "\n".join(buf)))
    return sections


def classify(header):
    h = header.upper()
    if "ELASTALERT" in h:
        return "alerts"
    if "SECGEN" in h or "FILTER DETECTION" in h or "EVENT" in h:
        return "events"
    if "ROUTER" in h or "BENCHMARK" in h:
        return "router"
    return "unknown"


def es_hits(body):
    """Extract [(_id, flattened _source)] from an ES _search response body.

    Tolerates the response being on one line, pretty-printed, or several
    concatenated responses in the same section.
    """
    text = body.strip()
    if not text:
        return [], None
    docs = []
    error = None
    # A section may hold more than one JSON document; decode greedily.
    decoder = json.JSONDecoder()
    idx = 0
    found_any = False
    while idx < len(text):
        while idx < len(text) and text[idx] not in "{[":
            idx += 1
        if idx >= len(text):
            break
        try:
            obj, end = decoder.raw_decode(text, idx)
        except ValueError as exc:
            if not found_any:
                error = str(exc)
            break
        found_any = True
        idx = end
        if isinstance(obj, dict):
            if "error" in obj and "hits" not in obj:
                error = "Elasticsearch returned an error object"
            hits = obj.get("hits", {})
            if isinstance(hits, dict):
                for h in hits.get("hits", []) or []:
                    src = h.get("_source")
                    if isinstance(src, dict):
                        docs.append((h.get("_id"), flatten(src)))
                if "total" in hits:
                    total = hits["total"]
                    if isinstance(total, dict):
                        total = total.get("value")
                    if isinstance(total, int) and total > len(docs):
                        error = ("hits.total reports %d but only %d returned: "
                                 "the size= cap truncated the result set"
                                 % (total, len(docs)))
    return docs, error


ROUTER_TS = re.compile(
    r"(\d{4}-\d{2}-\d{2}[T ]\d{2}:\d{2}:\d{2}(?:[.,]\d+)?(?:Z|[+-]\d{2}:?\d{2})?)")
ROUTER_KV = re.compile(r"([A-Za-z_][\w.\-]*)\s*[=:]\s*(\"[^\"]*\"|\S+)")
# Underscore-joined tokens such as raise_alert_event_received and
# action_dispatched have no word boundary after the verb, so anchor on the
# token rather than on \b.
ROUTER_RAISE = re.compile(
    r"(raise_alert_event_received|raise_alert|alert_raised|event_received|"
    r"\bRAISED?\b|\bRAISING\b|\bTRIGGER(?:ED)?\b)", re.I)
ROUTER_DISPATCH = re.compile(
    r"(action_dispatched|action_sent|alert_dispatched|dispatch(?:ed)?_action|"
    r"\bDISPATCHED?\b|\bSENT\b|\bEXECUTED?\b)", re.I)


def parse_router(body):
    """Parse BENCHMARK log lines into [{'kind','ts','fields','raw'}].

    Deliberately does not pair anything: pairing happens later, by identifier.
    """
    out = []
    for line in body.splitlines():
        s = line.strip()
        if not s:
            continue
        # A Ruby Logger line carries two timestamps: the logger prefix, in
        # local time, and the payload value, in UTC. Take the LAST one on the
        # line, which is the payload, so that a BST/UTC difference in the
        # prefix cannot leak a one-hour offset into the measurement.
        ts = None
        all_ts = ROUTER_TS.findall(s)
        if all_ts:
            ts = to_epoch_ms(all_ts[-1])
            if len(all_ts) > 1:
                prefix_ms = to_epoch_ms(all_ts[0])
                if prefix_ms is not None and abs(prefix_ms - ts) > 60000:
                    pass  # recorded by the caller as a prefix/payload offset
        fields = {}
        for k, v in ROUTER_KV.findall(s):
            fields[k] = v.strip('"')
        if ts is None:
            for k, v in fields.items():
                cand = to_epoch_ms(v)
                if cand and cand > 9e11:
                    ts = cand
                    break
        kind = None
        if ROUTER_DISPATCH.search(s):
            kind = "dispatch"
        elif ROUTER_RAISE.search(s):
            kind = "raise"
        prefix_off = None
        if len(all_ts) > 1:
            p = to_epoch_ms(all_ts[0])
            if p is not None and ts is not None:
                prefix_off = p - ts
        out.append({"kind": kind, "ts": ts, "fields": fields, "raw": s,
                    "prefix_offset": prefix_off})
    return out


# --------------------------------------------------------------------------
# Statistics
# --------------------------------------------------------------------------

def quantile(vals, q):
    if not vals:
        return None
    if len(vals) == 1:
        return float(vals[0])
    pos = (len(vals) - 1) * q
    lo, hi = int(math.floor(pos)), int(math.ceil(pos))
    if lo == hi:
        return float(vals[lo])
    frac = pos - lo
    return vals[lo] * (1 - frac) + vals[hi] * frac


def summarise(values):
    vals = sorted(float(v) for v in values)
    if not vals:
        return None
    q1, q3 = quantile(vals, 0.25), quantile(vals, 0.75)
    return {"n": len(vals), "median": quantile(vals, 0.5), "q1": q1, "q3": q3,
            "iqr": q3 - q1, "min": vals[0], "max": vals[-1],
            "p90": quantile(vals, 0.90), "p95": quantile(vals, 0.95),
            "p99": quantile(vals, 0.99),
            "mean": sum(vals) / len(vals), "values": vals}


def quantile_support(n, q):
    """How many observations sit at or above a quantile, and the weight the
    largest observation carries in it under type-7 interpolation.

    A quantile whose value is dominated by one or two order statistics is an
    order statistic, not an estimate of a population quantile, and must be
    labelled as such.
    """
    if n < 2:
        return {"above": 0, "top_weight": 1.0, "position": 0.0}
    pos = (n - 1) * q
    lo = int(math.floor(pos))
    frac = pos - lo
    top_weight = frac if lo == n - 2 else (1.0 if lo >= n - 1 else 0.0)
    return {"above": n - 1 - lo, "top_weight": top_weight, "position": pos}


def tail_caveats(n):
    """Return [(label, text)] for any reported quantile that n cannot support."""
    out = []
    for q, label in ((0.90, "p90"), (0.95, "p95"), (0.99, "p99")):
        s = quantile_support(n, q)
        need = int(10 / (1 - q))
        if s["top_weight"] > 0.25:
            out.append((label,
                        "%s draws %.0f%% of its weight from the single largest "
                        "observation at n=%d; it is an order statistic rather "
                        "than an estimate of the population %s (roughly n>=%d "
                        "would be required)."
                        % (label, 100 * s["top_weight"], n, label, need)))
        elif s["above"] < 3:
            out.append((label,
                        "%s rests on only %d observation(s) above it at n=%d; "
                        "roughly n>=%d would be required for a stable estimate."
                        % (label, s["above"], n, need)))
    return out


def bootstrap_median_diff(a, b, reps=BOOTSTRAP_REPS):
    if not a or not b:
        return None
    rng = random.Random(BOOTSTRAP_SEED)
    diffs = []
    for _ in range(reps):
        ra = sorted(rng.choice(a) for _ in range(len(a)))
        rb = sorted(rng.choice(b) for _ in range(len(b)))
        diffs.append(quantile(rb, 0.5) - quantile(ra, 0.5))
    diffs.sort()
    return (quantile(diffs, 0.5), quantile(diffs, 0.025), quantile(diffs, 0.975))


# --------------------------------------------------------------------------
# Analysis
# --------------------------------------------------------------------------

class Event(object):
    """A secgen-events document, optionally joined to its alert."""

    def __init__(self, doc_id, raw):
        self.id = doc_id
        self.raw = raw
        self.t = {}
        for stage in SECGEN_STAGES + ["alert"]:
            # 'alert' is normally supplied by the join with elastalert_status,
            # but the filter-based path has no such index and carries its own
            # detection timestamp on the event document.
            ms = to_epoch_ms(pick(raw, stage))
            if ms is not None:
                self.t[stage] = ms
        p = pick(raw, "path")
        if isinstance(p, list):
            p = p[0] if p else None
        self.path = str(p) if p is not None else None
        h = pick(raw, "host")
        self.host = str(h) if h is not None else None
        self.alert_doc = None
        self.dispatch_ts = None
        # audit timestamp as recorded, before any --victim-offset-ms correction
        self.audit_raw = self.t.get("audit")

    def increment(self, a, b):
        if a in self.t and b in self.t:
            return self.t[b] - self.t[a]
        return None


def load(path):
    fh = open(path, "r", errors="replace")
    try:
        text = fh.read()
    finally:
        fh.close()
    sections = split_sections(text)
    data = {"path": path, "sections": [], "events": [], "alerts": [],
            "router": [], "errors": []}
    for header, body in sections:
        kind = classify(header)
        info = {"header": header, "kind": kind, "bytes": len(body), "n": 0}
        if kind in ("alerts", "events"):
            docs, err = es_hits(body)
            info["n"] = len(docs)
            if err:
                data["errors"].append("[%s] %s" % (header, err))
            if kind == "events":
                data["events"].extend(docs)
            else:
                data["alerts"].extend(docs)
        elif kind == "router":
            lines = parse_router(body)
            info["n"] = len(lines)
            data["router"].extend(lines)
        elif body.strip():
            docs, _err = es_hits(body)
            if docs:
                info["kind"] = "unknown (contains ES hits)"
                info["n"] = len(docs)
        data["sections"].append(info)
    return data


def join_alerts(events, alert_docs, tolerance_ms, res):
    """Attach each alert document to the event it matched.

    The join key is the matched document's own timestamp, which ElastAlert
    copies into both match_time and match_body's @timestamp. This is the only
    thing linking the two indices in the data as collected.
    """
    by_ts = defaultdict(list)
    for ev in events:
        if "audit" in ev.t:
            by_ts[round(ev.t["audit"])].append(ev)

    joined = unmatched = ambiguous = 0
    for _doc_id, doc in alert_docs:
        key = None
        for field in ("match_body.@timestamp", "match_body.audit_time",
                      "match_time"):
            if field in doc:
                key = to_epoch_ms(doc[field])
                if key is not None:
                    break
        alert_ms = to_epoch_ms(pick(doc, "alert"))
        if key is None or alert_ms is None:
            unmatched += 1
            continue
        cands = []
        for off in range(-int(tolerance_ms), int(tolerance_ms) + 1):
            cands.extend(by_ts.get(round(key) + off, []))
        cands = [c for c in cands if c.alert_doc is None]
        if not cands:
            unmatched += 1
            continue
        if len(cands) > 1:
            ambiguous += 1
        ev = cands[0]
        ev.alert_doc = doc
        ev.t["alert"] = alert_ms
        joined += 1

    res["join"] = {"joined": joined, "unmatched_alerts": unmatched,
                   "ambiguous": ambiguous, "alert_docs": len(alert_docs)}
    if unmatched:
        res["notes"].append(
            "%d alert document(s) could not be joined to an event. These are "
            "either alerts for events outside the queried path set, or "
            "writeback documents of another type." % unmatched)
    if ambiguous:
        res["warnings"].append(
            "%d alert(s) matched more than one candidate event within the join "
            "tolerance; the first was used. Two events sharing an audit "
            "timestamp to the millisecond cannot be distinguished by this join."
            % ambiguous)


def attach_router(events, router_lines, res, args):
    """Pair raise -> dispatch.

    Your router log carries no identifier on either line, so identifier-based
    pairing is impossible. Two consequences, both stated rather than hidden:

      * the log is a grep of the entire alert-router.log, so it contains every
        run ever made. It MUST be windowed, or the same lines are attributed to
        both implementations. That is what produced n=152 from 50 detections.
      * within a window, pairing can only be by adjacency. This is reported
        under an explicit maximum gap, and the pair count is cross-checked
        against the number of detections.
    """
    lines = [l for l in router_lines if l["ts"] is not None and l["kind"]]
    res["router_lines_total"] = len(router_lines)
    res["router_lines_usable"] = len(lines)

    offs = [l["prefix_offset"] for l in router_lines
            if l.get("prefix_offset") is not None]
    if offs:
        offs_sorted = sorted(offs)
        med = quantile(offs_sorted, 0.5)
        if abs(med) > 60000:
            res["notes"].append(
                "Router log prefix runs %.0f minute(s) from the payload "
                "timestamp on the same line, consistent with the Ruby logger "
                "writing local time whilst the payload is UTC. The payload is "
                "used throughout, so this does not affect any figure."
                % (med / 60000.0))

    if args.window_start is not None or args.window_end is not None:
        before = len(lines)
        lines = [l for l in lines
                 if (args.window_start is None or l["ts"] >= args.window_start)
                 and (args.window_end is None or l["ts"] <= args.window_end)]
        res["router_lines_in_window"] = len(lines)
        if before and not lines:
            res["warnings"].append(
                "All %d router lines fall outside the window." % before)
    else:
        res["router_lines_in_window"] = len(lines)
        if len(lines) > 500:
            res["warnings"].append(
                "%d router lines are in scope and no window was given. This log "
                "is a grep of the whole alert-router.log and contains every run "
                "ever made, so without --window-start/--window-end the same "
                "lines are attributed to both implementations. Router latency "
                "is NOT reported." % len(lines))
            res["router_stats"] = None
            res["router_pairs"] = {}
            return

    lines.sort(key=lambda l: l["ts"])
    max_gap = args.router_max_gap_s * 1000.0
    pairs = {}
    pending = None
    unmatched_dispatch = orphan_raise = 0
    for l in lines:
        if l["kind"] == "raise":
            if pending is not None:
                orphan_raise += 1
            pending = l
        else:
            if pending is None:
                unmatched_dispatch += 1
                continue
            gap = l["ts"] - pending["ts"]
            if gap < 0 or gap > max_gap:
                unmatched_dispatch += 1
                pending = None
                continue
            pairs["%.3f" % pending["ts"]] = gap
            pending = None

    res["router_pairs"] = pairs
    res["router_stats"] = summarise(list(pairs.values())) if pairs else None
    res["router_pairing"] = {
        "orphan_raises": orphan_raise,
        "unmatched_dispatches": unmatched_dispatch,
        "max_gap_s": args.router_max_gap_s,
    }
    if pairs:
        res["notes"].append(
            "Router latency is paired by ADJACENCY, not by identifier, because "
            "the log lines carry none: %d pair(s) formed within %.0f s, with %d "
            "orphan raise(s) and %d unmatched dispatch(es). Add a goal or "
            "correlation identifier to both log lines before this row is "
            "reported in the paper."
            % (len(pairs), args.router_max_gap_s, orphan_raise,
               unmatched_dispatch))


RESPONSE_ROWS = [
    ("router_launch", "Detection -> alert router receives alert", "alert", "router_recv"),
    ("det_dispatch", "Detection -> first action dispatched", "alert", "dispatch"),
    ("e2e_dispatch", "End-to-end (system call -> first action dispatched)", "audit", "dispatch"),
]
ROUTER_ROW_LABEL = "Alert router: receipt -> first action dispatched"


def attach_response(usable, router_lines, res, args):
    """Per-event router receipt and first dispatch (measurement decision 7)."""
    lines = sorted((l for l in router_lines if l["ts"] is not None and l["kind"]),
                   key=lambda l: l["ts"])
    raises = [l["ts"] for l in lines if l["kind"] == "raise"]
    disps = [l["ts"] for l in lines if l["kind"] != "raise"]
    max_gap = args.router_max_gap_s * 1000.0
    used_r, used_d = set(), set()
    paired = no_receipt = no_dispatch = 0

    def first_after(series, used, t0):
        for i, t in enumerate(series):
            if i in used or t < t0:
                continue
            if t - t0 > max_gap:
                return None
            return i
        return None

    for e in sorted((e for e in usable if "alert" in e.t), key=lambda e: e.t["alert"]):
        ri = first_after(raises, used_r, e.t["alert"])
        if ri is None:
            no_receipt += 1
            continue
        di = first_after(disps, used_d, raises[ri])
        if di is None:
            no_dispatch += 1
            continue
        used_r.add(ri)
        used_d.add(di)
        e.t["router_recv"] = raises[ri]
        e.t["dispatch"] = disps[di]
        paired += 1

    res["response"] = {}
    for key, lbl, a, b in RESPONSE_ROWS:
        vals = [d for d in (e.increment(a, b) for e in usable) if d is not None]
        res["response"][key] = {"label": lbl, "stats": summarise(vals)}
    res["response_pairing"] = {"paired": paired, "no_receipt": no_receipt,
                               "no_dispatch": no_dispatch,
                               "detections": sum(1 for e in usable if "alert" in e.t)}
    if paired:
        res["notes"].append(
            "Response time is paired PER EVENT by adjacency (decision 7): %d of %d "
            "detection(s) matched to a router receipt and a first dispatch within "
            "%.0f s, %d with no receipt, %d with no dispatch. Rows starting at the "
            "system call assume the monitored VM and analysis server clocks agree; "
            "'dispatched' marks the start of the action, not its completion."
            % (paired, res["response_pairing"]["detections"], args.router_max_gap_s,
               no_receipt, no_dispatch))
    if paired < res["response_pairing"]["detections"]:
        res["warnings"].append(
            "%d detection(s) could not be matched to router activity, so the "
            "response-time rows cover fewer events than the detection rows."
            % (res["response_pairing"]["detections"] - paired))
    # Cross-check: the per-event receipt -> dispatch values should reproduce
    # the window-based router pairing when both are well formed.
    per_event = [e.t["dispatch"] - e.t["router_recv"] for e in usable
                 if "dispatch" in e.t]
    rs = res.get("router_stats")
    if rs and per_event:
        pe = summarise(per_event)
        if abs(pe["median"] - rs["median"]) > 1.0 or pe["n"] != rs["n"]:
            res["warnings"].append(
                "Per-event router pairing (n=%d, median %.1f ms) disagrees with "
                "the window-based pairing (n=%d, median %.1f ms); inspect the "
                "router lines before reporting either."
                % (pe["n"], pe["median"], rs["n"], rs["median"]))


def analyse(path, label, args, victim_offset_ms=0.0):
    data = load(path)
    res = {"path": path, "label": label, "sections": data["sections"],
           "notes": [], "warnings": [], "errors": data["errors"],
           "hops": {}, "router_stats": None, "router_pairs": {},
           "victim_offset_ms": victim_offset_ms}

    events = [Event(i, d) for i, d in data["events"]]
    # An elastalert_status document carries the whole pipeline inside
    # match_body, so it is a complete record in itself. Where the two sections
    # overlap, prefer whichever set actually has the stage timestamps.
    alert_events = [Event(i, d) for i, d in data["alerts"]]
    alert_events = [e for e in alert_events if "audit" in e.t]
    if len(alert_events) > len(events):
        if events:
            res["notes"].append(
                "%d secgen-events document(s) were present but %d "
                "elastalert_status documents carry the pipeline timestamps "
                "inside match_body; the latter are used."
                % (len(events), len(alert_events)))
        events = alert_events
        res["self_contained"] = True
    res["events_total"] = len(events)

    # path restriction
    if args.path == ["any"]:
        kept = events
        res["path_counts"] = Counter(e.path for e in events)
    else:
        res["path_counts"] = Counter(e.path for e in events)
        kept = [e for e in events if e.path in args.path]
        dropped = len(events) - len(kept)
        if dropped:
            res["notes"].append(
                "%d event(s) excluded by --path: the query in benchmark.sh "
                "matches two goal paths but the loop only triggers %s."
                % (dropped, ", ".join(args.path)))

    if res.get("self_contained"):
        res["join"] = {"joined": len(kept), "unmatched_alerts": 0,
                       "ambiguous": 0, "alert_docs": len(data["alerts"]),
                       "self_contained": True}
    else:
        join_alerts(kept, data["alerts"], args.join_tolerance_ms, res)
    attach_router(kept, data["router"], res, args)

    # Clock correction (measurement decision 8). Applied after the alert join,
    # which matches on the audit timestamp exactly as recorded; the window and
    # the match_time check below also use the recorded value.
    if victim_offset_ms:
        for e in kept:
            if "audit" in e.t:
                e.t["audit"] = e.audit_raw - victim_offset_ms
        res["notes"].append(
            "Audit timestamps moved onto the analysis server's clock by "
            "subtracting a victim clock offset of %+.1f ms (--victim-offset-ms); "
            "every interval that starts at the system call is corrected, the "
            "rest are unchanged." % victim_offset_ms)

    # exclusion policy
    reasons = Counter()
    usable = []
    max_ms = args.max_hop_s * 1000.0
    for e in kept:
        if "audit" not in e.t:
            reasons["no audit timestamp"] += 1
            continue
        if args.window_start is not None and e.audit_raw < args.window_start:
            reasons["before window start"] += 1
            continue
        if args.window_end is not None and e.audit_raw > args.window_end:
            reasons["after window end"] += 1
            continue
        neg, big = [], []
        for key, _l, a, b in HOPS:
            d = e.increment(a, b)
            if d is None:
                continue
            if d < 0:
                neg.append(key)
            elif d > max_ms:
                big.append(key)
        if neg:
            reasons["negative increment (%s)" % ",".join(neg)] += 1
            continue
        if big:
            reasons["increment over %.0fs (%s)" % (args.max_hop_s,
                                                   ",".join(big))] += 1
            continue
        usable.append(e)
    res["kept"] = usable
    res["reasons"] = reasons

    res["present"] = dict((s, sum(1 for e in usable if s in e.t))
                          for s in SECGEN_STAGES + ["alert"])

    for key, lbl, a, b in HOPS:
        vals = []
        for e in usable:
            d = e.increment(a, b)
            if d is not None:
                vals.append(d)
        res["hops"][key] = {"label": lbl, "stats": summarise(vals)}

    det = []
    for e in usable:
        d = e.increment("audit", "alert")
        if d is not None:
            det.append(d)
    res["e2e_detection"] = summarise(det)
    if not det:
        res["warnings"].append(
            "No detection latency measurable: no event carries both an audit "
            "timestamp and a joined alert_time.")

    attach_response(usable, data["router"], res, args)
    structural_checks(res)
    return res


def structural_checks(res):
    series = {}
    for key, h in res["hops"].items():
        if h["stats"]:
            series[key] = tuple(h["stats"]["values"])
    if res["e2e_detection"]:
        series["detection"] = tuple(res["e2e_detection"]["values"])
    names = sorted(series)
    for i in range(len(names)):
        for j in range(i + 1, len(names)):
            if len(series[names[i]]) > 2 and series[names[i]] == series[names[j]]:
                res["warnings"].append(
                    "IDENTICAL SERIES: '%s' and '%s' hold the same values. One "
                    "of them is not the quantity its label claims."
                    % (names[i], names[j]))

    # prove match_time is a copy, not a boundary
    same = tot = 0
    for e in res["kept"]:
        if e.alert_doc is None or "audit" not in e.t:
            continue
        mt = to_epoch_ms(pick(e.alert_doc, "match_copy"))
        if mt is None:
            continue
        tot += 1
        if abs(mt - e.audit_raw) < 1.0:
            same += 1
    if tot and float(same) / tot > 0.9:
        res["warnings"].append(
            "match_time equals the event's own audit timestamp in %d/%d joined "
            "records, confirming it is a copy of @timestamp from the matched "
            "document rather than the instant the rule fired. Any hop computed "
            "as alert_time - match_time is therefore end-to-end latency "
            "relabelled, which is the defect behind Table IV." % (same, tot))

    resid = []
    for e in res["kept"]:
        if "audit" not in e.t or "alert" not in e.t:
            continue
        parts = []
        for _k, _l, a, b in HOPS:
            d = e.increment(a, b)
            if d is not None:
                parts.append(d)
        if len(parts) != len(HOPS):
            continue
        resid.append(abs((e.t["alert"] - e.t["audit"]) - sum(parts)))
    res["tiling"] = summarise(resid) if resid else None
    if resid and max(resid) > 1.0:
        res["warnings"].append(
            "Hops do not tile the detection interval: worst per-record residual "
            "%.1f ms. There is unaccounted time between stages, or two hops "
            "overlap." % max(resid))
    elif not resid:
        res["notes"].append(
            "Tiling unverified: no event carries all four intermediate "
            "boundaries plus a joined alert.")


# --------------------------------------------------------------------------
# Inspection
# --------------------------------------------------------------------------

def inspect(path):
    fh = open(path, "r", errors="replace")
    try:
        text = fh.read()
    finally:
        fh.close()
    print("")
    print("=" * 78)
    print("FILE: %s   (%d bytes, %d lines)"
          % (path, len(text), text.count("\n") + 1))
    print("=" * 78)

    data = load(path)
    print("")
    print("  Sections found:")
    for s in data["sections"]:
        print("    %-46s kind=%-10s items=%d"
              % (s["header"][:46], s["kind"], s["n"]))
    for e in data["errors"]:
        print("    ! %s" % e)

    for kind, docs in (("secgen-events", data["events"]),
                       ("elastalert_status", data["alerts"])):
        print("")
        print("  --- %s: %d document(s) ---" % (kind, len(docs)))
        if not docs:
            continue
        counts = Counter()
        example = {}
        for _i, d in docs:
            for k, v in d.items():
                counts[k] += 1
                if k not in example:
                    example[k] = v
        known = {}
        for logical, names in FIELD_ALIASES.items():
            for n in names:
                known.setdefault(n.lower(), logical)
        print("  %-38s %6s %-11s %s"
              % ("FIELD", "COUNT", "MAPS TO", "EXAMPLE"))
        print("  " + "-" * 74)
        unmapped = []
        for k, n in sorted(counts.items(), key=lambda kv: (-kv[1], kv[0])):
            logical = known.get(k.lower(), "")
            ts = to_epoch_ms(example[k])
            flag = logical or ("timestamp?" if ts and ts > 9e11 else "-")
            if not logical and ts and ts > 9e11:
                unmapped.append(k)
            val = str(example[k])
            if len(val) > 24:
                val = val[:21] + "..."
            print("  %-38s %6d %-11s %s" % (k[:38], n, flag[:11], val))
        if unmapped:
            print("")
            print("  Unmapped fields that parse as timestamps: %s"
                  % ", ".join(unmapped[:12]))

    print("")
    print("  --- router BENCHMARK lines: %d ---" % len(data["router"]))
    for ln in data["router"][:6]:
        print("    kind=%-8s ts=%s fields=%s"
              % (ln["kind"], "yes" if ln["ts"] else "NO",
                 ",".join(sorted(ln["fields"])) or "(none)"))
        print("      %s" % ln["raw"][:110])
    if data["router"]:
        no_kind = sum(1 for l in data["router"] if l["kind"] is None)
        no_ts = sum(1 for l in data["router"] if l["ts"] is None)
        no_id = sum(1 for l in data["router"]
                    if not any(k in l["fields"] for k in
                               ("goal_id", "goal", "correlation_id", "id",
                                "event_id", "match_id", "action_id")))
        print("    lines with no raise/dispatch marker: %d" % no_kind)
        print("    lines with no timestamp:             %d" % no_ts)
        print("    lines with no identifier:            %d" % no_id)


# --------------------------------------------------------------------------
# Reporting
# --------------------------------------------------------------------------

def report(res):
    print("")
    print("File: %s [%s]" % (res["path"], res["label"]))
    for s in res["sections"]:
        print("    section %-40s %s=%d" % (s["header"][:40], s["kind"], s["n"]))
    for e in res["errors"]:
        print("    ! %s" % e)

    print("")
    print("  secgen-events documents: %d" % res["events_total"])
    if len(res["path_counts"]) > 1:
        print("  by goal path:")
        for p, n in res["path_counts"].most_common():
            print("      %-52s %d" % (str(p)[:52], n))
    j = res["join"]
    print("  elastalert_status documents: %d, joined to an event: %d, "
          "unjoinable: %d" % (j["alert_docs"], j["joined"],
                              j["unmatched_alerts"]))
    print("  usable events after exclusions: %d" % len(res["kept"]))
    for reason, n in res["reasons"].most_common():
        print("      excluded %4d: %s" % (n, reason))

    n = len(res["kept"])
    print("")
    print("  Stage timestamps present in usable events:")
    for stage in SECGEN_STAGES + ["alert"]:
        have = res["present"][stage]
        mark = "" if have == n else "   <-- not universal"
        print("      %-10s %d/%d%s" % (stage, have, n, mark))

    print("")
    print("  Boundary increments:")
    for key, _l, _a, _b in HOPS:
        h = res["hops"][key]
        s = h["stats"]
        if s is None:
            print("      %s %s: not measurable" % (key, h["label"]))
            continue
        print("      %s %s:" % (key, h["label"]))
        print("          n=%d, median=%.1f, IQR=%.1f (Q1=%.1f, Q3=%.1f)"
              % (s["n"], s["median"], s["iqr"], s["q1"], s["q3"]))
        print("          min=%.1f  p90=%.1f  p95=%.1f  p99=%.1f  max=%.1f"
              % (s["min"], s["p90"], s["p95"], s["p99"], s["max"]))
    rs = res["router_stats"]
    print("      h5 %s (window-based pairing):" % ROUTER_ROW_LABEL)
    if rs:
        print("          n=%d (see pairing note), median=%.1fms, "
              "IQR=%.1fms, min=%.1fms, max=%.1fms"
              % (rs["n"], rs["median"], rs["iqr"], rs["min"], rs["max"]))
    else:
        print("          NOT REPORTED (see warnings)")

    s = res["e2e_detection"]
    print("")
    print("  End-to-end, measured directly (audit event -> detector match):")
    if s:
        print("      n=%d, median=%.1f, IQR=%.1f (Q1=%.1f, Q3=%.1f)"
              % (s["n"], s["median"], s["iqr"], s["q1"], s["q3"]))
        print("      min=%.1f  p90=%.1f  p95=%.1f  p99=%.1f  max=%.1f"
              % (s["min"], s["p90"], s["p95"], s["p99"], s["max"]))
        for label, text in tail_caveats(s["n"]):
            print("      ! %s" % text)
        parts = [h["stats"]["median"] for h in res["hops"].values() if h["stats"]]
        if parts:
            print("      (sum of hop medians = %.1fms, differing from the "
                  "measured median by %+.1fms; medians of components do not sum "
                  "to the median of the total, so this is expected)"
                  % (sum(parts), s["median"] - sum(parts)))
    else:
        print("      not measurable")

    print("")
    print("  Response time, paired per event (see decision 7 and notes):")
    for key, _lbl, _a, _b in RESPONSE_ROWS:
        r = res["response"][key]
        s = r["stats"]
        if s is None:
            print("      %s: not measurable" % r["label"])
            continue
        print("      %s:" % r["label"])
        print("          n=%d, median=%.1f, IQR=%.1f (Q1=%.1f, Q3=%.1f)"
              % (s["n"], s["median"], s["iqr"], s["q1"], s["q3"]))
        print("          min=%.1f  p90=%.1f  p95=%.1f  p99=%.1f  max=%.1f"
              % (s["min"], s["p90"], s["p95"], s["p99"], s["max"]))

    if res["tiling"]:
        t = res["tiling"]
        print("")
        print("  Tiling |detection - sum(hops)| per event: median=%.3fms, "
              "max=%.3fms" % (t["median"], t["max"]))

    if res["notes"]:
        print("")
        print("  Notes:")
        for x in res["notes"]:
            print("      - %s" % x)
    if res["warnings"]:
        print("")
        print("  ** WARNINGS **")
        for x in res["warnings"]:
            print("      ! %s" % x)


def compare(a, b):
    print("")
    print("=" * 78)
    print("COMPARISON (Table V)")
    print("=" * 78)
    print("%-44s%10s%10s%12s" % ("Stage", "Indexed", "Filter", "Delta"))
    print("-" * 78)

    def row(label, sa, sb):
        va = "%.1f" % sa["median"] if sa else "n/a"
        vb = "%.1f" % sb["median"] if sb else "n/a"
        if sa and sb:
            d = "%+.1f" % (sb["median"] - sa["median"])
        elif sa and not sb:
            d = "eliminated"
        else:
            d = "n/a"
        print("%-44s%10s%10s%12s" % (label[:44], va, vb, d))

    for key, lbl, _a, _b in HOPS:
        row(lbl, a["hops"][key]["stats"], b["hops"][key]["stats"])
    row(ROUTER_ROW_LABEL, a["router_stats"], b["router_stats"])
    row("End-to-end (audit -> detection)", a["e2e_detection"], b["e2e_detection"])
    for key, lbl, _a, _b in RESPONSE_ROWS:
        row(lbl, a["response"][key]["stats"], b["response"][key]["stats"])
    print("  ('%s' is window-based: take it from per-file runs, since one window "
          "spanning both files mixes their router lines. The per-event rows "
          "below it are anchored to each detection and unaffected.)"
          % ROUTER_ROW_LABEL)

    sa, sb = a["e2e_detection"], b["e2e_detection"]
    if sa and sb:
        ma, mb = sa["median"], sb["median"]
        print("")
        print("  Detection latency reduction: %.1f ms (%.2f%% of %.1f); "
              "speed-up %.2fx" % (ma - mb, 100.0 * (ma - mb) / ma, ma, ma / mb))
        ci = bootstrap_median_diff(sa["values"], sb["values"])
        if ci:
            print("  Bootstrap median difference (filter - indexed): %.1f ms, "
                  "95%% CI [%.1f, %.1f], %d reps"
                  % (ci[0], ci[1], ci[2], BOOTSTRAP_REPS))

    ha, hb = a["hops"]["h1"]["stats"], b["hops"]["h1"]["stats"]
    if ha and hb:
        print("")
        print("  Collection stage (h1): fixed offset or back-pressure?")
        print("      indexed: median=%.1f min=%.1f Q3=%.1f max=%.1f IQR=%.1f"
              % (ha["median"], ha["min"], ha["q3"], ha["max"], ha["iqr"]))
        print("      filter : median=%.1f min=%.1f Q3=%.1f max=%.1f IQR=%.1f"
              % (hb["median"], hb["min"], hb["q3"], hb["max"], hb["iqr"]))
        shift = ha["median"] - hb["median"]
        print("      indexed min - filter median = %+.1f ms"
              % (ha["min"] - hb["median"]))
        print("      median shift                = %+.1f ms" % shift)
        if hb["iqr"] > 0:
            print("      IQR ratio (indexed/filter)  = %.1fx"
                  % (ha["iqr"] / hb["iqr"]))
        if abs(ha["min"] - hb["median"]) < 25 and shift > 100:
            print("      => Shared floor with a right-skewed indexed tail. A "
                  "fixed offset would translate the whole distribution and "
                  "preserve the IQR, so this is consistent with queueing under "
                  "the indexed configuration, not a measurement offset. Both "
                  "runs are on one host, so a clock difference is excluded by "
                  "construction.")
        elif hb["iqr"] > 0 and abs(ha["iqr"] - hb["iqr"]) < 0.2 * hb["iqr"] \
                and shift > 100:
            print("      => Near-constant shift with comparable spread, which "
                  "is what a fixed offset looks like. Investigate before "
                  "attributing this to load.")

    shared = set(a["router_pairs"]) & set(b["router_pairs"])
    if shared:
        print("")
        print("  ! %d router identifier(s) appear in BOTH files: the same log "
              "lines are being attributed to both paths." % len(shared))



def distribution_table(res):
    """Print a LaTeX booktabs table of the full distribution, plus the ordered
    values needed to plot an empirical CDF."""
    rows = []
    for key, _l, _a, _b in HOPS:
        h = res["hops"][key]
        if h["stats"]:
            rows.append((h["label"], h["stats"]))
    if res.get("router_stats"):
        rows.append((ROUTER_ROW_LABEL, res["router_stats"]))
    if res.get("e2e_detection"):
        rows.append(("End-to-end (system call -> detection)",
                     res["e2e_detection"]))
    for key, _lbl, _a, _b in RESPONSE_ROWS:
        r = res.get("response", {}).get(key)
        if r and r["stats"]:
            rows.append((r["label"], r["stats"]))

    print("")
    print("=" * 78)
    print("FULL DISTRIBUTION")
    print("=" * 78)
    print("%-44s%8s%8s%8s%8s%8s%8s"
          % ("Stage", "min", "median", "IQR", "p90", "p95", "max"))
    print("-" * 78)
    for label, s in rows:
        print("%-44s%8.1f%8.1f%8.1f%8.1f%8.1f%8.1f"
              % (label[:44], s["min"], s["median"], s["iqr"],
                 s["p90"], s["p95"], s["max"]))

    if rows:
        for label, text in tail_caveats(rows[0][1]["n"]):
            print("  ! %s" % text)

    print("")
    print("--- LaTeX (booktabs) ---")
    print("\\begin{tabular}{lrrrrrr}")
    print("\\toprule")
    print("Stage & Min & Median & IQR & p90 & p95 & Max \\\\")
    print("\\midrule")
    for label, s in rows:
        print("%s & %.1f & %.1f & %.1f & %.1f & %.1f & %.1f \\\\"
              % (label.replace("->", "$\\rightarrow$"), s["min"], s["median"],
                 s["iqr"], s["p90"], s["p95"], s["max"]))
    print("\\bottomrule")
    print("\\end{tabular}")

    print("")
    print("--- ECDF data (paste into pgfplots, or plot elsewhere) ---")
    for label, s in rows:
        vals = s["values"]
        n = len(vals)
        pts = " ".join("(%.1f,%.4f)" % (v, float(i + 1) / n)
                       for i, v in enumerate(vals))
        print("")
        print("%% %s  (n=%d)" % (label, n))
        print("\\addplot coordinates {%s};" % pts)



def dump_values(res, prefix):
    """Write every observation, one column per stage, for plotting."""
    cols = []
    for key, _l, _a, _b in HOPS:
        h = res["hops"][key]
        if h["stats"]:
            cols.append((h["label"], h["stats"]["values"]))
    if res.get("e2e_detection"):
        cols.append(("end_to_end", res["e2e_detection"]["values"]))
    for key, _lbl, _a, _b in RESPONSE_ROWS:
        r = res.get("response", {}).get(key)
        if r and r["stats"]:
            cols.append((key, r["stats"]["values"]))
    if not cols:
        return
    out = "%s_distribution.csv" % prefix
    fh = open(out, "w")
    try:
        w = csv.writer(fh)
        w.writerow([c[0] for c in cols])
        depth = max(len(c[1]) for c in cols)
        for i in range(depth):
            w.writerow(["%.3f" % c[1][i] if i < len(c[1]) else "" for c in cols])
    finally:
        fh.close()
    print("")
    print("  wrote %s (every observation, sorted, one column per stage)" % out)


def dump_csv(res, prefix):
    safe = res["label"].replace(" ", "_").replace("/", "-")
    out = "%s_%s.csv" % (prefix, safe)
    fh = open(out, "w")
    try:
        w = csv.writer(fh)
        w.writerow(["doc_id", "path", "host"] + [k for k, _l, _a, _b in HOPS]
                   + ["detection"] + [k for k, _l, _a, _b in RESPONSE_ROWS])
        for e in res["kept"]:
            row = [e.id, e.path or "", e.host or ""]
            for _k, _l, x, y in HOPS:
                d = e.increment(x, y)
                row.append("" if d is None else "%.3f" % d)
            d = e.increment("audit", "alert")
            row.append("" if d is None else "%.3f" % d)
            for _k, _l, x, y in RESPONSE_ROWS:
                d = e.increment(x, y)
                row.append("" if d is None else "%.3f" % d)
            w.writerow(row)
    finally:
        fh.close()
    print("  wrote %s" % out)


# --------------------------------------------------------------------------

def main():
    p = argparse.ArgumentParser(
        description=__doc__,
        formatter_class=argparse.RawDescriptionHelpFormatter)
    p.add_argument("mode", choices=["keys", "describe", "compare", "dist"])
    p.add_argument("files", nargs="+")
    p.add_argument("--window-start")
    p.add_argument("--window-end")
    p.add_argument("--path", action="append", default=None)
    p.add_argument("--max-hop-s", type=float, default=32.0)
    p.add_argument("--join-tolerance-ms", type=float, default=1.0)
    p.add_argument("--router-max-gap-s", type=float, default=10.0)
    p.add_argument("--map", action="append", default=[], metavar="STAGE=FIELD")
    p.add_argument("--dump-csv")
    p.add_argument("--victim-offset-ms", action="append", type=float,
                   default=[], metavar="MS")
    args = p.parse_args()

    # one offset for every file, or one per file in the order given
    if not args.victim_offset_ms:
        offsets = [0.0] * len(args.files)
    elif len(args.victim_offset_ms) == 1:
        offsets = args.victim_offset_ms * len(args.files)
    elif len(args.victim_offset_ms) == len(args.files):
        offsets = list(args.victim_offset_ms)
    else:
        sys.exit("--victim-offset-ms: give one value, or one per file (%d)"
                 % len(args.files))
    offset_of = dict(zip(args.files, offsets))

    for spec in args.map:
        if "=" not in spec:
            sys.exit("--map expects STAGE=FIELD, got %r" % spec)
        stage, field = [x.strip() for x in spec.split("=", 1)]
        if stage not in FIELD_ALIASES:
            sys.exit("--map: unknown stage %r; choose from %s"
                     % (stage, ", ".join(sorted(FIELD_ALIASES))))
        FIELD_ALIASES[stage].insert(0, field)
        print("field override: %s -> %s" % (stage, field))

    if args.path is None:
        args.path = list(DEFAULT_PATHS)
    ws = to_epoch_ms(args.window_start) if args.window_start else None
    we = to_epoch_ms(args.window_end) if args.window_end else None
    if args.window_start and ws is None:
        sys.exit("could not parse --window-start %r" % args.window_start)
    if args.window_end and we is None:
        sys.exit("could not parse --window-end %r" % args.window_end)
    args.window_start, args.window_end = ws, we

    if args.mode == "keys":
        for f in args.files:
            inspect(f)
        return

    print("=" * 78)
    print("StateSentry Pipeline Latency Characterisation")
    print("=" * 78)
    print("Exclusion policy, applied identically to every file:")
    print("  window: %s .. %s" % (args.window_start or "none",
                                  args.window_end or "none"))
    print("  goal paths: %s" % ", ".join(args.path))
    print("  per-hop ceiling: %.0f s (benchmark repetition spacing)"
          % args.max_hop_s)
    print("  join tolerance: %.0f ms" % args.join_tolerance_ms)
    for f in args.files:
        print("  victim clock offset (victim - server): %+.1f ms for %s%s"
              % (offset_of[f], f,
                 "" if offset_of[f] else " (uncorrected)"))

    if args.mode == "dist":
        for f in args.files:
            res = analyse(f, "described", args, offset_of[f])
            distribution_table(res)
            if args.dump_csv:
                dump_values(res, args.dump_csv)
        return

    if args.mode == "describe":
        for f in args.files:
            res = analyse(f, "described", args, offset_of[f])
            report(res)
            if args.dump_csv:
                dump_csv(res, args.dump_csv)
        return

    if len(args.files) != 2:
        sys.exit("compare takes exactly two files: INDEXED then FILTER")
    a = analyse(args.files[0], "indexed implementation", args,
                offsets[0])
    b = analyse(args.files[1], "filter-based implementation", args,
                offsets[1])
    print("")
    print("--- INDEXED (ES/ElastAlert) PATH ---")
    report(a)
    print("")
    print("--- FILTER-BASED PATH ---")
    report(b)
    compare(a, b)
    if args.dump_csv:
        print("")
        dump_csv(a, args.dump_csv)
        dump_csv(b, args.dump_csv)


if __name__ == "__main__":
    main()
