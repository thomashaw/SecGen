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

- **Guest commands run as root.** For "as the intended user" checks use
  `run_as_user(user, cmd)` or pass `user:` to `test_local_command` /
  `test_command_succeeds` (wraps `runuser -u <user> -- bash -lc`, falling back
  to `su`).
- **The dev server can't reach the VMs' VLAN IPs directly.** So on Proxmox
  `test_service_up`, `test_banner`, `test_http` and `test_html_returned_content`
  all run **inside the guest** via the agent (port check with `ss`, HTTP with
  curl → wget → bash `/dev/tcp`, against 127.0.0.1 then the guest's own IP), not
  from the host. Remote/exploit-from-attacker tests must run **from a VM on the
  scenario VLAN** (e.g. a Kali box in the scenario), not from the host.
- **Each API call ~3s** from this dev server; batch guest checks into one
  command where you can rather than many round-trips. The system IP is only
  looked up (one `agent/network-get-interfaces` call) if something needs it.
- On Proxmox, `secgen.rb` runs the tests **after** net0 teardown and the
  reboot (see the loop below). Other providers keep the old in-build runner
  (vagrant halt/up, before any post-build).

## Writing a secgen_test

Subclass `PostProvisionTest`, set `module_name`/`module_path`, call `super`, then
assert in `test_module`. Every check is recorded with a **tier**:

1. **Provisioned** — the package/files/accounts exist (`test_local_command`,
   default tier 1).
2. **Service/tool works** — the service listens / answers / the tool runs from a
   terminal (`test_service_up`, `test_banner`, `test_http`,
   `test_command_succeeds`; default tier 2). For utilities actually invoke the
   binary, as the intended user, so PATH and missing-lib problems surface.
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
    tier(1) { test_local_command('installed?', 'which foo', '/usr/bin/foo') }
    tier(2) do
      test_service_up                                 # port from json_inputs
      test_banner('220 ', send: nil)                  # first bytes from the port
      test_http('/login.php', match: 'Sign in')       # status 2xx/3xx + body match
      test_command_succeeds('foo runs', 'foo --version', user: 'alice')
    end
    # tier(3) { ... exploit ... }  (1C)
  end
end
FooTest.new.run
```

API (all in `lib/objects/post_provision_test.rb`):

| Helper | Passes when |
|---|---|
| `test_service_up(port: self.port)` | port is listening (SKIP if there's no port input) |
| `test_local_command(label, cmd, substr, user: nil)` | `substr` in stdout **or** stderr |
| `test_command_succeeds(label, cmd, user: nil)` | exit status 0 |
| `test_banner(match, port:, send: nil)` | what the port sends (after `send` + CRLF) contains `match` |
| `test_http(path, match: nil, status: nil, port:, scheme: 'http')` | status matches (default 2xx/3xx) and body contains `match` |
| `test_html_returned_content(page, match, hide = false)` | legacy: body contains `match` |
| `pass_check` / `fail_check` / `skip_check(name, detail = nil, tier:, evidence:)` | record your own check |
| `skip!(reason)` | stop now, report SKIP (prerequisite missing) |
| `run_command(cmd)` / `run_as_user(user, cmd)` / `http_get(path)` | raw `{stdout, stderr, exit_status}` / `{status, body, error}` |
| `add_evidence(name, cmd)` | extra command whose output is saved if the test FAILs |

Every helper takes `tier:`; a `tier(n) { ... }` block sets it for the checks
inside. Legacy tests that push `"PASSED: ..."` / `"FAILED: ..."` onto `outputs`
and set `all_tests_passed` still work — those lines are recorded as checks.

### Results and exit codes

`run` prints `PASSED:` / `FAILED:` / `SKIPPED:` lines, writes
`projects/<id>/test_results/<system>/<module>.json`
(`{module, module_path, system, backend, started_at, duration_s, status,
tier_reached, results: [{tier, name, status, detail, evidence?}]}`), and exits:

- `0` **PASS** — every check passed.
- `1` **FAIL** — a check failed. Evidence (failed units, `systemctl status
  *<module>*`, the boot journal, listening ports, processes, plus any
  `add_evidence`) is collected in one guest call into
  `test_results/<system>/evidence/<module>/`.
- `2` **SKIP** — could not test: no `SECGEN_PROXMOX_PASS`, guest agent not
  answering (waits `SECGEN_AGENT_WAIT`, default 60s), no IP when one was needed
  (DHCP on Vagrant), or the test itself raised. **A SKIP is never a pass** —
  usually a harness problem, not the module.

`tier_reached` is the highest tier whose checks (and every lower tier's) all
passed. The format is the results contract in `agentic_pipeline/ROADMAP.md`.
## The build + test loop

### One shot (preferred): `test-scenario` / `test-module`

`scripts/secgen-run --test -s scenarios/tests/<scn>.xml` (or
`scripts/secgen-run -m modules/<type>/.../<mod>` for a generated one-system
scenario holding just that module on Debian 12; `-- --test-base "<distro>"`
to change it) runs `secgen.rb test-scenario` / `test-module`:

build (with `--retries 1`) → `net0` teardown → snapshot if `--snapshot` →
start (the one full reboot: static IPs up, reboot-dependent modules settle) →
wait for the guest agent (`SECGEN_AGENT_WAIT_BOOT`, default 600s) → settle
(`SECGEN_TEST_SETTLE`, default 30s) → every module's `secgen_test` (each with a
`SECGEN_TEST_TIMEOUT`, default 900s) → `test_results/<project-id>/` →
destroy the VMs via the API and remove the project.

- **Exit code:** 0 all PASS, 1 any FAIL (or the build failed), 2 any SKIP.
- **Results** (gitignored, survive teardown): `test_results/<project-id>/`
  holds `summary.json` (build status/attempts, counts, per-system modules with
  status and `tier_reached`, SecGen commit/branch, `masked_secrets`),
  `scenario.xml` (the resolved scenario: what was actually tested),
  `build.log`, and `<system>/<mod>.json`, `<mod>.log`, `evidence/<mod>/`.
  Every copied file has the Proxmox password masked; `masked_secrets` > 0
  means something leaked it and should be fixed at source.
- `-- --keep-vms` keeps the VMs and project for debugging (even on PASS);
  `-- --no-destroy-on-failure` keeps them only if the build fails or the
  tests don't PASS. Destroy them afterwards with `scripts/secgen-destroy projects/<id>`.
- A plain `secgen.rb run` on Proxmox without `--no-tests` uses the same
  order and writes the same report. On PASS it shuts the VMs down unless
  `--proxmox-post-boot`; on a **FAIL or SKIP it destroys them** (the project
  dir and results stay) unless `--no-destroy-on-failure`, and never retries,
  because batches treat surviving VMs as good builds (they get pulled into
  Hacktivity) and a SKIP is unverified. It exits 1/2 on FAIL/SKIP. The other
  providers' in-build runner does the same now (so on VirtualBox a system
  whose network test SKIPs, e.g. DHCP, is destroyed).

### Step by step (to iterate on one VM)

1. Make a minimal test scenario in `scenarios/tests/` (one `<system>`, the module,
   a `parameterised_accounts` user if required, a `<network>`, and the **required**
   `build type="cleanup"` root-password reset). Pick a base whose Proxmox template
   exists (`scripts/pve-check`). Debian 12 provisions reliably; Debian 9 server has a flaky
   provisioning net. See the `review-secgen-scenario` skill for scenario rules.
2. Validate + resolve cheaply first (see CLAUDE.md "Check cheaply"): XSD validate,
   then `build-project` (no VMs).
3. Build: `scripts/secgen-run -p <name> -s scenarios/tests/<scn>.xml`
   (`--dry-run` first). It leaves the VM running in final state (net0 gone),
   with tests off. It passes `--retries 1` (`SECGEN_RETRIES`) to absorb the
   intermittent provisioning-net flake (no DHCP lease → vagrant times out
   "waiting for SSH to configure network interfaces" with no Puppet run); when
   vagrant can't say which VM failed, SecGen now destroys all and retries.
4. Run the test: `scripts/secgen-test-run <project>/puppet/<system>/modules/<mod>/secgen_test/<mod>.rb`
   — expect `PASSED: ...` / exit 0 (1 FAIL, 2 SKIP). `scripts/secgen-test-run` injects
   `SECGEN_PROXMOX_PASS` from the config without printing it. The JSON result
   and any FAIL evidence land in `<project>/test_results/<system>/`.
5. **If it fails**, start from the collected evidence
   (`test_results/<system>/evidence/<mod>/`), then diagnose further via the guest agent (`scripts/agent-check <vmid>`, or a small
   script calling `exec_qemu_guest` for `systemctl status`, `journalctl`,
   config-syntax checks). A failing test on a genuinely broken module is the
   pipeline working — fix the **module template/manifest**, not the generated
   project, then rebuild.
6. **Clean up**: `scripts/secgen-destroy projects/<id>` stops and deletes the
   project's VMs via the API, checks they're gone, and removes the project dir.
   Remove any scratch files/logs too. Verify no orphans with
   `scripts/pve-check --all | grep <vmid>`.

## Worked precedent

`scenarios/tests/test_scenario_proftpd.xml` caught a real regression: proftpd
wouldn't start on Debian 12 (`fatal: unknown configuration directive 'IdentLookups'`
— removed in modern ProFTPD). The test reported `FAILED: Port 21 is closed`; after
dropping the stale directive from the module template it reported
`PASSED: Port 21 is open`. That is the detect → fix → re-verify loop this skill is for.

## Then

Recommend `review-secgen-module` on the module and `review-secgen-scenario` on the
test scenario — static review and live testing are complementary.
