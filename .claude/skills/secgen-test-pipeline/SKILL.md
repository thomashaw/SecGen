---
name: secgen-test-pipeline
description: Test a SecGen module or scenario end-to-end on Proxmox using the agentic testing pipeline - build a VM, then run its secgen_test over the QEMU Guest Agent (no guest network needed, works after net0 teardown + reboot). Covers writing secgen_tests, the PostProvisionTest guest-agent transport, the build+test loop with secgen-run/secgen-test-run, diagnosing failures via the agent, and cleaning up VMs. Use when asked to test/verify a module or scenario, write a secgen_test, run the pipeline, or debug why a built VM's service/exploit isn't working. Trigger words - "test this module", "run the pipeline", "secgen_test", "does it provision", "verify the service starts", "post-provision test".
---

# Test a SecGen module/scenario (Proxmox guest-agent pipeline)

This is the automated-verification half of the agentic pipeline (see
`agentic_pipeline/ROADMAP.md`). It answers: *does this module actually provision,
and is the intended service/exploit really there on a freshly built VM?* — without
which agent-generated content can't be trusted.

The helper scripts are in `scripts/` (usage in `scripts/README.md`); run them as
`scripts/<name>` from the checkout/worktree you're working in. Host specifics
(Proxmox hosts, creds location, VLAN range) live in `CLAUDE.local.md`. This skill
is the mechanism and the workflow.

## How testing works

- Each module may ship `secgen_test/<name>.rb`, a subclass of `PostProvisionTest`
  (`lib/objects/post_provision_test.rb`), copied into every project at build time.
- On a **Proxmox** build the test reaches the VM through the **QEMU Guest Agent
  over the Proxmox REST API** — not `vagrant ssh`. This needs no network path into
  the guest, so it works after the provisioning NIC (net0) is torn down and the VM
  has rebooted onto its isolated static VLAN (the state that broke the legacy
  suite). The transport is in `lib/helpers/proxmox_connection.rb`
  (`exec_qemu_guest`, `qemu_agent_get_ip`, `qemu_agent_running?`).
- At build time `project_files_creator` writes `proxmox_test_context.json`
  (url + user, **no password**) and copies the client into the project `lib/`.
  The test reads the password from `ENV['SECGEN_PROXMOX_PASS']` at run time, and
  the node/VMID from `.vagrant/machines/<system>/proxmox/id`.
- `PostProvisionTest.proxmox?` guards all of this; the Vagrant path is unchanged.

### Caveats that change how you write/read tests

- **Guest commands run as root.** For "as the intended user" checks, wrap the
  command: `runuser -u <user> -- <cmd>`.
- **The dev server can't reach the VMs' VLAN IPs directly.** So `test_service_up`
  checks the port **inside the guest** on Proxmox (via the agent), not with a TCP
  connect from the host. Remote/exploit-from-attacker tests must run **from a VM
  on the scenario VLAN** (e.g. a Kali box in the scenario), not from the host.
- **Each API call ~3s** from this dev server; batch guest checks into one
  `sh -c '...'` where you can rather than many round-trips.
- Legacy `secgen.rb` still runs its in-build test runner at the *old* point
  (before net0 teardown, via a vagrant reboot). Until that's reordered, **test
  out-of-band** with `scripts/secgen-test-run` against a VM left running by `scripts/secgen-run`,
  rather than relying on `--no-tests` being off.

## Writing a secgen_test

Subclass `PostProvisionTest`, set `module_name`/`module_path`, call `super`, then
assert in `test_module`. Tiers to aim for:

1. **Provisioned** — the package/files/accounts exist (`test_local_command`).
2. **Service/tool works** — the service listens / the tool runs from a terminal
   (`test_service_up`; for utilities actually invoke the binary so PATH and
   missing-lib problems surface).
3. **Exploitable** — the intended vuln is actually exploitable (real exploit:
   Metasploit or a crafted request, run from an attacker VM — roadmap Phase 1, stream 1C).

```ruby
require_relative '../../../../../lib/post_provision_test'
class FooTest < PostProvisionTest
  def initialize
    self.module_name = 'foo'
    self.module_path = get_module_path(__FILE__)
    super
  end
  def test_module
    super
    test_service_up                                   # port from json_inputs
    test_local_command('installed?', 'which foo', '/usr/bin/foo')
  end
end
FooTest.new.run
```

`test_local_command(label, cmd, expected_substring)` passes if `expected_substring`
is in stdout **or** stderr. `run` prints outputs and exits non-zero on any failure.

## The build + test loop

1. Make a minimal test scenario in `scenarios/tests/` (one `<system>`, the module,
   a `parameterised_accounts` user if required, a `<network>`, and the **required**
   `build type="cleanup"` root-password reset). Pick a base whose Proxmox template
   exists (`scripts/pve-check`). Debian 12 provisions reliably; Debian 9 server has a flaky
   provisioning net. See the `review-secgen-scenario` skill for scenario rules.
2. Validate + resolve cheaply first (see CLAUDE.md "Check cheaply"): XSD validate,
   then `build-project` (no VMs).
3. Build: `scripts/secgen-run -p <name> -s scenarios/tests/<scn>.xml`
   (`--dry-run` first). It leaves the VM running in final state (net0 gone).
   **The provisioning net is an intermittent flake across bases** — if `vagrant up`
   times out "waiting for SSH to configure network interfaces" (no IP via agent)
   with no Puppet having run, just retry (or pass `--retries`).
4. Run the test: `scripts/secgen-test-run <project>/puppet/<system>/modules/<mod>/secgen_test/<mod>.rb`
   — expect `PASSED: ...` / exit 0. `scripts/secgen-test-run` injects
   `SECGEN_PROXMOX_PASS` from the config without printing it.
5. **If it fails**, diagnose via the guest agent (`scripts/agent-check <vmid>`, or a small
   script calling `exec_qemu_guest` for `systemctl status`, `journalctl`,
   config-syntax checks). A failing test on a genuinely broken module is the
   pipeline working — fix the **module template/manifest**, not the generated
   project, then rebuild.
6. **Clean up**: destroy every VM you built (stop + delete via the Proxmox API),
   remove the `projects/*` dir and any scratch files/logs. Verify no orphans by
   listing `/cluster/resources?type=vm` and grepping your prefix.

## Worked precedent

`scenarios/tests/test_scenario_proftpd.xml` caught a real regression: proftpd
wouldn't start on Debian 12 (`fatal: unknown configuration directive 'IdentLookups'`
— removed in modern ProFTPD). The test reported `FAILED: Port 21 is closed`; after
dropping the stale directive from the module template it reported
`PASSED: Port 21 is open`. That is the detect → fix → re-verify loop this skill is for.

## Then

Recommend `review-secgen-module` on the module and `review-secgen-scenario` on the
test scenario — static review and live testing are complementary.
