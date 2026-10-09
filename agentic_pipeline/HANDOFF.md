# Hand-off: Agentic SecGen testing pipeline (continuing)

Context auto-loads from `CLAUDE.md`, `CLAUDE.local.md`, and memory. Read
`agentic_pipeline/ROADMAP.md` and `agentic_pipeline/OPEN_QUESTIONS.md` first for
the authoritative state — this file is a quick orientation.

## Where to work

- One worktree + branch per task, off `master` (e.g. one per Phase 1 stream),
  pushed to remote `thomashaw`. Several agents work in parallel — never bare
  `git stash`.
- Never push master. Ask before history-rewriting git.
- Run SecGen via `scripts/` - the helpers use the checkout they live in and
  pick up the main checkout's `vendor/bundle` from a worktree automatically.

## Done so far

- **Phase 0 complete:** CLAUDE.md; PAT removed from the remote URL; creds in `~/.config/secgen/secgen.conf` via
  `--read-options`; permission deny rules; Proxmox password no longer written
  into generated `projects/*/Vagrantfile` or `systems` (Vagrantfile reads
  `SECGEN_PROXMOX_PASS`, set by secgen.rb). PVE 9.2.3; guest agent enabled on
  the templates the bases use.
- **Phase 1 transport proven end-to-end:** `PostProvisionTest` runs tests over
  the Proxmox QEMU Guest Agent — no guest network path needed, so it works after
  net0 teardown + reboot (the exact state that broke the legacy suite). Added
  `exec_qemu_guest` / `qemu_agent_running?` / `qemu_agent_enabled?` / `post_json`
  to `lib/helpers/proxmox_connection.rb`; fixed two pre-existing client bugs
  (`qemu_agent_get_ip` nil crash; `delete()` missing TLS-verify-skip).
- **First real detect-and-fix:** proftpd failed to start on Debian 12
  (`IdentLookups` removed in modern ProFTPD). Fixed the module template
  (`c6897c96c`, dropped `IdentLookups` + deprecated `MultilineRFC2228`);
  `secgen_test` now PASSES from a clean Debian 12 build. Test scenario:
  `scenarios/tests/test_scenario_proftpd.xml`.

## Helper scripts (`scripts/`, see `scripts/README.md`)

- `scripts/secgen-run -s scenario.xml [-p name] [--dry-run] [-- extra args]` —
  build; auto prefix `<owner>-<name>-NN` + next free VLAN (200–1000); logs to
  `log/`. Real runs build VMs on shared hardware — dry-run first / ask.
- `scripts/secgen-test-run <path/to/secgen_test/x.rb>` — run one module's test,
  creds injected from config (password never printed).
- `scripts/pve-check [--all]` — read-only PVE version + guest-agent option per template.
- `scripts/agent-check <vmid> [node]` — read-only guest-agent smoke test.

## Key gotchas

- Guest commands run as **root** — wrap with `runuser -u <user> -- ...` for
  user-context tests.
- Every Proxmox API call from this server takes ~3s (so one guest command ~6s).
- The net0/DHCP provisioning failure is an **intermittent flake across all
  bases** (OPEN_QUESTIONS #14), not base-specific — mitigate with SecGen's
  `--retries`.
- Always destroy test VMs via the API (stop + delete) and remove `projects/*`
  dirs and scratch files when done. Verify no orphans:
  list `/cluster/resources?type=vm` and grep your prefix.

## Next steps (ROADMAP Phase 1 streams, in parallel)

The results contract (`test_results/<project-id>/`, PASS/FAIL/SKIP, tiers 1–3)
is agreed — see ROADMAP Phase 1. Streams:

- **1A - Lifecycle + CLI** (`secgen.rb`): tests after net0 teardown + reboot +
  agent ready; `test-module` / `test-scenario`; absorb the provisioning flake
  with `--retries`.
- **1B - `PostProvisionTest` refactor**: JSON PASS/FAIL/SKIP, no silent
  `exit(0)` passes, IP via `network-get-interfaces`, test tiers.
- **1C - Exploit tests from an attacker VM**: decide OPEN_QUESTIONS #8/#9,
  attacker VM in test scenarios, one module spike.

Then Phase 2 (repo split), then Phase 3 (coverage baseline: run all 60 tests).
`dirtycow` is in the Backlog.

## Orient on start

```
cd <your worktree> && git log --oneline -12 \
  && cat agentic_pipeline/ROADMAP.md agentic_pipeline/OPEN_QUESTIONS.md
```
