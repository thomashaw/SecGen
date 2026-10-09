---
name: hackerbot-lab-testing
description: End-to-end testing and improvement loop for a SecGen Hackerbot lab and its labsheet - review the bot template and labsheet for bugs and student pitfalls, regression-test the bot's checks offline, write an automated tester that solves the lab on real VMs (driving Hackerbot over IRC, making deliberate student mistakes and recording every hint), serve it to the VMs from hackerbot:8080, build on Proxmox and run it via the QEMU guest agent, fix and re-run until clean, give the owner a manual test guide, then ship a clean commit with no test files. Use whenever asked to test, verify, solve, play through, harden or improve a Hackerbot lab or its labsheet, to check the hints/feedback are good, to "build the VMs and test them", or to talk to Hackerbot from a script ("say ready to the bot", "what does the bot reply") - even if only one of these steps is asked for. Pairs with secgen-hackerbot (how bots work) and secgen-test-pipeline (Proxmox builds).
---

# Hackerbot lab testing loop

A Hackerbot lab chains tasks (each `ready` checks the student's VM over SSH and replies with a flag or a
hint), and a labsheet tells students what to type. Both rot: commands stop working on new OS versions,
hints mislead, regexes over random values misfire, and a student who slips at task 4 is stuck at task 11.
This loop finds those problems with evidence, fixes them, and proves the fixes on fresh VMs - without
shipping any of the test machinery.

Read first: `secgen-hackerbot` (bot XML semantics, `hb_check.rb`) and, for builds, `secgen-test-pipeline`.
Then keep `references/pitfalls.md` open - every item there cost a build cycle once.

## Tools in this skill (use these instead of rewriting them)

| Script / asset | What it does |
|---|---|
| `scripts/hackerbot_irc.py` | Talk to a bot like a student: `list`, `goto N`, `ready [N]`, `answer X`, `say ...`; CLI or importable (`Hackerbot` class, `Reply` with `.verdict .fyi .flags`). Any lab. |
| `scripts/fake_hackerbot.py` | Fake bot from a rendered `bot.xml` (real prompts, canned passes) for offline IRC tests. |
| `scripts/hb_lint.rb` | Syntax-check every bot command the way the bot runs it (dash for pre/post_shell, bash for base64 payloads); lists flags hidden in payloads. |
| `scripts/hb_sim.rb` | Replay an attack's conditions against a given output, or run its real check against a fake dir tree (`--root`). |
| `scripts/check_guide.rb` | Manual guide: every command block parses; every expected bot phrase exists in the template. |
| `scripts/py311_fstring_check.py` | Tester f-strings that break on Python < 3.12 (lab VMs). |
| `scripts/tester_dry_run.py` | Run a tester's `run_all()` against the fake bot with all shell commands syntax-checked instead of executed. |
| `scripts/guest-exec` | Run a shell command in a Proxmox VM via the guest agent (creds never printed). |
| `scripts/build_dev_pages.sh` + `assets/dev_page_template.html` | Markdown guide → self-contained HTML (copy buttons, saved checkboxes) to serve to VMs. Needs pandoc. |
| `assets/lab_test_skeleton.py` | Starting point for a lab's on-VM tester (report format, sudo/SSH setup, bot calls). |

