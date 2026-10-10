# LLM Assistant in Ghidra Labs: Roadmap

Goal: students reverse engineering in SecGen labs can ask the university's local
Spark LLMs about the code in front of them, through the GhidrAssist plugin in
Ghidra, without any student VM joining the internal network and without the API
key being stored on a student VM. The design follows route A of *Using the Spark
LLMs with Ghidra for teaching* (the PDF in the repository root): a relay VM, which
students do not control, is the only machine with a leg on the internal network.

The work so far is summarised in [Work done](#work-done-as-of-2026-10-10); the
remaining steps follow in order, and open questions are listed at the end.

## Order of work (as of 2026-10-10)

1. **Phase 0, the working route**: **done** on `feature/ghidra-llm-assistant`
   (see [Work done](#work-done-as-of-2026-10-10)).
2. **Phase 1, staff pilot**: a real key in `secgen.conf`, real answers from
   `fast` and `smart`, and a judgement on whether the answers are good enough to
   build into a lab.
3. **Phase 2, refactor**: replace the `keep_provisioning_nic` module type with
   an internal network `<network>` module in the scenario, and tidy the code.
   This comes before hardening, since the firewall rules in Phase 3 need to know
   which NIC is the internal one.
4. **Phase 3, relay hardening in the module**: required before students use it;
   the relay is currently a full bridge into 172.22.0.0/16 should it be
   compromised.
5. **Phase 4, Proxmox firewall**: required before external Hacktivity users;
   partly an infrastructure change outside SecGen.
6. **Phase 5, per-lab keys**: depends on a key database on the gateway.
7. **Phases 6 and 7, MCP agent route and lab content**: follow once the
   benchmarks and the pilot have reported.

## Work done (as of 2026-10-10)

All of the following is on the branch `feature/ghidra-llm-assistant` of the
`thomashaw` fork, in three commits:

| Commit | Summary |
|---|---|
| `d7368f09b` | GhidrAssist in the `ghidra` module, the new `llm_relay` module, and the test scenario |
| `6970bd1b9` | Move the relay's net0 onto the internal bridge after the build, rather than keeping it on the provisioning bridge |
| `88c809d61` | The API key via `--llm-api-key`, never written to the scenario or the project |

### What was built

- **`ghidra` module (Kali only)**: the optional `llm_assistant` input (off by
  default, so assessed labs remain unassisted) installs GhidrAssist 2.2.0 (the
  Ghidra 12.1 build, pinned by checksum) into the Ghidra installation's
  `Extensions` directory. Installing it there bypasses the GUI installer's exact
  version check, which would otherwise refuse 12.1 against Ghidra 12.1.4. A
  root-only script, `secgen-ghidra-llm-prefs`, presets one OpenAI-compatible
  provider per model (`fast`, `smart`) in each user's
  `~/.config/ghidra/ghidra_12.1.4_PUBLIC/preferences` and in `/etc/skel`, so that
  accounts created later inherit them. The plugin points at the relay when
  `llm_relay_ip` is set, and at `llm_api_url` (the gateway) otherwise; student
  VMs only ever hold the placeholder key `relay`.
  Files: `manifests/llm.pp`, `templates/secgen-ghidra-llm-prefs.sh.erb`,
  `secgen_test/ghidra.rb`, and new inputs in `secgen_metadata.xml`.
- **`llm_relay` module** (`modules/utilities/unix/llm/llm_relay`): an nginx
  reverse proxy that relays only `chat/completions` and `models` to the gateway
  (172.22.222.222:8080) and returns 403 for every other path, so the Open WebUI
  interface, sign-up and administration pages are not reachable through it.
  Responses are streamed (`proxy_buffering off`) with long timeouts for slow
  models, and `allowed_networks` optionally restricts which subnets may use it.
- **Internal network NIC** (`lib/helpers/proxmox.rb`, `secgen.rb`): systems
  carrying a module of type `keep_provisioning_nic` have net0 moved, with its MAC
  unchanged, onto `--proxmox-internal-bridge` (default `vmbr3`) after the build,
  rather than having it removed.
- **API key handling** (`secgen.rb`, `lib/templates/Vagrantfile.erb`):
  `--llm-api-key`, kept in a `--read-options` file such as `secgen.conf`, sets
  `SECGEN_LLM_API_KEY`, in the same way as `--proxmoxpass`. For modules of type
  `llm_api_key` the generated Vagrantfile passes
  `ENV['SECGEN_LLM_API_KEY']` as the Puppet fact `llm_api_key`, so the key itself
  is never written to the project; the relay prefers that fact to its `api_key`
  input. The masking of test results and `build.log` now covers the key as well
  as the Proxmox password.
- **Test scenario**: `scenarios/tests/test_scenario_ghidra_llm.xml` (a Debian 12
  relay and a Kali Ghidra VM pointed at it).

### What was found along the way

- GhidrAssist's "Open WebUI" provider type calls Open WebUI's Ollama passthrough
  (`ollama/api/chat`), which does not serve the vLLM-hosted Spark models; the
  OpenAI-compatible provider type is used instead, and its base URL must end in
  a slash, since `chat/completions` is appended directly.
- The bridges on pmox01 are: `vmbr2`, 192.168.201.0/24; `vmbr3`, 172.22.0.0/16
  with DHCP (the gateway's network); `vmbr4`, 172.33.0.0/16 (SecGen's
  provisioning net0, which has no route to 172.22). The pipeline roadmap had
  previously recorded net0 as `vmbr3`; this was corrected in
  `agentic_pipeline/ROADMAP.md`.
- The gateway's `/api/v1/chat/completions` endpoint exists, as the PDF states
  (it answers 401 without a key).

### How it was verified

| Build | Result |
|---|---|
| `deploy-ghidrallm-01` | Ghidra and plugin checks passed; relay returned 502 ("no route to host"), which exposed the bridge error |
| `deploy-ghidrallm-02` | 3/3 tests passed after the bridge fix; Kali reached the gateway through the relay (401 without a key) |
| `deploy-ghidrallm-03` | Kept running for manual testing (relay 122688943, Kali 886096595); built before the key change |
| `deploy-ghidrallm-04` | 3/3 passed with a fake canary key; the relay reported adding the key, and the canary appeared in no project file, log or test result |

## Phase 1: staff pilot

- [ ] Create the dedicated `ghidra-lab` account in Open WebUI and add its key to
      `secgen.conf` as `--llm-api-key` (the PDF recommends a shared lab account
      rather than a personal key for this stage).
- [ ] Build `scenarios/tests/test_scenario_ghidra_llm.xml` and confirm that a
      question asked in GhidrAssist returns a real answer from `fast`, and from
      `smart` on a harder function.
- [ ] Try it on binaries that are well understood (for example an old CTF
      challenge) and judge the quality of the explanations, as the PDF's pilot
      suggests.
- [ ] Note in the lab sheet that Ghidra shows a "New plugins found" prompt on the
      first launch of CodeBrowser, and that GhidrAssist must be ticked there.
- [ ] Agree with Harry how the relay is hooked into the Hackerbot scenarios (or
      whether a dedicated relay VM is preferred; see Phase 3).

## Phase 2: refactor

### Internal network as a `<network>` module

At present the relay asks SecGen for the internal network implicitly: its
metadata carries the `keep_provisioning_nic` type, and the Proxmox post-build
moves net0 onto the internal bridge for any system with a module of that type.
This hides a networking decision inside a utility module and ties it to the
provisioning NIC. The refactor makes the internal network explicit in the
scenario, in the same way as the lab network:

```xml
<system>
  <system_name>llm_relay</system_name>
  ...
  <utility module_path=".*/llm_relay"/>
  <network type="private_network"/>   <!-- the lab network, as now -->
  <network type="internal_network"/>  <!-- new: the 172.22 internal network -->
</system>
```

- [ ] Add a network module, `modules/networks/internal_network`, with inputs for
      the bridge (default `vmbr3`), DHCP or a static address (the static option is
      also needed for the Proxmox firewall in Phase 4), and no VLAN.
- [ ] Teach `lib/templates/Vagrantfile.erb` (and the network helpers) to attach
      that network on the given bridge, untagged. Two approaches are possible:
      attach it as an ordinary extra NIC at build time, so that net0 is torn
      down as normal for every system; or keep the post-build approach, but key
      it on the presence of the `internal_network` module rather than on a module
      type. The first is simpler and removes a special case from the teardown,
      and is preferred unless vagrant-proxmox cannot attach an untagged NIC.
- [ ] **Internal check in the relay module**: the relay determines whether its
      system has the `internal_network` module, and only then applies the
      internal-network configuration (for example the in-guest firewall rules
      on that NIC in Phase 3). SecGen would expose this to the module, for
      instance as an input or fact listing the system's network modules (or the
      internal NIC's address), so that the module can identify the internal
      interface. If the network is absent, the relay should fail clearly, since
      it cannot reach the gateway without it.
- [ ] Remove the `keep_provisioning_nic` type, the net0 move in
      `lib/helpers/proxmox.rb`, and `--proxmox-internal-bridge` (which becomes an
      input of the network module), and update the test scenario and the module
      description.
- [ ] Re-run the test build to confirm the relay still reaches the gateway.

### General tidy-up

- [ ] Define the list of masked secret environment variables once, rather than
      separately in `lib/helpers/test_results.rb` and
      `scripts/lib/attach_build_log.rb`.
- [ ] Review the `ghidra` module's LLM code (`manifests/llm.pp` and the
      preferences script) and the `llm_relay` manifests with the
      `review-secgen-module` skill, and the test scenario with
      `review-secgen-scenario`.
- [ ] Consider whether the `llm_api_key` module type should also become explicit
      (for example a module input that names the key source), for consistency
      with the network refactor.

## Phase 3: relay hardening in the module

Whilst student VMs have no route to the internal network, the relay does, and
nothing yet prevents a compromised relay from being used to pivot into
172.22.0.0/16 with the same access as any other internal machine. The real
security boundary is therefore whether a lab VM can compromise the relay. The
following items reduce that risk within SecGen and the module alone, and are
required before students (rather than staff) use the relay.

- [ ] **Verify on a built relay** (read-only, via the guest agent) which services
      listen on the lab interface, whether a `vagrant` account or insecure key is
      present, and the value of `net.ipv4.ip_forward`.
- [ ] **In-guest firewall** (nftables, managed by Puppet): on the internal NIC,
      permit only DHCP and outbound connections to the gateway port; on the lab
      NIC, permit only inbound connections to the relay port. Root on the relay
      could remove these rules, but they prevent casual pivoting and limit what
      is exposed to students.
- [ ] **Set `net.ipv4.ip_forward=0` explicitly**, rather than relying on the
      Debian default.
- [ ] **Lock down logins**: disable SSH, or bind it to the internal side only,
      and remove or lock the `vagrant` account.
- [ ] **Rate limiting** per lab IP in nginx, so that a single VM (or a malware
      sample) cannot monopolise the Sparks.
- [ ] **Decide between a dedicated relay VM and the Hackerbot server.** The PDF
      suggests the Hackerbot server; however, Hackerbot exposes IRC and its bot
      services to students by design, each of which is a potential way onto a
      machine with an internal leg. A small dedicated relay keeps the exposed
      surface to nginx alone.

## Phase 4: Proxmox firewall

The PDF's `llm-only` security group (outbound only to 172.22.222.222 on ports
8080 and 4000, everything else dropped in both directions, with `ipfilter` to
prevent address spoofing) is enforced on the Proxmox host, so even root inside
the relay cannot remove it; this is what stops a compromised relay from being a
pivot. The PDF describes it as initial thinking rather than a tested design.

- [ ] Enable the firewall at datacenter and node level (an infrastructure change:
      enabling the datacenter firewall for the first time alters the hosts' own
      inbound policy, so web UI and SSH access must be checked from a console
      first).
- [ ] Add the `llm-only` group to `/etc/pve/firewall/cluster.fw`.
- [ ] Extend the Proxmox post-build in SecGen to set `firewall=1` on the relay's
      internal NIC and to write its per-VM firewall file (group, `ipfilter` and
      IP set) through the Proxmox API, using the static address from the
      `internal_network` module, since the rules block DHCP.
- [ ] Confirm that the rules apply live and that the relay can still reach the
      gateway (and nothing else) once they are enabled.

## Phase 5: per-lab keys

- [ ] Gateway: a key database on the LiteLLM gateway (port 4000), so that keys can
      be issued and expired programmatically.
- [ ] SecGen: request a key per VM set during the build and pass it through the
      existing `SECGEN_LLM_API_KEY` route, so that no shared key is used.
- [ ] Expire keys at the end of the lab. Per-lab keys would also make it possible
      to attribute requests to a lab, which a single shared key cannot.

## Phase 6: MCP agent route (option 2 in the PDF)

- [ ] Await Harry's 32K and 64K token benchmarks, which will indicate how many
      students the Sparks can support when an agent resends long contexts on
      every round.
- [ ] GhidrAssistMCP in Ghidra, with OpenCode configured for the Spark models
      (the PDF's `opencode.json` sketch is untested).
- [ ] Switch off the GhidrAssistMCP tools that a lab does not need, which reduces
      both the context budget and what the agent can alter.
- [ ] Enable tool calling for the `demo` model (vLLM's hermes tool parser) if
      the prompt injection exercises are to use option 2.

## Phase 7: lab content

- [ ] A lab scenario with `llm_assistant` enabled and the relay wired in.
- [ ] Teaching activities from the PDF: explain then verify, a human first pass
      compared with the assistant, `fast` against `smart`, and prompt injection
      through a crafted binary (the weak `demo` model shows the effect most
      clearly).
- [ ] Keep `llm_assistant` off on assessed reverse engineering labs, unless its
      use is explicitly permitted.

## What the PDF does and does not cover

| Topic | In the PDF? |
|---|---|
| Route A (relay) over route B (NIC on every student VM) | Yes |
| Proxmox firewall `llm-only` group and its caveats | Yes, as untested initial thinking |
| Shared lab key for development, per-VM expiring keys before Hacktivity | Yes |
| Binaries never leaving the network; only decompiled text sent | Yes |
| Malware spreading from the lab network | No |
| How a student VM might compromise the relay (SSH, `vagrant` account, Hackerbot services) | No |
| Packet forwarding or a firewall inside the relay VM | No |
| Arbitrary input reaching the gateway API, rate limiting | No |
| Attribution of requests under a shared key | No |
| A dedicated relay VM as an alternative to the Hackerbot server | No |

## Open questions

- Who owns enabling the datacenter firewall on the cluster, and when?
- Should the relay be a dedicated VM, or run on the Hackerbot server as the PDF
  suggests (with Hackerbot's services then inside the trust boundary)?
- Can vagrant-proxmox attach an untagged NIC on `vmbr3` at build time (the
  preferred approach in Phase 2), or must the internal network still be added
  in the post-build?
- Could the gateway sit on its own small network segment, rather than the
  general 172.22.0.0/16 network, so that the relay's internal NIC cannot reach
  anything else even without host rules?
- Is GhidrAssist's 12.1 build reliable on Ghidra 12.1.4 across a full lab, or
  should the plugin be built against 12.1.4?
