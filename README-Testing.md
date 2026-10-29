# Testing modules and scenarios

This document describes how SecGen verifies that a module provisions correctly and that its intended service, tool or vulnerability is present on a freshly built VM, and it defines the results contract (the layout of the files a test run produces) that scripts and other tools can rely upon. Writing a test is covered in further detail by the `write-module-secgen-tests` skill (`.claude/skills/write-module-secgen-tests/SKILL.md`), and the build, test and fix loop by the `secgen-test-pipeline` skill (`.claude/skills/secgen-test-pipeline/SKILL.md`).

## Overview

A module may ship a post-provision test at `secgen_test/<name>.rb`, which is a subclass of `PostProvisionTest` (`lib/objects/post_provision_test.rb`) and is copied into every generated project. On a Proxmox build, tests reach each VM through the QEMU Guest Agent over the Proxmox REST API rather than over SSH; this requires no network path into the guest, so tests continue to work after the provisioning NIC has been removed and the VM has rebooted onto its isolated VLAN. Tier 3 (exploit) checks are run from an attacker VM inside the same scenario, identified by a base of `<type>attack</type>`, so that the exploit travels over the scenario's own network.

Two commands build, test and then destroy:

```
ruby secgen.rb test-scenario scenarios/tests/test_scenario_proftpd.xml
ruby secgen.rb test-module modules/services/unix/ftp/proftpd
```

`test-module` generates a one-system scenario holding just that module (`lib/test/module_test_scenario.rb`) on a default base, `modules/bases/debian_bookworm_desktop_kde`; `--test-base <name>` selects another base from `modules/bases/`. The scenario is regenerated from the module's metadata on every run, replacing any earlier copy, and SecGen resolves it before creating any VM, so a module that declares a `<conflict>` with the chosen base stops at that point. Both commands write their results to `test_results/<project-id>/`, destroy the VMs and project unless `--keep-vms` is given, and exit with the worst status across all modules. VMs that were kept can be deleted later with `ruby secgen.rb destroy-project -p projects/<id>`; on Proxmox this hard-stops and deletes them through the API and so needs the Proxmox options (for example `--read-options <conf>`). The helper scripts in `scripts/` (see `scripts/README.md`) wrap these commands for Proxmox development and also run a single test against an existing VM.

## Tiers

Each check belongs to a tier, which records how much of the module has been demonstrated to work:

| Tier | Meaning | Example |
|---|---|---|
| 1 | Provisioned | the package is installed and the configuration file is in place |
| 2 | Service or tool works | the service is listening and answers; the tool runs from a terminal as the intended user |
| 3 | Exploitable | the intended exploit, run from the attacker VM, achieves code execution or access |

`tier_reached` is the highest tier whose checks, together with those of every lower tier, all passed (`0` if none did).

## Parameterised modules

A module's inputs (its `<read_fact>`s, with `<default_input>` values) can be set differently by every scenario, and a test must be correct for any valid deployment of the module, not only for the defaults. At test time `self.json_inputs` holds the values the module actually received, as a hash of arrays keyed by input name, and `self.port` is derived from the `port` input. Before writing any assertion, list the module's inputs and, for each one that changes something observable on the VM (a port, banner, account, path, hostname, DNS record or file content), read the value from `json_inputs` and check that the module applied it, guarding the check on the input being present. Inputs with nothing to observe from the VM, such as a start-at-boot switch or the IP address of another system, may be skipped, with a comment in the test saying so. A test that hardcodes a value which is also the input's default passes on the default build and tests the wrong thing on every other. The `write-module-secgen-tests` skill (`.claude/skills/write-module-secgen-tests/SKILL.md`) gives the pattern and examples.

## Statuses and exit codes

Every check, every module and every run has one of three statuses, and the test process exits with the corresponding code:

| Status | Exit code | Meaning |
|---|---|---|
| `PASS` | `0` | every check passed |
| `FAIL` | `1` | a check failed; evidence is collected automatically |
| `SKIP` | `2` | the test could not be carried out (for example, no Proxmox password in the environment, an unresponsive guest agent, or an exception raised by the test itself) |

A module is `FAIL` if any of its checks failed, otherwise `SKIP` if any check was skipped, otherwise `PASS`; a run takes the worst module status (`FAIL` over `SKIP` over `PASS`). A `SKIP` is never treated as a pass, since it usually indicates a harness or network problem rather than a working module. Whilst the JSON files described below are the authoritative record, tests also print human-readable `PASSED:`, `FAILED:` and `SKIPPED:` lines for compatibility with older tests and scripts.

A failure that Puppet itself detects during provisioning (for example, a service that fails to start) is a build failure rather than a test failure: no tests are run, and the failure is recorded under `build` in `summary.json`.

## Results contract

