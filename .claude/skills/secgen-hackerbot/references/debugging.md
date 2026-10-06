# Debugging Hackerbot labs

`S=.claude/skills/secgen-hackerbot/scripts/hb_check.rb`, `G=modules/generators/structured_content/hackerbot_config`.

## Build time (`secgen.rb ... build-project` / `run`)

SecGen prints `Running: ruby .../secgen_local/local.rb --b64 ...`. On failure it prints the generator's
stderr, then `Module failed to run`. Reproduce it in isolation with the scenario's real inputs:

```bash
ruby $S $G/<lab> --scenario scenarios/.../<lab>.xml [--input name=value ...]
```

| Symptom (stderr) | Cause | Fix |
|---|---|---|
| `GetoptLong::InvalidOption: unrecognized option '--x'` | scenario or default passes an input `local.rb` doesn't accept | add it to `get_options_array`/`process_options` and `read_fact`, or stop passing it |
| `NoMethodError ... in 'process_options'` | option handler writes to a missing accessor (copy-paste, e.g. `self.ids_server_ip`) | fix the accessor name |
| `(erb):N: undefined method 'x' for nil` | template line N reads an input the scenario didn't pass (`self.foo.first` is nil), or indexes `IP_addresses[k]` past the end | pass the input, or fix the index/comments in the scenario |
| `JSON::ParserError ... (erb):N` | `JSON.parse` of a missing input (`self.accounts[1]` with 1 account, `coconut_config` unset) | pass it, or guard it |
| `Sorry, you need to provide an account` | no `accounts` input | wire `<datastore>accounts</datastore>` into the generator |
| `Warning: Not enough flags provided` | fewer flags than `REQUIRED_FLAGS`; random padding used | match the `flag_generator` defaults or scenario flags to the `$flags.pop` count |
| `wrong number of arguments (given 3, expected 1)` in `ERB#initialize` | running outside `bundle exec` with erb ≥ 5 installed | use `bundle exec`, or `hb_check.rb` (which shims it) |
| `default_input into='x' has no corresponding read_fact` (SecGen exits at startup) | metadata drift | add the `read_fact` |

## Runtime: bot on the hackerbot_server

You often don't need the VM to see the exact bot config. The generated project's `scenario.xml`
(`lib/output/xml_scenario_generator.rb`) records every module's resolved inputs as literal `<value>`s,
including the hackerbot utility's `hackerbot_configs` JSON. Extract `xml_config` from there and run
`ruby $S --xml` on it. The same file holds the `spoiler_admin_pass` value that the cleanup build sets as
root's password, for logging in to the VMs (or use `vagrant ssh` from the project dir).

```bash
systemctl status hackerbot ergochat hackerbot-webclient
journalctl -u hackerbot -e                     # bot stdout/stderr (Print.debug is very verbose)
ls -l /opt/hackerbot/config/                   # bot_0.xml ... one per hackerbot_configs value
ls -l /opt/hackerbot/keys/id_rsa               # must exist, 0600
# foreground run: see handler exceptions that never reach chat
systemctl stop hackerbot; cd /opt/hackerbot && ruby hackerbot.rb
```

Copy `bot_0.xml` back and run `ruby $S --xml bot_0.xml`. It catches the structural crashes below without
another build.

