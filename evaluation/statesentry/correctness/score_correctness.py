#!/usr/bin/env python3
"""
StateSentry detection correctness test: scorer.

Matches filter-based detections to the case windows recorded by
run_correctness_cases.sh, and reports how many legitimate accesses were
detected and how many non-qualifying accesses were (wrongly) detected.

  python3 score_correctness.py CASES.csv DETECTIONS.txt [--out RESULTS.csv]

CASES.csv       written by run_correctness_cases.sh on the monitored VM.
DETECTIONS.txt  filter detections covering the run, one per line, in the
                pipe format of benchmark.sh's FILTER DETECTIONS section:
                  audit_time|logstash_receive|logstash_normalised|detect_time|path|host
                A whole benchmark-style results file also works: only lines
                in a FILTER DETECTIONS section (or bare pipe lines) are read.

A detection belongs to a case if its audit timestamp (the system call time,
taken from the same clock as the case window) falls inside that window.
Runs on Python 3.6 and later. No third-party dependencies.
"""
import argparse
import csv
import sys
from collections import OrderedDict, defaultdict
from datetime import datetime

WINDOW_MARGIN_MS = 200  # tolerance either side of a case window


def to_ms(ts):
    ts = ts.strip().replace("Z", "+00:00")
    # Python 3.6 fromisoformat is unavailable; strptime needs fixed fraction
    if "." in ts:
        head, rest = ts.split(".", 1)
        frac, tz = rest[:rest.index("+")], rest[rest.index("+"):]
        ts = "%s.%s%s" % (head, (frac + "000000")[:6], tz.replace(":", ""))
        fmt = "%Y-%m-%dT%H:%M:%S.%f%z"
    else:
        ts = ts[:-6] + ts[-6:].replace(":", "")
        fmt = "%Y-%m-%dT%H:%M:%S%z"
    return datetime.strptime(ts, fmt).timestamp() * 1000.0


def load_cases(path):
    with open(path, newline="") as fh:
        rows = list(csv.DictReader(fh))
    for r in rows:
        r["start_ms"] = to_ms(r["start_utc"])
        r["end_ms"] = to_ms(r["end_utc"])
        r["detections"] = []
    return rows


def load_detections(path):
    dets, in_section, saw_header = [], True, False
    with open(path, errors="replace") as fh:
        for line in fh:
            s = line.strip()
            if s.startswith("===") and s.endswith("==="):
                saw_header = True
                in_section = "FILTER DETECTION" in s.upper()
                continue
            if not s or "|" not in s or (saw_header and not in_section):
                continue
            parts = s.split("|")
            try:
                dets.append({"audit_ms": to_ms(parts[0]), "audit": parts[0],
                             "path": parts[4] if len(parts) > 4 else "",
                             "host": parts[5] if len(parts) > 5 else ""})
            except (ValueError, IndexError):
                print("  ignored unparseable line: %r" % s[:100], file=sys.stderr)
    return dets


def outcome(truth, detected):
    if truth == "detect":
        return "TP" if detected else "FN"
    return "FP" if detected else "TN"


