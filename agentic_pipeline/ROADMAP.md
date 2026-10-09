# Agentic SecGen Pipeline — Roadmap

Goal: an AI agent (Claude Code, Claude Max subscription) can be handed a list of
modules/scenarios to create, deploy and verify, and work through it with minimal
human intervention. Every module is tested end-to-end on Proxmox, then reviewed
by a SecGen developer (Tom / Cliffe) before merging.

Open decisions are tracked in [OPEN_QUESTIONS.md](OPEN_QUESTIONS.md).

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
- Linux first; Windows bases are a later phase (Phase 8).

---

## Phase 0 — Foundations

- [ ] Install Claude Code on the dev server; run the pipeline from there (it needs
      Proxmox API access, the SecGen toolchain and to stay up for long runs).
- [x] Add a repo `CLAUDE.md`: conventions, how to build/test, what never to touch.
      Three-way split: shared `CLAUDE.md` (repo map, hard rules, cheap checks,
      skills index); server-specific `CLAUDE.local.md` (gitignored); procedures
      as Phase 3 skills.
- [x] Confirm base images have `qemu-guest-agent` installed and Proxmox
      **Options → QEMU Guest Agent** enabled in the templates (needs full stop/start
      after changing, not a guest reboot). *2026-10-08: installed; option ON for
      every template used by `modules/bases` (checked with `pve-check`).*
- [x] Record the PVE version — **9.2.3** (2026-10-08).
- [ ] *(Deferred)* `debian_wheezy_desktop_kde` and `debian_wheezy_server` bases reference
      Proxmox template `DebianWheezyDesktopKDE2`, which doesn't exist on the
      cluster (closest: `DebianDesktopKDE` / `DebianWheezyServer`). Fix or drop.
- Helper scripts (now in `scripts/`, see `scripts/README.md`):
  `secgen-run` (auto-increment prefix per name + VLAN 200–1000, creds from
  config) and `pve-check` (read-only: PVE version + guest-agent option per
  template). Both use curl `-k`, matching SecGen's `verify_ssl: false`; TODO
  trust the PVE CA instead.
