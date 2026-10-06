---
name: secgen-hackerbot
description: Understand, create, and debug SecGen Hackerbot labs - the hackerbot utility (an IRC chatbot that attacks/tests the student's VM over SSH and rewards flags in chat) and the hackerbot_config generators (modules/generators/structured_content/hackerbot_config/*) whose local.rb + .xml.erb templates produce the bot's attack XML. Use when asked how a Hackerbot lab works, to write a new hackerbot_config generator or add/change an <attack>, to wire a hackerbot lab into a scenario, or to debug a bot that won't start, won't answer, fails to get shell, never matches a condition, rejects a correct quiz answer, or a generator that fails during `secgen.rb build-project`. Trigger words - "hackerbot", "hackerbot_config", "bot_0.xml", "post_command", "trigger_quiz", "the bot says Failed to get shell".
---

# SecGen Hackerbot labs

A Hackerbot lab is a **defensive** guided lab: the student works on their own `desktop` VM, chats to a bot that
announces an attack, prepares a defence, says `ready`, and the bot SSHes in as root, runs the attack, regex-matches
the output, and replies with a flag (or a hint) in chat.

Related skills, to use instead of or alongside this one:

- **`convert_hackerbot_to_hacktivity_lab_sheets`**: lab sheets. They are no longer generated or served by SecGen. The
  `<tutorial>` / `<tutorial_info>` elements in the templates are leftovers that nothing reads.
- **`review-secgen-scenario`**: rule S8 covers bot wiring at scenario level (flag count, winnable/losable attacks,
  key handshake, IRC transport).
- **`review-secgen-module`**: R1–R3 apply to generators too, grepped in `local.rb` and ERB, not Puppet.

## How the pieces fit

```
scenario.xml
 ├─ desktop VM
 │    hackerbot_client ── installs hackerbot_key public half in /root/.ssh/authorized_keys
 │    hosts  ("hackerbot" -> hackerbot_server IP)   iceweasel start_page hackerbot:8080
 └─ hackerbot_server VM (Kali, has msf/nmap for attacks)
      ergochat (ircd)  :6667 plain, :8097 websocket; ip-cloaking + lookup-hostnames OFF
      hackerbot_webclient  python http.server :8080, DMs the nick `hackerbot_nick` (default "Hackerbot")
      hackerbot utility  ── input hackerbot_configs <- generator .*/hackerbot_config/<lab>
                         ── input ssh_key_pair     <- hackerbot_key datastore (private half)

build time:  SecGen runs  <lab>/secgen_local/local.rb  (stdin "--b64 --accounts=<b64> --flags=<b64> ...")
             -> HackerbotConfigGenerator#generate renders config_template_path (ERB, trim '<>-')
             -> outputs base64( {"xml_config": "<hackerbot>...</hackerbot>"} )
puppet:      hackerbot/manifests/config.pp writes each value's xml_config to /opt/hackerbot/config/bot_<N>.xml,
             private key to /opt/hackerbot/keys/id_rsa; hackerbot.service runs /opt/hackerbot/hackerbot.rb
runtime:     hackerbot.rb (Cinch) connects to IRC on **localhost** (it is started with no --irc-server, so
             the utility's server_ip read_fact is unused, and ergochat must be on the same VM).
             student says "ready" -> bot runs pre_shell, then get_shell (ssh root@{{chat_ip_address}} /bin/bash),
             pipes post_command into it, then post_shell; each output goes through <condition>s
```

`{{chat_ip_address}}` is the **IRC host of whoever is chatting**, meaning the desktop's IP. It works only
because ergochat has cloaking and hostname lookup turned off (`modules/services/unix/irc/ergochat/files/ircd.yaml`).

Key files:

| What | Where |
|---|---|
| Bot runtime (read this when behaviour is in doubt) | `modules/utilities/unix/hackerbot/files/opt_hackerbot/hackerbot.rb` |
| Generator base class | `lib/objects/local_hackerbot_config_generator.rb` (< `local_string_generator.rb`) |
| Bot puppet | `modules/utilities/unix/hackerbot/manifests/{install,config,service}.pp` |
| Cleanest current generator to copy | `hackerbot_config/integrity_detection` (single template, all features, checks clean) |
| Multi-VM / composed-template example | `hackerbot_config/hacker_vs_hackerbot_2` (`lab.xml.erb` pulls in per-attack `*.xml.erb`) |
| Current scenario wiring | `scenarios/labs/response_and_investigation/2_integrity_detection.xml` |

Don't copy `example_bot` (its output is not the JSON the utility parses), `hackerbot_intro.xml.erb` (it uses an
undefined `TEMPLATES_PATH`), or `dead_analysis` v1 (it has a `trigger_quiz` without a `quiz`).

## The checker script

`scripts/hb_check.rb` runs a generator's **real** `local.rb` the same way SecGen does: base64 args on stdin,
with stub inputs and an erb-compat shim (`hb_prelude.rb`) so plain `ruby` works without `bundle exec`. It then
checks the bot XML against what `hackerbot.rb` actually needs. It needs only the `nokogiri` gem.

```bash
S=.claude/skills/secgen-hackerbot/scripts/hb_check.rb
G=modules/generators/structured_content/hackerbot_config
ruby $S $G/integrity_detection                       # stub every option local.rb accepts
ruby $S $G/rema_coconut --scenario scenarios/labs/software_and_malware_analysis/11_coconut.xml \
        --input coconut_config='{"...":"..."}'        # pass only what that scenario wires in
ruby $S --xml bot_0.xml                               # check a config pulled off a deployed server
```

Flags: `--input k=v` (repeatable), `--flags N`, `--accounts N`, `--out DIR`. It writes `bot.xml` and
`generator.log` to the out dir. Exit status is 1 on any ERROR.

