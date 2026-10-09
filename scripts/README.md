# SecGen helper scripts (Proxmox dev/test)

Small wrappers for building and testing on a Proxmox cluster. Run them by path
from the checkout or worktree you're working in (`scripts/secgen-run ...`): each
script uses the checkout it lives in, so a worktree builds its own code. If that
checkout has no `vendor/bundle` but the main checkout does, the scripts point
bundler at the main checkout's gems.

| Script | What it does |
|---|---|
| `scripts/secgen-run -s <scenario.xml> [-p name] [--dry-run] [-- extra secgen args]` | Proxmox build with prefix `<owner>-<name>-NN` (per-name counter) and the next VLAN in the range (shared counter). Leaves the VM running in its final state (`--shutdown --no-tests --proxmox-post-boot --no-destroy-on-failure`). Logs to `log/[ERROR_]<project_id>`. `--dry-run` shows the command and numbers without using them. |
| `scripts/secgen-test-run <projects/.../secgen_test/<mod>.rb>` | Runs one module's post-provision test against its built VM over the QEMU Guest Agent. Expect `PASSED: ...` / exit 0. |
| `scripts/pve-check [--all]` | Read-only: PVE version and the QEMU Guest Agent option on every template (or all VMs). |
| `scripts/agent-check <vmid> [node]` | Read-only guest-agent smoke test (`id`, `hostname`, IPs) via `lib/helpers/proxmox_connection.rb`. |

## Setup

- **Credentials:** a SecGen `--read-options` file, default
  `~/.config/secgen/secgen.conf` (mode 600), with at least `--proxmox-url`,
  `--proxmoxuser`, `--proxmoxpass` and `--proxmox-node`. The scripts never print
  the password or put it on a command line; `secgen-test-run` passes it to the
  test as `SECGEN_PROXMOX_PASS`.
- **Personal settings** (optional) go in `~/.config/secgen/secgen-run.env`,
  which is sourced by every script:

  ```sh
  SECGEN_RUN_OWNER=tom        # prefix owner (default: $USER)
  SECGEN_VLAN_MIN=200         # VLAN range secgen-run cycles through
  SECGEN_VLAN_MAX=1000
  # SECGEN_CONF=...           # alternative creds file
  # SECGEN_STATE=...          # counter file (default ~/.config/secgen/run_counter)
  ```

- The counter file holds `vlan <last>` plus one `<name> <last_index>` line per
  name; edit it by hand to jump numbers. It is locked, so parallel runs don't clash.

Real `secgen-run` builds create VMs on shared hardware: dry-run first, and
destroy the VMs you built when you're done (see the `secgen-test-pipeline` skill).