Results are kept per project run rather than per scenario, since scenarios are randomised and the resolved `scenario.xml` is therefore the record of what was actually tested. They are written to the top-level `test_results/` directory, which is gitignored and survives the teardown of the VMs and project:

```
test_results/
  <project-id>/                    e.g. tom-proftpd-03_SecGen20261009_140211
    summary.json                   project-level report
    scenario.xml                   copy of the resolved projects/<id>/scenario.xml
    build.log                      copy of the build log
    <system>/
      <module>.json                per-module result
      <module>.log                 the test's raw stdout and stderr
      evidence/<module>/*.txt      collected on FAIL
```

### Per-module JSON

Each test writes `projects/<id>/test_results/<system>/<module>.json`, which is then copied into `test_results/<project-id>/`:

```json
{
  "module": "vsftpd_234_backdoor",
  "module_path": "modules/vulnerabilities/unix/ftp/vsftpd_234_backdoor",
  "test": "Vsftpd234BackdoorTest",
  "system": "target",
  "backend": "proxmox-guest-agent",
  "started_at": "2026-10-10T18:42:00+01:00",
  "duration_s": 99.9,
  "status": "PASS",
  "tier_reached": 3,
  "results": [
    { "tier": 1, "name": "vsftpd 2.3.4 installed (test -x /usr/local/sbin/vsftpd)", "status": "PASS", "detail": "... exited 0 on target" },
    { "tier": 2, "name": "port 21 open", "status": "PASS", "detail": "Port 21 is open at 10.89.0.10 (target)!" },
    { "tier": 3, "name": "msf exploit (exploit/unix/ftp/vsftpd_234_backdoor)", "status": "PASS", "detail": "RCE confirmed: ... sentinel /tmp/secgen_pwned_... contains uid=0(root)." }
  ]
}
```

`test` is the test's class name, and `backend` records how the VM was reached (`proxmox-guest-agent` or `vagrant`). A check carries an `evidence` list (paths to saved files) only when it saved some.

### summary.json

```json
{
  "project": "deploy-vsftpd-01_SecGen20261010_183043",
  "source_scenario": "scenarios/tests/test_scenario_vsftpd.xml",
  "secgen_commit": "4ab3d0625d5ffd8c800b7dc999f00a2411d7b7eb",
  "secgen_branch": "worktree-phase1c-followups",
  "started_at": "2026-10-10T18:30:43+01:00",
  "finished_at": "2026-10-10T18:43:41+01:00",
  "build": { "status": "success", "attempts": 1, "log": "build.log" },
  "status": "PASS",
  "counts": { "PASS": 1, "FAIL": 0, "SKIP": 0 },
  "masked_secrets": 0,
  "systems": [
    {
      "system": "target",
      "base": "modules/bases/debian_bookworm_desktop_kde",
      "vmid": "pmox01/383100476",
      "modules": [
        { "module": "vsftpd_234_backdoor", "status": "PASS", "tier_reached": 3, "result": "target/vsftpd_234_backdoor.json" }
      ]
    }
  ]
}
```

A failed build is recorded under `build` rather than as module failures, and the run's `status` is then `FAIL`.

On Proxmox, a build whose VMs outlive the tests (anything except `test-scenario` and `test-module` without `--keep-vms`) is snapshotted as `original` after the provisioning NIC is removed and before the test boot. After a `PASS` every VM is rolled back to that snapshot, so the VMs that are kept carry no traces of the tests, and the result is recorded as `"revert": { "status": "success", "failed_vms": [] }`. If any VM could not be snapshotted or rolled back, `revert.status` is `failed`, the run's `status` becomes `FAIL`, and the VMs are destroyed as for any other failure. The snapshot is deleted after the rollback unless `--snapshot` was given. A run that does not pass is never rolled back, so its VMs keep the evidence.

### Evidence

When a module fails, a standard set of diagnostics (failed units, the module's service status, the boot journal, listening ports and processes) is collected in a single guest-agent call, together with anything the test registered with `add_evidence`, and saved under `evidence/<module>/`.

### Secrets

The files copied into `test_results/` exclude the `Vagrantfile`, `systems`, `datastores`, the flags and hints XML, spoiler passwords and `test_context.json`. Every file that is copied has the Proxmox password replaced with `********` by a literal (non-regular-expression) replacement in Ruby, and the number of replacements is recorded as `masked_secrets`, so that any leak can be fixed at its source.

### Versioning

The contract is currently at version 1, and the files do not yet carry a version number. Adding a field is not a breaking change; renaming or removing a field, or changing its meaning, is. When the first breaking change is made, a top-level `schema_version` field will be added to `summary.json` and to each per-module JSON file, starting at `2`, and readers should treat a missing `schema_version` as version 1.