Run it after every change to a generator. Treat ERRORs as bugs. Read each WARNING and decide; for example,
"gets a shell but has no `<post_command>`" is deliberate for attacks whose conditions read the result of
`post_shell`.

Without `--scenario`, every accepted option gets a stub. That exercises all of `process_options`, so it
catches latent bugs such as `hb_labtainer`'s `--hackerbot_server_ip` writing to a nonexistent `ids_server_ip`.
Use `--scenario` to find out whether a failure actually breaks a shipped lab.

## Understanding a lab

1. Read the scenario: which generator runs, and which `<input>`s it gets (accounts, `*_ip`, flags). Index
   comments in the scenario (e.g. `IP_addresses` order) must match the template's indexes.
2. Read `secgen_local/local.rb`: which extra options it accepts, and which `.xml.erb` it renders.
3. Read the template's `<% %>` header: globals (`$main_user`, `$flags`, `REQUIRED_FLAGS`, IPs), then each
   `<attack>`, following any `ERB.new(File.read ...)` includes.
4. Render it: `ruby $S $G/<lab> --scenario <scenario>`, then read `bot.xml`. The rendered attack is easier to
   follow than the ERB.
5. For each attack, state what the student must do and which `<condition>` releases the flag. Then check that
   a wrong defence falls through to a non-reward condition.

## Creating or changing a lab

Read `references/generator.md` for skeletons of the metadata, `local.rb`, template header, attack patterns, and
scenario wiring. Read `references/bot-xml.md` for every element and its exact runtime semantics. Rules that
are easy to get wrong:

- **Every `<attack>` that runs anything needs ≥ 2 `<condition>`s plus an `<else_condition>`.** Nori turns a
  single element into a Hash, not an Array, so `.each` crashes. An attack with none crashes on `nil.each`.
- **Every message the bot `.sample`s needs ≥ 2 alternatives**: `say_ready next previous goto last_attack
  first_attack getting_shell got_shell repeat`. The single-reply messages must appear exactly once.
- **Escape `&` as `&amp;` in commands** (or use CDATA). The bot parses in recover mode, so a bare `&&` is
  **silently deleted** and `a && b` runs as `a  b`. Also escape `<` (`&lt;`) in commands and regexes.
- **Conditions are tried in order, and the first match wins.** `output_matches` is an unanchored multiline
  regex, so `0` matches any output containing a zero. Put the specific failure branches (permission
  denied, read-only) before broad success branches, or anchor the success branch, or use
  `output_equals`.
- **A quiz `<answer>` is a regex**, matched as `/^(?:answer)$/i`. Escape literal `.` `+` `(`. Placeholders
  are substituted unescaped and joined with `|` across every attempt. So a `post_command` that feeds
  `{{post_command_output}}` must print **exactly the answer on one line**, and it can print `a|b` to accept
  either. Add `<suppress_command_output_feedback/>`, or the bot replies `FYI: <output>` and gives the answer away.
- **One `$flags.pop` per reward site, and `REQUIRED_FLAGS` equal to that count.** Make the metadata's
  `flag_generator` default count, and any flags passed in the scenario, match it as well. Padded flags
  (`flag{<random hex>}`) are not tracked by SecGen. The checker reports both under- and over-supply.
- **Each input the scenario passes must be an option `local.rb` accepts**, or GetoptLong aborts the build.
  Keep `<read_fact>`s, `get_options_array`, and `process_options` in sync, and make sure each option writes
  to an accessor that actually exists.
- **Commands run as root on the hackerbot server**: `pre_shell`/`post_shell` run locally, and `get_shell`
  wraps a `bash` on the target. To reach other VMs, use the same key:
  `ssh -i /opt/hackerbot/keys/id_rsa -oStrictHostKeyChecking=no root@<%= $ids_server_ip %> ...`. That VM
  also needs `hackerbot_client` with the same `hackerbot_key` datastore.
- The bot's `<name>` is its IRC nick. It must equal `hackerbot_webclient`'s `hackerbot_nick` input (default
  `Hackerbot`), or the web client DMs nobody.
- **Multi-step challenges (prepare → baseline → attack → quiz → flag in ONE `<attack>`, no `next`)**: have the
  shell step toggle on a **stage file** and echo a distinct marker per stage; a condition matching the
  "prepare" marker has no trigger (so `ready` stays on the attack and re-runs), the "attack" marker's
  condition has `<trigger_quiz/>`. This also gives a **revert/rollback** step for free (prepare restores a
  known-good state, so re-attempts re-baseline cleanly). The quiz `<answer>` must be build-time-known, not
  `{{post_command_output}}` (outputs accumulate across readys). See `references/bot-xml.md` ("Multi-step
  challenges") and the example in `references/generator.md`. This is what `integrity_detection` uses.

## Debugging

Start with `references/debugging.md`, which maps each symptom to its cause and gives the commands to run on
the VMs. The quick path:

1. **Build fails at the generator** ("Module failed to run"): run
   `ruby $S $G/<lab> --scenario <scenario>`. That reproduces it in seconds and prints the real exception.
   `(erb):N` is line N of the template.
2. **Built, but the bot misbehaves**: copy `/opt/hackerbot/config/bot_*.xml` off the server and run
   `ruby $S --xml bot_0.xml`. Most runtime crashes are structural (Nori single elements, missing quiz,
   deleted `&`) and the checker reports them.
3. **Still unclear**: stop the service and run the bot in the foreground on the server
   (`systemctl stop hackerbot; cd /opt/hackerbot && ruby hackerbot.rb`). Exceptions inside Cinch handlers
   are logged there and never shown in chat. Then reproduce the attack's commands by hand as root on
   the server.

When you finish, report what you changed, the checker's result before and after, and anything you could only
verify by deploying (shell access, timing, real command output).