| Symptom in chat | Likely cause | Check |
|---|---|---|
| Bot never appears / service restarting | XML so broken that `name`/`get_shell`/`AIML_chatbot_rules` is missing; gem missing (cinch, nori, programr, nokogiri); ergochat not up | `journalctl -u hackerbot`; `ruby $S --xml` |
| Bot answers `hello` but stops partway through a reply | a `.sample`d message has only one alternative (NoMethodError on String) | checker: `<messages><x> appears once` |
| `ready` → `getting_shell` → silence | attack has 0 or 1 `<condition>` (nil / Hash `.each`), or `trigger_quiz` without `<quiz>` | checker; foreground run shows the exception |
| `...` repeated, then `Took too long...` or `shell_fail_message` | SSH as root to `{{chat_ip_address}}` failed: `hackerbot_client` missing on the target, a different `hackerbot_key` datastore, sshd down, or the student's defence blocked root SSH | on the server: `ssh -i /opt/hackerbot/keys/id_rsa -oStrictHostKeyChecking=no root@<desktop_ip> echo ok`; on the desktop: `/root/.ssh/authorized_keys` |
| shell targets the wrong host | `{{chat_ip_address}}` is the IRC host. If ergochat cloaking/hostname lookup were enabled, or the student connected through a proxy/NAT, it isn't the desktop IP | ergochat `ircd.yaml`: `ip-cloaking.enabled: false`, `lookup-hostnames: false`; `/whois <nick>` |
| `Looks like there is some software missing` | `command not found` during the shell step | install the tool on the hackerbot server (or target) via a utility |
| Defence was right but the reply is the wrong condition or `else` | condition order or regex: unanchored `output_matches`; output includes the `shelltest` lines and the shell banner; `&&` silently deleted from the command | run the `post_command` by hand as root over the same SSH; `ruby $S --xml` for bare `&` |
| `Incorrect (answer)` for the right answer | answer regex has unescaped `.`/`+`/`(`; `{{post_command_output}}` is multi-line or has extra text; answer has trailing spaces in the XML | `grep -A3 '<answer>' bot_0.xml`; check the post_command prints exactly one line |
| `ready` repeats the same reply forever / never advances | the current attack has a matched `<condition>` with no `<trigger_quiz/>`/`<trigger_next_attack/>` (e.g. a "prepare"/reset step), so `current_attack` never moves. Intended for the prepare stage of a staged challenge (next `ready` runs the attack stage via the stage file); a bug only if the attack stage's marker/condition never matches | check the stage file toggles (`ls /root/.hb_stage` on the bot) and that the attack-branch marker matches its condition; see bot-xml.md "Multi-step challenges" |
| staged attack: "couldn't reach your system" never shows (always `else_condition`) | `pre_shell` backticks drop stderr, so the ssh error text is lost | add `2>&1` to the `ssh` call |
| `There is no question to answer` | the current attack has no `<quiz>` (the student is on a different attack: `list`) | `list` / `goto N` |
| Earlier attempt's answer also accepted | by design: outputs accumulate across `ready`s | n/a |
| The bot gives away the answer as `FYI: ...` | missing `<suppress_command_output_feedback/>` | add it |
| `next` after the last attack loops on `last_attack` | by design | n/a |

## Runtime: web client and IRC

- The desktop browser opens `http://hackerbot:8080` (iceweasel `start_page`). `hackerbot` resolves via the
  `hosts` utility to `IP_addresses[1]`, so check that `/etc/hosts` on the desktop points at the hackerbot_server.
- The page connects with a WebSocket to `ws://<irc_server_ip>:8097/`. `irc_server_ip` is resolved **by the browser
  on the desktop**, so `hackerbot` is correct and `localhost` is wrong.
- It registers with the nick set to the desktop user's username and auto-sends `hello` to `hackerbot_nick`.
  If the bot's `<name>` differs (e.g. `Bossbot`), nothing answers. Set `hackerbot_nick`.
- Plain IRC clients use port 6667 on the hackerbot server. Join `#<botname>` or DM the bot.
- `curl -s hackerbot:8080/config.js` from the desktop shows the baked-in nick, target, and server.

## When a lab "used to work"

Check what changed since the Apache retirement (`git log -- modules/utilities/unix/hackerbot lib/objects/local_hackerbot_config_generator.rb`):
the generator now outputs only `{"xml_config": ...}`, and there is no lab sheet, redcarpet, or `html_template_path`.
A `local.rb` that still sets `self.html_template_path` raises NoMethodError, because the accessor is gone.