- [ ] **Keep Proxmox credentials out of chat and shell history.** Now that an
      external LLM sees commands and output, creds must not appear on the command
      line or in files Claude reads.
  - [x] Stub config at `~/.config/secgen/secgen.conf` (deploy user, mode 600,
        outside the repo so every worktree can use the same absolute path).
        Loaded with `ruby secgen.rb --read-options ~/.config/secgen/secgen.conf ...`.
        Format: whitespace-separated flags only — no comments, no spaces in values
        (`secgen.rb:486-497` splits the file on whitespace).
  - [ ] Fill in real values (by hand, not via Claude); remove creds from
        `~/.bash_history`.
  - [x] Generated projects no longer contain the Proxmox password: the
        Vagrantfile reads `ENV['SECGEN_PROXMOX_PASS']` (set by `secgen.rb`), and
        the `projects/*/systems` debug dump masks proxmox/ovirt/esxi passwords.
        Verified with a fake password + `build-project`: 0 files contain it.
        Running `vagrant` by hand in a project dir now needs
        `SECGEN_PROXMOX_PASS` exported. Projects generated before this change
        still contain the password in `Vagrantfile` and `systems`.
  - [x] Claude Code `permissions.deny` rules (deploy user `~/.claude/settings.json`)
        for `~/.config/secgen/**`, `~/.git-credentials` and `projects/**/Vagrantfile`
        (Read tool + Bash commands naming them). Tested on a decoy file.
  - [ ] Later: switch to a scoped Proxmox API token (see OPEN_QUESTIONS #4).
  - [ ] Remove the GitHub PAT embedded in the `thomashaw` remote URL on the dev
        server (rotate it; use a credential helper or `gh auth`).

## Phase 1 — Proxmox testing suite (critical path)

**Transport: QEMU Guest Agent over the Proxmox REST API.** Works without any
guest network path, so it survives `net0` teardown and the post-provision reboot.

1. **Port guest-agent helpers into `proxmox_connection.rb`** (existing rest-client only):
   *2026-10-08: `post_json`, `exec_qemu_guest`, `qemu_agent_running?`,
   `qemu_agent_enabled?` done and tested (`scripts/agent-check <vmid>`).
   Findings: guest commands run as **root** (tests for "as the intended user"
   need `runuser -u <user> -- ...`); every API call from the dev server takes
   **~3.1s** (server-side wait after TLS, via the Squid proxy — direct access
   is blocked), so one guest command costs ≥ ~6s. Investigate (does the API
   respond faster from elsewhere?) before test suites get large; batch checks
   into one `sh -c` per module meanwhile.*
   - [x] `post_json` (form-encoding breaks the `agent/exec` command array).
   - [x] `exec_qemu_guest(vm_id, node, command, timeout: 10)` — POST
         `agent/exec`, poll `agent/exec-status?pid=` with a monotonic deadline and
         backoff (0.25s doubling, cap 1s). Success decided by **`exitcode`**, not
         presence of `err-data`; accept `exited`/`out-truncated` as `1` or `true`;
         warn on truncation; log stderr as a warning when exit code is 0.
   - [x] `qemu_agent_running?(vm_id, node)` — `agent/ping`; plus a config check
         (`agent: 1`) to distinguish "option off" from "agent not running".
   - [ ] Optional: API token auth (`PVEAPIToken=...`) instead of user/password.
   *2026-10-08: transport wired into `PostProvisionTest` and verified against a
   VM in the FINAL isolated state — net0 (provisioning bridge) removed, rebooted
   onto its static VLAN (10.x on vmbr1), unreachable from the deploy server by
   network. The guest agent reached it fine. This is the state tests must run in.*
2. **Lifecycle** — define and implement one known-good order:
   *Not done. secgen.rb still runs `post_provision_tests` BEFORE
   `proxmox_post_build` (net0 teardown) using a `vagrant halt/up` reboot. Needs
   reordering so tests run after teardown + reboot + start. Meanwhile,
   `secgen-run` already leaves the VM in final state, so a module's test can be
   run externally with `secgen-test-run <secgen_test/x.rb>`.*
   provision → `net0` teardown → single full reboot (static IPs + reboot-dependent
   modules settle) → wait for guest agent → run tests → snapshot / destroy.
3. **Refactor `PostProvisionTest`**:
   - [ ] Backend detection (Vagrant vs Proxmox) and a way to get node / VMID /
         credentials (e.g. a JSON test-context file written into the project dir
         at build time from `.vagrant/machines/*/proxmox/id`). Vagrant path keeps
         working.
   - [ ] `test_local_command` → `exec_qemu_guest` on Proxmox (also fixes the
         `-c '#{args}'` quoting bug).
   - [ ] Resolve the DHCP TODO via `agent/network-get-interfaces` (first
         non-loopback IPv4) instead of silently passing.
   - [ ] Structured results (JSON) with PASS / FAIL / SKIP, so agents can parse them.
   - [ ] Test tiers: **provisioned** → **service/tool works** → **exploitable**.
4. **Network-side and exploit tests** need something on the scenario VLAN:
   run them *from inside a VM* via the guest agent (e.g. a Kali/attacker VM in the
   test scenario, or from the target itself against `localhost` where meaningful).
5. **CLI entry points**: `secgen.rb test-module <path>` and
   `test-scenario <xml>` — build, test, write report, destroy; meaningful exit codes.
   *2026-10-08: first fresh build attempt (`test_scenario_proftpd`, Debian 9
   server) FAILED to provision — guest got no DHCP on net0, vagrant timed out
   before Puppet ran, so proftpd was never installed (OPEN_QUESTIONS #14). The
   test transport + password handling + context file all verified correct on the
   real build; the blocker is base/provisioning-network, not the harness. Fixed
   two pre-existing proxmox-client bugs found en route: `qemu_agent_get_ip` nil
   crash and `delete()` TLS verify. VM cleaned up. `agent-check`, `secgen-test-run`
   helpers added.*
6. [x] Spike: **proftpd end-to-end PROVEN on a fresh Debian 12 build (2026-10-08)**.
       Full loop works: build → provision → net0 teardown → `secgen_test` over the
       guest agent. Broken proftpd → `FAILED: Port 21 is closed` (exit 1); after
       fixing the config → `PASSED` (exit 0). The pipeline both ran and correctly
       reported pass/fail, and surfaced a real module regression (see finding
       below). `secgen-test-run <test.rb>` runs a module's test with creds from
       the config.
   - [x] **FIXED — proftpd module config stale on Debian 12 (Bookworm).**
         `templates/proftpd.erb` used `IdentLookups` (removed in modern ProFTPD →
         `fatal: unknown configuration directive`) and deprecated
         `MultilineRFC2228`. Both dropped (commit `726fb87b6`). Verified on a
         clean Debian 12 build: proftpd starts, `secgen_test` → `PASSED: Port 21
         is open` (exit 0). This was the pipeline's first real detect-and-fix.
   - [ ] `dirtycow` (local command test) via the guest agent.
   - [ ] Then run all 60 existing tests → baseline report; failures become issues.

## Phase 2 — Coverage baseline

- [ ] **Exploit-capability audit.** For every vulnerability module, classify how it
      can be verified with a *real* exploit:
      Metasploit module exists / web-app exploit (HTTP request sequence we'd write) /
      local privesc script / credential-based / needs bespoke tooling / not
      automatable. Output: what the current harness can already do and what we
      must add (e.g. Metasploit RPC/`msfconsole -r` from the attacker VM, a small
      HTTP exploit helper library in `PostProvisionTest`).
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
- [ ] Write the missing tests (agents, using Phase 3 skills — first real use of the pipeline).

## Phase 3 — Claude skills

Derived from analysis of existing modules + `README-Modules-*.md`. Each skill is
validated by having a fresh agent rebuild a known module and diffing the result.

- [ ] Write a SecGen module (Puppet + Ruby) — extend `secgen-puppet` rather than duplicate.
- [ ] Build a basic scenario.
- [ ] Provision a VM (Proxmox dev environment).
- [ ] Write `secgen_test`s (all three tiers).
- [ ] Test a module / scenario (run, read results, triage).
- [ ] Add `requires` / `conflict` when incompatibilities are found.
- Reuse `review-secgen-module` / `review-secgen-scenario` as a pre-human-review gate.

## Phase 4 — Issue tracker (GitHub Issues)

Agreed with Cliffe: use GitHub Issues as the central tracker.

- [ ] Labels: `agent:ready`, `agent:claimed`, `needs-human`, `type:bug`,
      `type:new-module`, `type:gap`, `type:test-failure`, plus module-area labels.
- [ ] Issue templates; `needs-human` issues must include a summarised action plan.
- [ ] Claim protocol (assign + label, with a stale-claim timeout) so agents never
      double-pick.
- [ ] `next-issue` picker script (`gh` CLI) ordered by priority.

## Phase 5 — Gap analysis

- [ ] Coverage matrix against: CWE/vulnerability classes, CyBOK knowledge areas
      (reuse existing tags), services/protocols, platforms, network topologies.
- [ ] Auto-create ranked `type:gap` / `type:new-module` issues.

## Phase 6 — Orchestration

Per issue: pick → worktree/branch → write module + test → schema validation
(`lib/CyBOK/validate_xml_*`) → Proxmox build + tests → review skills → draft PR,
or `needs-human` with summary. Run headless on the dev server (`claude -p` /
scheduled loop), paced to Max-plan limits, with a concurrency cap on Proxmox.

- [ ] A reserved VMID range / pool for agent builds, and a sweeper that destroys
      orphaned test VMs (moved from Phase 0 — only needed once agents build
      unattended).

## Phase 7 — Content production and feedback

- [ ] Generate new modules/scenarios from the gap backlog.
- [ ] Human review (Tom / Cliffe); review findings → issues; recurring issues →
      skill updates.
- [ ] Metrics: first-attempt pass rate, human interventions per module, review
      defects per module.

## Phase 8 — Windows (after Linux pipeline is fully working)

- [ ] Windows bases with the virtio-win QEMU Guest Agent service.
- [ ] `exec_qemu_guest(..., windows: true)` paths in the test harness.
- [ ] Extend tests/skills to the 3 Windows vulnerabilities and 20 utilities, then new Windows content.
