# Agentic SecGen Pipeline — Roadmap

Goal: an AI agent (Claude Code, Claude Max subscription) can be handed a list of
modules/scenarios to create, deploy and verify, and work through it with minimal
human intervention. Every module is tested end-to-end on Proxmox, then reviewed
by a SecGen developer (Tom / Cliffe) before merging.

Open decisions are tracked in [OPEN_QUESTIONS.md](OPEN_QUESTIONS.md).

## Order of work (as of 2026-10-10)

1. **Phase 1 — Test harness**: lifecycle (1A) and `PostProvisionTest` (1B)
   are **done and on master** (2026-10-09): `secgen.rb test-scenario` /
   `test-module` build, test after net0 teardown + reboot, report to
   `test_results/` and destroy. Tests run in parallel and the destroy-on-failure path is checked on a real
   VM ([follow-ups](#phase-1-follow-ups)). **In-VM exploit tests (1C)** have
   their framework support done on branch `worktree-phase1c-distcc-exploit`
   (tier-3 `test_msf_exploit` from a `type=attack` VM, distcc verified end to
   end, skill documented) — not yet merged to master; follow-ups (negative
   case, a second module, HTTP exploit runners) remain.
2. **Phase 2 — Repo split**: move the pipeline into a private repo before more
   pipeline-only code lands in SecGen.
3. **Phase 3 — Coverage baseline**: run every existing test, audit, fill gaps.
4. Phases 4–10 follow in order (skills, tracker, gap analysis, orchestration,
   content, Windows, then VirtualBox and base boxes much later).

Edge cases and deferred items live in the [Backlog](#backlog) at the end.

## Baseline (as of 2026-10-07)

| Area | State |
|---|---|
| Config modules | 92 vulnerabilities (89 unix / 3 windows), 30 services (all unix), 121 utilities (101 / 20) |
| Modules with a `secgen_test` | 60 / 243 — mostly "port open" or one `vagrant ssh` grep; almost none exercise the exploit |
| Test scenarios | 12 in `scenarios/tests/` |
| Test harness | `lib/objects/post_provision_test.rb`, invoked by `post_provision_tests()` in `secgen.rb`. Unused for a long time; ordering not trustworthy |
| Proxmox client | `lib/helpers/proxmox_connection.rb` (rest-client). Has `qemu_agent_get_ip`; **no** `exec_qemu_guest`, no JSON POST |
| Metadata hints | 113 modules CyBOK-tagged; 228 use `requires`/`conflict` |
| Existing skills | `review-secgen-module`, `review-secgen-scenario`, `secgen-hackerbot`, `write-secgen-ctf-description`, `convert_hackerbot_to_hacktivity_lab_sheets`, `secgen-puppet` |

### Why the legacy tests broke on Proxmox

- Vagrant provisions over `net0` (DHCP, vmbr3). Scenario networking is static on
  vmbr1 + VLAN, which the SecGen host can't reach.
- Static IPs (and some modules, e.g. WordPress) only take effect after a reboot.
- For isolation, `net0` is removed after provisioning — so after the reboot there
  is no path from the host into the VM via SSH/Vagrant.
- Tests read the static IP from a Vagrantfile comment and `exit(0)` (silent pass)
  for DHCP systems.

### Constraints

- **No new external dependencies.** No new gems; no `qm` (the app talks to
  Proxmox remotely over the REST API).
- Dev Proxmox environment only for now.
- Linux first; Windows bases are a later phase (Phase 9).

---

## Phase 0 — Foundations (done)

- [x] Claude Code installed on the dev server; the pipeline runs from there.
- [x] Repo `CLAUDE.md`: conventions, how to build/test, what never to touch.
      Three-way split: shared `CLAUDE.md` (repo map, hard rules, cheap checks,
      skills index); server-specific `CLAUDE.local.md` (gitignored); procedures
      as Phase 4 skills.
- [x] Base images have `qemu-guest-agent` installed and Proxmox
      **Options → QEMU Guest Agent** enabled in the templates (needs full stop/start
      after changing, not a guest reboot). *2026-10-08: option ON for every
      template used by `modules/bases` (checked with `pve-check`).*
- [x] PVE version — **9.2.3** (2026-10-08).
- [x] Helper scripts in `scripts/` (see `scripts/README.md`): `secgen-run`
      (auto-increment prefix per name + VLAN 200–1000, creds from config),
      `pve-check`, `agent-check`, `secgen-test-run`.
- [x] **Proxmox credentials kept out of chat and shell history.**
  - [x] Config at `~/.config/secgen/secgen.conf` (deploy user, mode 600,
        outside the repo so every worktree can use the same absolute path),
        loaded with `--read-options`. Format: whitespace-separated flags only —
        no comments, no spaces in values (`secgen.rb:486-497`).
  - [x] Real values filled in by hand; creds removed from `~/.bash_history`.
  - [x] Generated projects no longer contain the Proxmox password: the
        Vagrantfile reads `ENV['SECGEN_PROXMOX_PASS']` (set by `secgen.rb`), and
        the `projects/*/systems` debug dump masks proxmox/ovirt/esxi passwords.
        Projects generated before this change still contain it.
  - [x] Claude Code `permissions.deny` rules (deploy user `~/.claude/settings.json`)
        for `~/.config/secgen/**`, `~/.git-credentials` and `projects/**/Vagrantfile`.
  - [x] GitHub PAT removed from the `thomashaw` remote URL and rotated.
  - Scoped API token: moved to the end of Phase 6 (before unattended runs).

## Phase 1 — Test harness (critical path, current)

**Transport: QEMU Guest Agent over the Proxmox REST API.** Works without any
guest network path, so it survives `net0` teardown and the post-provision reboot.

### Done

- [x] Guest-agent helpers in `proxmox_connection.rb`: `post_json`,
      `exec_qemu_guest` (decides on `exitcode`, polls `exec-status` with
      backoff), `qemu_agent_running?`, `qemu_agent_enabled?`. Fixed
      `qemu_agent_get_ip` nil crash and `delete()` TLS verify.
      Findings: guest commands run as **root** (use `runuser -u <user> -- ...`
      for user-context tests); each API call from the dev server takes **~3.1s**
      (see Backlog), so batch checks into one `sh -c` per module.
- [x] Backend detection + test context: Proxmox builds write
      `proxmox_test_context.json` (url + user, **no password** — that comes from
      `SECGEN_PROXMOX_PASS`); `PostProvisionTest#proxmox?` uses it plus
      `.vagrant/machines/*/proxmox/id` for node/VMID. Vagrant path still works.
- [x] `test_local_command` / `test_service_up` go over the guest agent on Proxmox.
- [x] **proftpd end-to-end on a fresh Debian 12 build (2026-10-08).** build →
      provision → net0 teardown → `secgen_test` over the agent. Broken config →
      `FAILED` (exit 1); fixed module (`IdentLookups` / `MultilineRFC2228`
      dropped) → `PASSED` (exit 0). First real detect-and-fix.

### Parallel streams

Each stream is one agent, one worktree, one branch off `master`. They touch
different files; the only shared surface is the **results contract** below,
which 1B owns and 1A/1C consume. It is agreed (below), so the three can
proceed independently. In practice 1B and 1A were done by one agent in
sequence (stacked branches `phase1b-post-provision-test` →
`phase1a-lifecycle-cli`, full history on `thomashaw`) and landed on master as
one squashed commit each. 1C branches from master.

**Results contract (agreed 2026-10-09):**

Results are kept per **project run** (not per scenario — scenarios are
randomly fulfilled, so the resolved `scenario.xml` is what was tested), in a
top-level, gitignored `test_results/` that survives VM and project teardown:

```
test_results/
  <project-id>/                    # e.g. tom-proftpd-03_SecGen20261009_140211
    summary.json                   # project-level report (1A)
    scenario.xml                   # copy of projects/<id>/scenario.xml (resolved)
    build.log                      # copy of log/<id> from secgen-run
    <system>/
      <module>.json                # per-module result (1B)
      <module>.log                 # the test's raw stdout/stderr
      evidence/<module>/*.txt      # collected on FAIL (journalctl, ss, ...)
```

- **Per-module JSON** (1B; tests write it into `projects/<id>/test_results/`,
  1A copies it out):
  `{module, module_path, system, backend, started_at, duration_s, status,
  tier_reached, results: [{tier, name, status, detail, evidence?}]}`.
  Each check's `status` is `PASS` / `FAIL` / `SKIP`. Module status: FAIL if any
  check failed, else SKIP if any skipped, else PASS — a SKIP is never a pass.
- **Tiers:** `1` provisioned, `2` service/tool works, `3` exploitable.
  `tier_reached` = highest tier whose checks all passed (`0` if none).
- **Exit codes:** `0` PASS, `1` FAIL, `2` SKIP (could not test — usually a
  harness/network problem, not the module). Human-readable `PASSED:` /
  `FAILED:` lines stay on stdout so legacy tests and scripts keep working.
- **Evidence:** collected automatically on FAIL — a standard set of commands
  (service status, recent journal, listening ports) batched into one guest-agent
  call (~6s), plus anything the test adds.
- **`summary.json`** (1A): `{project, source_scenario, secgen_commit,
  secgen_branch, started_at, finished_at, build: {status, attempts, log},
  status, counts: {PASS, FAIL, SKIP}, masked_secrets, systems: [{system, base,
  vmid, modules: [{module, status, tier_reached, result}]}]}`. A failed build
  is recorded under `build`, not as module failures. `test-scenario` exits with
  the worst status across modules (FAIL > SKIP > PASS).
- **Never copy** `Vagrantfile`, `systems`, `datastores`, the flags/hints XML,
  spoiler passwords or `proxmox_test_context.json`.
- **Secret masking on copy:** every file copied into `test_results/` has the
  Proxmox password replaced with `********` — in Ruby, reading
  `SECGEN_PROXMOX_PASS` and doing a literal (non-regex) replace, as
  `project_files_creator.rb` already does for the `systems` dump. Never `sed`
  with the password on a command line. The replacement count goes into
  `summary.json` (`masked_secrets`) so leaks get fixed at source.
- `test_results/` is pipeline output: after the Phase 2 split it belongs to
  (or is referenced from) the private pipeline repo, not public SecGen.

#### 1A — Lifecycle and CLI (`secgen.rb`, `scripts/`)

- [x] **Reorder:** on Proxmox (tests on), `build_vms` no longer runs tests in
      the vagrant loop: provision → halt → `net0` teardown (stop) → snapshot
      if `--snapshot` (pristine, pre-test) → start (the single full reboot) →
      wait for every guest agent (`SECGEN_AGENT_WAIT_BOOT`, default 600s) →
      settle (`SECGEN_TEST_SETTLE`, 30s) → tests → report → shut down again
      unless `--proxmox-post-boot`. A test **FAIL or SKIP destroys the VMs**
      (unless `--no-destroy-on-failure`) and is never retried: batches treat
      surviving VMs as good builds that get pulled into Hacktivity, and a SKIP
      is unverified (Tom, 2026-10-09). Other providers keep the old in-build
      runner, but a non-PASS there now also destroys without retrying
      (retries are for build flakes).
- [x] Results copied into `test_results/<project-id>/` per the contract
      (`lib/helpers/test_results.rb`): per-test log + JSON (synthesised as
      SKIP/FAIL if a test dies without writing one; per-test timeout
      `SECGEN_TEST_TIMEOUT`, 900s), evidence, resolved `scenario.xml`,
      `summary.json`; Proxmox password masked on copy, count in
      `masked_secrets`. `run` / `build-vms` with tests exit 1 on FAIL, 2 on SKIP.
      `secgen-run --test` attaches its log as `build.log` (masked in Ruby).
- [x] CLI: `secgen.rb test-scenario [xml]` and `test-module <path>` (generated
      one-system scenario: the module + an account on Debian 12,
      `--test-base` to change) — build, test, report, destroy VMs via the API,
      remove the project; `--keep-vms` to keep them. Exit 0/1/2 (a failed
      build is 1, recorded under `build`). Wrapped by `scripts/secgen-run
      --test` / `-m`; `scripts/secgen-destroy` cleans up kept projects.
- [x] Absorb the provisioning flake (OPEN_QUESTIONS #14): `secgen-run` always
      passes `--retries 1` (`SECGEN_RETRIES`), `test-*` default to 1, and when
      vagrant fails without naming a VM SecGen now destroys all and retries
      instead of giving up. (Not yet seen to fire: no flake in these builds.)
- Done (2026-10-09, `tom-p1a-01`): `scripts/secgen-run --test -s
  scenarios/tests/test_scenario_proftpd.xml` → build, net0 teardown, reboot,
  agent up, 2/2 PASS, `summary.json` + masked `build.log`, VM deleted and
  verified gone, project removed; exit 0, ~10 min. `secgen-run -m
  modules/services/unix/ftp/proftpd` (`tom-p1a-mod-01`, generated scenario) → same, PASS.

#### 1B — `PostProvisionTest` refactor (`lib/objects/post_provision_test.rb`)

- [x] Structured results per the contract (JSON + exit codes), keeping existing
      `secgen_test`s working unchanged. Legacy `PASSED:`/`FAILED:` lines pushed
      onto `outputs` are recorded as checks; all 60 tests compile (gnuscreen's
      lowercase class name fixed).
- [x] Replace silent passes with **SKIP**: no IP (agent or DHCP), no
      `SECGEN_PROXMOX_PASS`, an agent that never answers (`SECGEN_AGENT_WAIT`,
      default 60s) or a test that raises → SKIP, exit 2. The IP is resolved
      lazily via `agent/network-get-interfaces` (first non-loopback,
      non-link-local IPv4), so in-guest tests don't need one.
- [x] Fix the Vagrant-path quoting bug in `run_vagrant_ssh`: argv to
      `vagrant ssh -c` (no local shell). Not exercised on a VirtualBox VM.
- [x] **Test tiers**: `tier(n) { ... }` or `tier:` per check; helpers default
      to 1 (`test_local_command`) or 2 (service/HTTP/banner/command);
      `tier_reached` in the JSON.
- [x] Helpers for tier 2: `run_as_user` / `user:` (`runuser`, `su` fallback),
      `test_command_succeeds`, `test_banner`, `test_http`, and in-guest HTTP
      for `test_html_returned_content` (curl → wget → bash `/dev/tcp`), which
      fixes the 28 HTML checks that could never reach the VLAN from the host.
      Evidence (failed units, service status, journal, ports, processes, plus
      `add_evidence`) collected on FAIL in one guest call.
- [x] Update the `secgen-test-pipeline` skill with the new API.
- Done (2026-10-09, `tom-p1b-01`, Debian 12): proftpd test → PASS JSON
  (exit 0, IP from the agent after net0 teardown); legacy
  `parameterised_accounts` test unchanged → PASS; deliberate tier-3 failure →
  FAIL (exit 1, `tier_reached` 2, 6 evidence files); no password → SKIP
  (exit 2). The no-IP SKIP was checked offline against a stubbed agent.

#### Phase 1 follow-ups

- [x] Speed: module tests run side by side (`SECGEN_TEST_JOBS`, default 4;
      scripts sharing a module dir run in turn). Each guest call is still ~3s
      (see Backlog), but a scenario's tests now overlap: offline, 7 tests
      against a 3s-per-call stub went from 85s to 36s; on Proxmox two tests
      on one VM ran concurrently with no agent problems (24s for both).
- [x] Non-PASS destroy path on a real VM (2026-10-09, throwaway branch with
      proftpd's `Port` hard-coded to 2121, a plain `run` with tests on):
      Puppet succeeds, proftpd test FAILs (port 21 closed; evidence shows it
      listening on 2121), one build attempt (no retry), VM deleted and gone
      from the cluster, project + `test_results/` kept, exit 1. With
      `--no-destroy-on-failure` the VM was kept (exit 1).
      Note: putting `IdentLookups` back makes Puppet's own service restart
      fail, so that breakage is a **build** failure (retried, then destroyed,
      or kept with `--no-destroy-on-failure`) — also checked, and it behaves.
- The no-IP SKIP and the destroy-all-and-retry on the DHCP flake
  (OPEN_QUESTIONS #14) were only checked offline; not worth forcing — note
  them when they turn up in normal runs. VirtualBox items moved to Phase 10.

#### 1C — Exploit tests from inside a VM (tier 3)

Network-side and exploit tests need something on the scenario VLAN; run them
*from inside a VM* via the guest agent.

**Framework support landed (2026-10-10, `distcc_exec`, branch
`worktree-phase1c-distcc-exploit`).** Decisions #8/#9 taken: an **attacker VM
per test scenario** (a base of `<type>attack</type>`, i.e. Kali/MSF) on the
scenario's own private network, and **Metasploit** is acceptable inside that
attacker VM (it ships in the base; the SecGen host stays dependency-free).
Tier-3 exploit testing is now a reusable part of `PostProvisionTest`, not a
one-off: a new exploit test is a few lines. Verified end to end on three real
two-VM Proxmox builds (`deploy-distcc-01..03`) — the last two a full
`test-scenario` from committed source that PASSed at `tier_reached` 3 and then
auto-destroyed. The Kali attacker runs `exploit/unix/misc/distcc_exec` against
the Debian target; proof is a sentinel the target-side guest agent reads back
(`uid=119(distccd)`).

- [x] Attacker/runner VM in test scenarios (Kali/MSF base), guest agent working
      after net0 teardown. (`scenarios/tests/test_scenario_distcc.xml`:
      Debian 12 target + Kali attacker on one 10.88.0.0/24 private network.)
- [x] `PostProvisionTest` cross-system helpers: `run_on_system` /
      `other_system_ip` / `system_present?`, each reaching a sibling VM over
      *its own* QEMU Guest Agent (node/VMID from
      `.vagrant/machines/<name>/proxmox/id`). Proxmox only.
- [x] Attacker discovered by base `<type>attack</type>` (`attack_system`, read
      from the resolved project `scenario.xml` + the base metadata), not a
      hard-coded system name. Both Kali bases already declare the type. Tier 3
      SKIPs if there is no attacker, so single-VM module runs still work.
- [x] Reusable exploit runner `test_msf_exploit(module, rhost:, rport:,
      collect:, options:, tier:)`: resolves the attacker, runs `msfconsole
      -q -x` on it via `run_on_system`, and verifies RCE by the sentinel the
      target's agent reads back. Two gotchas baked in — export `HOME`
      (guest-agent exec has none, so msfconsole's rb-readline aborts) and a
      **no-session** payload (`cmd/unix/generic` + `CMD`) so there's no
      reverse/bind shell to race.
- [x] Spike on one network vuln module end to end (distcc; exploit → PASS),
      refactored onto `test_msf_exploit` (a 4-line call).
- [x] Document it: `secgen-test-pipeline` skill gained a "Tier 3: exploit from
      an attacker VM" section (scenario pattern, the helper, the sentinel
      proof, the gotchas, and the non-Metasploit `run_on_system` path).
- [ ] Negative case: vuln removed → FAIL (not yet run for distcc; the tier-3
      check already FAILs cleanly when the sentinel is absent).
- [ ] Second module: first reuse of `test_msf_exploit` on another vuln to
      confirm the API generalises (ideally a different payload/verification).
- [ ] HTTP-request exploit runners (a small helper library) for web vulns that
      have no Metasploit module, on the same attacker-VM / guest-agent shape.
- Depended on 1B for tier reporting (done).

## Phase 2 — Repo split

Suggested by Cliffe (2026-10-09). Move the automation pipeline out of the SecGen
repo into a separate **private** repo, so SecGen stays focused on the
generator, modules and scenarios. Related repos are opened together in one
multi-root VS Code workspace, the same way Hacktivity, SecGen, BreakEscape and
HacktivityLabSheets are already developed side by side.

Do this after Phase 1 (the harness stays in SecGen) and before Phases 5–7
(tracker, gap analysis, orchestration), which would otherwise add more
pipeline-only code to SecGen.

Proposed layout (to agree; see OPEN_QUESTIONS #15):

| Repo | Visibility | Holds |
|---|---|---|
| `SecGen` | public | Core generator, modules, scenarios, `secgen_test`s, the test harness (`PostProvisionTest`, `proxmox_connection.rb`, which generated projects copy), `CLAUDE.md`, and the skills useful to any contributor (module/scenario review, `secgen-puppet`, hackerbot, CTF descriptions). |
| `secgen-pipeline` (name TBD) | private | Orchestration and agent workflows, issue picker / claim protocol, gap analysis and backlog generation, coverage inventory and reports, metrics, pipeline-specific skills, run configs, and this roadmap and open questions. |

- [ ] Agree what goes where. Open points: `scripts/` (generic Proxmox dev
      helpers, possibly useful to any contributor → SecGen?), and the
      `secgen-test-pipeline` skill (documents the harness, but also the
      agent loop).
- [ ] Define the interface between them: the pipeline drives SecGen only
      through its CLI (`secgen.rb`, `scripts/`, the Phase 1A `test-*`
      commands) and a path to a SecGen checkout, not by reaching into `lib/`
      internals.
- [ ] Create the private repo; move `agentic_pipeline/` and pipeline-only code
      with history where practical.
- [ ] Shared `.code-workspace` file covering SecGen + the pipeline repo (and
      the other related repos if wanted).
- [ ] Update `CLAUDE.md` and the skills index to point at the new
      locations; make sure Claude Code picks up skills/CLAUDE.md from both repos
      in a multi-root session.

## Phase 3 — Coverage baseline

- [ ] **Run all 60 existing tests** → baseline report; failures become issues.
- [ ] **Exploit-capability audit.** For every vulnerability module, classify how it
      can be verified with a *real* exploit:
      Metasploit module exists / web-app exploit (HTTP request sequence we'd write) /
      local privesc script / credential-based / needs bespoke tooling / not
      automatable. Output: what the harness (after 1C) can do and what to add.
- [ ] **Utility tests run the tool from a terminal** (as the intended user, via the
      guest agent) — catches PATH, missing libraries and broken installs, not just
      "package present".
- [ ] Service tests: port open *and* a real protocol interaction (banner, login,
      fetch a page).
- [ ] Inventory script: per module → has test? which tiers? which test scenario?
      last pass date. This is the coverage dashboard.
- [ ] Test scenarios: auto-generate one minimal scenario per module, then pack
      compatible modules together (respecting `conflict`) to cut VM count.
- [ ] Generators/encoders (~190) — local unit tests, no VM needed.
- [ ] Write the missing tests (agents, using `secgen-test-pipeline` and the
      Phase 4 skills as they land — first real use of the pipeline).
- [ ] **Only then: turn tests on in real batches.** `lib/batch/batch_secgen.rb`
      prepends `--shutdown --no-tests` to every queued job, so batches never
      run the tests or reach destroy-on-FAIL/SKIP. Keep it that way until the
      suite has full, comprehensive coverage and a clean baseline (Tom,
      2026-10-09): with gaps or flaky tests, turning them on would destroy
      good builds. When dropped, non-PASS builds are destroyed and never reach
      Hacktivity (~1 min extra a build).

## Phase 4 — Claude skills

Derived from analysis of existing modules + `README-Modules-*.md`. Each skill is
validated by having a fresh agent rebuild a known module and diffing the result.

- [ ] Write a SecGen module (Puppet + Ruby) — extend `secgen-puppet` rather than duplicate.
- [ ] Build a basic scenario.
- [ ] Provision a VM (Proxmox dev environment).
- [ ] Write `secgen_test`s (all three tiers).
- [ ] Test a module / scenario (run, read results, triage).
- [ ] Add `requires` / `conflict` when incompatibilities are found.
- Reuse `review-secgen-module` / `review-secgen-scenario` as a pre-human-review gate.

## Phase 5 — Issue tracker (GitHub Issues)

Agreed with Cliffe: use GitHub Issues as the central tracker.

- [ ] Labels: `agent:ready`, `agent:claimed`, `needs-human`, `type:bug`,
      `type:new-module`, `type:gap`, `type:test-failure`, plus module-area labels.
- [ ] Issue templates; `needs-human` issues must include a summarised action plan.
- [ ] Claim protocol (assign + label, with a stale-claim timeout) so agents never
      double-pick.
- [ ] `next-issue` picker script (`gh` CLI) ordered by priority.

## Phase 6 — Gap analysis

- [ ] Coverage matrix against: CWE/vulnerability classes, CyBOK knowledge areas
      (reuse existing tags), services/protocols, platforms, network topologies.
- [ ] Auto-create ranked `type:gap` / `type:new-module` issues.
- [ ] **Scoped Proxmox API token** (OPEN_QUESTIONS #4) — before Phase 7 runs
      agents unattended. Today the pipeline logs in with a user password; a
      privilege-separated token limits what a leaked secret can do (only the
      agent pool, no template changes, guest-agent exec only on test VMs) and
      can be revoked/expired independently.
  - [ ] `PVEAPIToken=` header auth in `proxmox_connection.rb` (no ticket/CSRF).
  - [ ] Dedicated user + pool + minimal role (VM allocate/config/power, guest
        agent on the pool; audit-only on templates).
  - [ ] Config / `secgen-run` / `secgen-test-run` take the token instead of the
        password.

## Phase 7 — Orchestration

Per issue: pick → worktree/branch → write module + test → schema validation
(`lib/CyBOK/validate_xml_*`) → Proxmox build + tests → review skills → draft PR,
or `needs-human` with summary. Run headless on the dev server (`claude -p` /
scheduled loop), paced to Max-plan limits, with a concurrency cap on Proxmox.

- [ ] A reserved VMID range / pool for agent builds (same pool as the API
      token), and a sweeper that destroys orphaned test VMs.

## Phase 8 — Content production and feedback

- [ ] Generate new modules/scenarios from the gap backlog.
- [ ] Human review (Tom / Cliffe); review findings → issues; recurring issues →
      skill updates.
- [ ] Metrics: first-attempt pass rate, human interventions per module, review
      defects per module.

## Phase 9 — Windows (after Linux pipeline is fully working)

- [ ] Windows bases with the virtio-win QEMU Guest Agent service.
- [ ] `exec_qemu_guest(..., windows: true)` paths in the test harness.
- [ ] Extend tests/skills to the 3 Windows vulnerabilities and 20 utilities, then new Windows content.

## Phase 10 — VirtualBox parity and base boxes (much later)

The pipeline targets Proxmox; VirtualBox (and other Vagrant providers) keep the
legacy in-build test runner. Pick these up only once everything above is done.

- [ ] Run the harness on a VirtualBox VM: check the `run_vagrant_ssh` argv
      quoting fix and the vagrant-ssh paths of the 1B helpers (HTTP/banner
      checks go from the host there, not the guest).
- [ ] DHCP systems: network checks can't find an IP, so they SKIP and the VMs
      are now destroyed. Resolve the IP (e.g. via `vagrant ssh`) or accept it.
- [ ] Optionally move VirtualBox onto the Proxmox-style lifecycle (tests after
      the final reboot).
- [ ] Base boxes: update the existing bases (Proxmox templates and VirtualBox
      boxes) to current releases, and add more bases for both, with the QEMU
      Guest Agent baked into the Proxmox templates.

---

## Backlog

Edge cases and deferred items. Pick up when convenient or when they block something.

- [ ] **`dirtycow` local-privesc test** via the guest agent. Real edge case
      (kernel-version dependent); add a test and check it works once the
      tier-3 harness (1B/1C) exists.
- [ ] `debian_wheezy_desktop_kde` and `debian_wheezy_server` bases reference
      Proxmox template `DebianWheezyDesktopKDE2`, which doesn't exist on the
      cluster (closest: `DebianDesktopKDE` / `DebianWheezyServer`). Fix or drop.
- [ ] Helper scripts use curl `-k` (matching SecGen's `verify_ssl: false`);
      trust the PVE CA instead.
- [ ] Proxmox API latency: ~3.1s per call from the dev server (server-side wait
      after TLS, via the Squid proxy). Investigate before test suites get large.
- [ ] Root-cause the provisioning DHCP flake (OPEN_QUESTIONS #14) with Cliffe.
