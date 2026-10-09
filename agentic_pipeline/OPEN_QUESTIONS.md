# Open Questions

Decisions to discuss (mostly with Cliffe). Move items to "Decided" with a date.

## Open

| # | Question | Notes |
|---|---|---|
| 1 | Which GitHub repo hosts the issues — `cliffe/secgen` or `thomashaw/secgen`? Can agents open issues / PRs upstream directly? | GitHub Issues agreed in principle. |
| 2 | Label scheme and claim protocol for agents. | Draft in ROADMAP Phase 4. |
| 3 | How does `PostProvisionTest` get node / VMID / API credentials? | Proposal: test-context JSON written at build time. Avoid putting secrets in the project dir? |
| 4 | API token (`PVEAPIToken`) vs username/password for the pipeline; what minimum privileges? | Depends on PVE version. |
| 7 | Keep the old "any stderr = failure" behaviour for any existing `exec_qemu_guest` callers (Hacktivity)? | Proposed: decide on `exitcode`. |
| 8 | How are exploit tests run — attacker VM inside each test scenario, or one shared test-runner VM per VLAN? | |
| 9 | Is Metasploit acceptable as a test-time dependency (inside the attacker VM, not the SecGen host)? | Keeps "no new external deps" on the host. |
| 10 | Proxmox capacity / concurrency limits for agent builds. | Later — once pipeline works. |
| 11 | Review cadence and capacity (Tom + Cliffe). | Later. |
| 12 | Target number / priority areas of new modules. | Later — gap analysis informs this. |

| 14 | **Intermittent** provisioning-network failure on pmox01: guest sometimes gets **no DHCP lease on net0**, so `vagrant up` times out "waiting for SSH to configure network interfaces" (no IP via agent) → no Puppet. Seen on both Debian 9 **and** Debian 12 (a D12 build failed this way, an identical retry succeeded), so it's a **flaky provisioning net issue, not base-specific**. Mitigation: SecGen's `--retries` flag (and/or `secgen-run` auto-retry) should absorb it; root cause (DHCP on the provisioning bridge) still worth understanding with Tom/Cliffe. Not a blocker now — retries work around it. |

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
| 2026-10-08 | Dev builds use VLANs 200–1000 on vmbr0; `~/.local/bin/secgen-run` (deploy user) auto-increments prefix + VLAN and wraps within that range. |