def main():
    ap = argparse.ArgumentParser(description=__doc__.split("\n\n")[0])
    ap.add_argument("cases")
    ap.add_argument("detections")
    ap.add_argument("--out", help="write per-run results CSV here")
    a = ap.parse_args()

    cases = load_cases(a.cases)
    dets = load_detections(a.detections)
    run_start = min(c["start_ms"] for c in cases) - WINDOW_MARGIN_MS
    run_end = max(c["end_ms"] for c in cases) + WINDOW_MARGIN_MS

    unassigned = []
    for d in dets:
        if not run_start <= d["audit_ms"] <= run_end:
            continue  # belongs to some other run; ignore silently
        hit = [c for c in cases
               if c["skipped"] != "yes"
               and c["start_ms"] - WINDOW_MARGIN_MS <= d["audit_ms"] <= c["end_ms"] + WINDOW_MARGIN_MS]
        if hit:
            for c in hit:
                c["detections"].append(d)
        else:
            unassigned.append(d)

    # ---- per-run outcomes ------------------------------------------------
    for c in cases:
        c["outcome"] = "SKIP" if c["skipped"] == "yes" else outcome(c["ground_truth"], bool(c["detections"]))

    # ---- per-case summary (across repetitions) ---------------------------
    by_case = OrderedDict()
    for c in cases:
        by_case.setdefault(c["case_id"], []).append(c)

    print("=" * 96)
    print("PER CASE (detected / repetitions run)")
    print("=" * 96)
    print("%-4s %-9s %-9s %-8s %-7s %-6s %-9s %s"
          % ("ID", "CATEGORY", "TRUTH", "READER", "LISTED", "DET", "OUTCOME", "DESCRIPTION"))
    flaky = []
    case_outcome = OrderedDict()
    for cid, runs in by_case.items():
        ran = [r for r in runs if r["skipped"] != "yes"]
        c0 = runs[0]
        if not ran:
            case_outcome[cid] = "SKIP"
            print("%-4s %-9s %-9s %-8s %-7s %-6s %-9s %s"
                  % (cid, c0["category"], c0["ground_truth"], c0["reader"],
                     c0["reader_listed"], "-", "SKIP", c0["description"]))
            continue
        k = sum(1 for r in ran if r["detections"])
        outs = {r["outcome"] for r in ran}
        if len(outs) > 1:
            flaky.append(cid)
            label = "MIXED"
        else:
            label = outs.pop()
        case_outcome[cid] = label
        print("%-4s %-9s %-9s %-8s %-7s %-6s %-9s %s"
              % (cid, c0["category"], c0["ground_truth"], c0["reader"],
                 c0["reader_listed"], "%d/%d" % (k, len(ran)), label, c0["description"]))

    # ---- totals ----------------------------------------------------------
    should = [cid for cid, rs in by_case.items() if rs[0]["ground_truth"] == "detect" and case_outcome[cid] != "SKIP"]
    shouldnt = [cid for cid, rs in by_case.items() if rs[0]["ground_truth"] == "no_detect" and case_outcome[cid] != "SKIP"]
    tp = [c for c in should if case_outcome[c] == "TP"]
    fn = [c for c in should if case_outcome[c] == "FN"]
    fp = [c for c in shouldnt if case_outcome[c] == "FP"]
    tn = [c for c in shouldnt if case_outcome[c] == "TN"]
    skipped = [cid for cid, o in case_outcome.items() if o == "SKIP"]
    reps = max(int(c["rep"]) for c in cases)

    print("")
    print("=" * 96)
    print("SUMMARY (by case; a case counts once whatever its repetitions)")
    print("=" * 96)
    print("  Should detect:     %d case(s): detected %d, missed %d" % (len(should), len(tp), len(fn)))
    print("  Should not detect: %d case(s): correctly silent %d, false detections %d"
          % (len(shouldnt), len(tn), len(fp)))
    print("  Skipped (tool not installed): %s" % (", ".join(skipped) or "none"))
    print("  Inconsistent across repetitions: %s" % (", ".join(flaky) or "none"))

    fn_unlisted = [c for c in fn if by_case[c][0]["reader_listed"] == "no"]
    fn_other = [c for c in fn if c not in fn_unlisted]
    print("")
    print("  Misses attributable to the invoking-process restriction (reader not listed): %s"
          % (", ".join("%s(%s)" % (c, by_case[c][0]["reader"]) for c in fn_unlisted) or "none"))
    print("  Misses NOT explained by the restriction (investigate): %s" % (", ".join(fn_other) or "none"))
    print("  False detections (investigate): %s"
          % (", ".join("%s: %s" % (c, by_case[c][0]["description"]) for c in fp) or "none"))
    if unassigned:
        print("")
        print("  ! %d detection(s) inside the run but outside every case window "
              "(setup activity, or a window too narrow):" % len(unassigned))
        for d in unassigned[:10]:
            print("      %s  %s" % (d["audit"], d["path"]))
    if flaky:
        print("  ! Inconsistent cases need a look before reporting: %s" % ", ".join(flaky))

    # ---- draft sentence --------------------------------------------------
    print("")
    print("=" * 96)
    print("DRAFT SENTENCE FOR SECTION V.B (check before use)")
    print("=" * 96)
    n_total = len(should) + len(shouldnt)
    fp_text = "no false detections" if not fp else "%d false detection(s)" % len(fp)
    if fn and not fn_other:
        fn_text = ("; the remaining %d were missed, each because the reading process was not "
                   "among those the rule accepts" % len(fn))
    elif fn:
        fn_text = ("; %d were missed, %d of them not explained by the reader restriction"
                   % (len(fn), len(fn_other)))
    else:
        fn_text = ""
    print("  A scripted test of %d access variants, each repeated %d times, detected %d of %d "
          "legitimate accesses to the target%s. Across %d non-qualifying accesses, including "
          "decoy paths, failed accesses and other accounts, there were %s."
          % (n_total, reps, len(tp), len(should), fn_text, len(shouldnt), fp_text))
    if flaky or skipped:
        print("  (Not yet reportable as written: resolve the skipped/inconsistent cases above first.)")

    if a.out:
        with open(a.out, "w", newline="") as fh:
            w = csv.writer(fh)
            w.writerow(["case_id", "rep", "category", "ground_truth", "reader", "reader_listed",
                        "description", "exit_code", "skipped", "start_utc", "end_utc",
                        "n_detections", "outcome"])
            for c in cases:
                w.writerow([c["case_id"], c["rep"], c["category"], c["ground_truth"], c["reader"],
                            c["reader_listed"], c["description"], c["exit_code"], c["skipped"],
                            c["start_utc"], c["end_utc"], len(c["detections"]), c["outcome"]])
        print("\nwrote %s" % a.out)

    # non-zero exit if anything needs investigation, for scripted use
    return 1 if (fp or fn_other or flaky) else 0


if __name__ == "__main__":
    sys.exit(main())
