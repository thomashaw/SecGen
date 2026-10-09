# Hand-off: Agentic SecGen testing pipeline (continuing)

Context auto-loads from `CLAUDE.md`, `CLAUDE.local.md`, and memory. Read
`agentic_pipeline/ROADMAP.md` and `agentic_pipeline/OPEN_QUESTIONS.md` first for
the authoritative state — this file is a quick orientation.

## Where to work

- Worktree `/home/deploy/SecGen/.claude/worktrees/agentic-pipeline`, branch
  `worktree-agentic-pipeline`, pushed to remote `thomashaw` (SSH via the
  `secgen-fork` alias, through the Squid proxy).
- Commit footer: `Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>`.
  Never push master. Ask before history-rewriting git.
- Run SecGen via `scripts/` — the helpers use the checkout they live in and
  pick up the main checkout's `vendor/bundle` from a worktree automatically.

## Done so far

- **Phase 0 complete:** CLAUDE.md; creds in `~/.config/secgen/secgen.conf` via
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

## Next steps (roadmap Phase 1 tail)

1. Add build-retry to `secgen-run` (or always pass `--retries`) to absorb the
   provisioning flake.
2. Add the `dirtycow` local-command test (exercises the other test type).
3. Run all 60 existing module tests → coverage baseline; failures become issues.
4. Wire test-ordering into `secgen.rb` itself (run after net0 teardown + reboot)
   so `secgen.rb test-module` / `test-scenario` work without external
   `secgen-test-run`.

## Orient on start

```
cd ~/SecGen/.claude/worktrees/agentic-pipeline && git log --oneline -12 \
  && cat agentic_pipeline/ROADMAP.md agentic_pipeline/OPEN_QUESTIONS.md
```
