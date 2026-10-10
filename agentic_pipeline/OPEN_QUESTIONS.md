# Open Questions

Decisions to discuss (mostly with Cliffe). Move items to "Decided" with a date.

## Open

| # | Question | Notes |
|---|---|---|
| 1 | Which GitHub repo hosts the issues — `cliffe/secgen` or `thomashaw/secgen`? Can agents open issues / PRs upstream directly? | GitHub Issues agreed in principle. |
| 2 | Label scheme and claim protocol for agents. | Draft in ROADMAP Phase 5. |
| 4 | API token (`PVEAPIToken`) vs username/password for the pipeline; what minimum privileges? | Planned for the end of ROADMAP Phase 6, before unattended orchestration (2026-10-09). PVE 9.2.3. |
| 7 | Keep the old "any stderr = failure" behaviour for any existing `exec_qemu_guest` callers (Hacktivity)? | Proposed: decide on `exitcode`. |
| 10 | Proxmox capacity / concurrency limits for agent builds. | Later — once pipeline works. |
| 11 | Review cadence and capacity (Tom + Cliffe). | Later. |
| 12 | Target number / priority areas of new modules. | Later — gap analysis informs this. |
| 14 | **Intermittent** provisioning-network failure on pmox01: guest sometimes gets **no DHCP lease on net0**, so `vagrant up` times out "waiting for SSH to configure network interfaces" (no IP via agent) → no Puppet. Seen on both Debian 9 **and** Debian 12 (a D12 build failed this way, an identical retry succeeded), so it's a **flaky provisioning net issue, not base-specific**. Mitigation: SecGen's `--retries` flag (and/or `secgen-run` auto-retry) should absorb it; root cause (DHCP on the provisioning bridge) still worth understanding with Tom/Cliffe. Not a blocker now — retries work around it. |
| 15 | Repo split: what lives in public SecGen vs the private pipeline repo (esp. `scripts/` and the `secgen-test-pipeline` skill)? Repo name and owner (`cliffe` org vs `thomashaw`)? | Cliffe suggested a private repo (2026-10-09). Timing decided: ROADMAP Phase 2, after the test harness and before the tracker. Draft layout there. |

## Decided

| Date | Decision |
|---|---|
| 2026-10-07 | Tests reach VMs via the QEMU Guest Agent over the Proxmox REST API. No `qm`, no new gems. |
| 2026-10-07 | Lifecycle must keep working after provisioning + one reboot; use the API rather than SSH. |
| 2026-10-07 | Vulnerability tests should use real exploits where possible; audit what's feasible per module. |
| 2026-10-07 | Utility tests should actually run the tool from a terminal. |
| 2026-10-07 | Windows is a second phase after the Linux pipeline is fully working. |
| 2026-10-07 | Reviewers: Tom and Cliffe. |
| 2026-10-07 | GitHub Issues is the central tracker (agreed with Cliffe). |
| 2026-10-07 | Roadmap lives in the repo (`agentic_pipeline/`). Dev environment only for now. |
| 2026-10-07 | Claude Code runs on the dev server for pipeline work. |
| 2026-10-08 | PVE version is 9.2.3 (was Q5). |
| 2026-10-08 | Base templates have `qemu-guest-agent` installed and the Proxmox QEMU Guest Agent option enabled (was Q6). |
| 2026-10-08 | `PostProvisionTest` gets node/VMID from `.vagrant/machines/*/proxmox/id` and url/user from `test_context.json` (was proxmox_test_context.json) written at build time; the password comes only from `SECGEN_PROXMOX_PASS`, never the project dir (was Q3). |
| 2026-10-08 | Dev builds use VLANs 200–1000 on vmbr0; `scripts/secgen-run` auto-increments prefix + VLAN and wraps within that range. |
| 2026-10-09 | Work order: Phase 1 test harness (parallel streams 1A–1C) → Phase 2 repo split → Phase 3 coverage baseline. `dirtycow` moved to the backlog. |
| 2026-10-09 | Test results contract: per project run under gitignored `test_results/<project-id>/` (resolved `scenario.xml`, `build.log`, per-module JSON/logs/evidence, `summary.json`); PASS/FAIL/SKIP with exit 0/1/2; tiers 1–3; evidence auto-collected on FAIL; Proxmox password masked (not refused) in copied files. See ROADMAP Phase 1. |
| 2026-10-10 | (was Q8) Exploit tests run from an **attacker VM inside each test scenario**, not a shared per-VLAN runner. The attacker is discovered by base `<type>attack</type>` (not a hard-coded name); tier 3 SKIPs if there is none. See ROADMAP 1C. |
| 2026-10-10 | (was Q9) Metasploit is acceptable **inside the attacker VM** (it ships in the Kali base); the SecGen host stays dependency-free. Framework helper `test_msf_exploit` drives it over the guest agent. See ROADMAP 1C. |