Ruby scripts need nokogiri: run them with the repo's bundle (`BUNDLE_PATH=<main checkout>/vendor/bundle
bundle exec ruby ...`).

## Where to work

Keep test machinery separate from what ships. Work on a feature branch in a worktree (e.g.
`<lab>-lab`), with all dev-only material in one directory (`<lab>_dev/` or similar: roadmap, guides,
tester, regression script, reports) plus the DEV hackerbot_webclient resources. Only the bot template and
scenario go to master in the end (phase 8). Commit and push the branch as you go; ask before any rebase.

## Phase 0 - Locate the labsheet

**TODO:** the labsheets live in a separate repository (HacktivityLabSheets, `_labs/<category>/...`) that
isn't wired into this setup yet. For now ask the owner where the current labsheet is (last time they
copied it to the repo root as `<lab>_lab.md`). Once found, put a working copy on the feature branch so
edits are tracked; the owner syncs it back to the labsheet repo themselves. Always check whether they
have edited it in another session before changing it.

## Phase 1 - Review

1. Understand the lab (secgen-hackerbot "Understanding a lab"): render with `hb_check.rb --scenario`,
   read the scenario, the template and the labsheet side by side, attack by attack.
2. Trace each task as a **student who makes mistakes**: wrong trailing slash, missing sudo, typo'd path,
   doing tasks out of order, skipping one, redoing one late, restoring in the wrong order. For each: what
   does the bot say, and can they recover without rebuilding?
3. Also check: regexes over output containing random values; conditions that match too broadly;
   answers derivable from the VM; whether each value a student submits is random per build; labsheet
   commands against the current OS (OpenSSH, rsync, Debian versions change behaviour).
4. Write findings into the roadmap with IDs and proposed fixes. Present them and **agree what to change
   before changing it** - the owner decides; some "bugs" are deliberate teaching.

## Phase 2 - Confirm cheaply

- Logic bugs (regexes, condition order): confirm offline with `hb_sim.rb` on a rendered `bot.xml`.
- Behaviour claims (shell/rsync/scp semantics): write a short verification walkthrough with exact
  commands and "bug is real if…" expectations; better, put them in the automated tester (phase 4).

## Phase 3 - Fix the bot, with offline regression

- Prefer structured check output (`OK|MISSING|UNEXPECTED <label>`, `RESULT PASS|FAIL`, stderr
  discarded) and one specific hint per mistake, naming the recovery (`goto N` …). See pitfalls.
- Keep the attack count stable so `goto N` references in the labsheet stay valid.
- Write `regress.rb` for the lab (pattern in `references/tester-design.md` §2) and run after every change,
  together with `hb_check.rb` (0 errors) and `hb_lint.rb` (0 problems).

## Phase 4 - The automated tester

Copy `assets/lab_test_skeleton.py`, fill `discover/clean_start/run_all` (design notes:
`references/tester-design.md` §3). Before any VM sees it: `tester_dry_run.py` against
`fake_hackerbot.py`, and the Python-compatibility check. Serve it plus `hackerbot_irc.py` from
hackerbot:8080 (§4).

## Phase 5 - Build and run on Proxmox

1. Build master + the lab's files (pitfalls: never from an old branch base). Cheap checks first
   (CLAUDE.md), then `scripts/secgen-run --dry-run`, then the real build in the background (~15-20 min).
2. `guest-exec` to find the desktop, check the agent, run `--irc-check`, start the full run detached,
   poll, fetch the report (§5). Save it as `reports/runN_<date>.txt` on the branch.
3. Builds and VMs use shared hardware: only what you were asked to build; destroy them when done.

## Phase 6 - Fix and re-run

Triage the report: every PROBLEM is either a lab bug (fix template/labsheet → phase 3 → rebuild) or a
tester bug (fix tester → `tester_dry_run.py` → re-run). A finding the owner hits manually that the tester
missed is a missing test - add it. Repeat until 0 PROBLEMs. Text-only template changes covered by the
offline regression don't need a rebuild; anything touching commands, shell syntax or VM state does.

## Phase 7 - Manual test guide

Automated checks can match phrases but can't judge whether a hint helps. Write `MANUAL_TEST.md`
(§6), check it with `check_guide.rb`, serve it as a page, and ask the owner which "hint clear?" boxes
they left unticked. Answer questions mid-test with exact commands for their VM state.

## Phase 8 - Ship

- Confirm the labsheet working copy has the owner's latest edits (it's theirs to sync; don't ship it), and
  that it follows the site's rendering rules (pitfalls: "Labsheet") - especially that `> Hackerbot:` blocks
  are summaries, not copies of the bot's prompts.
- `git diff --stat master...<branch>`: ship **only** the bot template (and scenario if changed) - no
  dev directory, no reports, no walkthroughs/solutions, no hackerbot_webclient DEV resources, no labsheet.
  Grep the shipped files for references to dev files.
- Check master is in sync with upstream (owner's rule: commits on top, no merges), apply with
  `git checkout <branch> -- <files>`, run hb_check/hb_lint/regress against master's copy, and commit one
  squashed commit describing the changes. **The owner pushes master.** Mirror later fixes on the branch
  first, then cherry-pick.
- Randomness review before shipping: every value a student submits or can share must vary per build
  (flags from flag generators, `SecureRandom` values ≥ hex(4) for answers, not visible elsewhere on the VM).

## Phase 9 - Clean up

Destroy the VMs with the repo's `scripts/secgen-destroy <projects/<id>>` (only the VMs in that project's
`.vagrant/machines/*/proxmox/id`; it checks they're gone and removes the project dir); remove temporary build worktrees (`git worktree remove`; ask
before `--force` on anything not yours); delete scratch files. Leave the feature branch and its dev
directory - it's the record of what was tested.
