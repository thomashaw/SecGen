# Hackerbot bot XML: elements and runtime semantics

Everything here is from `modules/utilities/unix/hackerbot/files/opt_hackerbot/hackerbot.rb` (cited as
`hb:LINE`). `hackerbot_schema.xsd` is a stub and schema validation is commented out, so the runtime is the only
spec. When this file and the code disagree, the code wins. Re-read it.

## Loading (hb:69-130)

- Every `/opt/hackerbot/config/*.xml` is loaded (`Dir.glob("config/*.xml")`), one per `hackerbot_configs`
  value. Each file becomes one bot, keyed by `<name>`, so two files with the same name overwrite each other.
- Parsed with `Nokogiri::XML` in **recover mode**. Parse errors are printed, then whatever libxml2 salvaged
  is used. Bare `&` is dropped silently; mismatched tags restructure the tree.
- Namespaces are removed. Then `<name>`, `<AIML_chatbot_rules>`, and `<get_shell>` are read with `.text`, so
  each must exist.
- `<messages>` and each `<attack>` are converted to hashes with **Nori**, which has these consequences:
  - A repeated child becomes an **Array**, a single one becomes a **String/Hash**, and an empty one (`<x/>`)
    becomes `nil`, though the key still exists (`key?` is true). That is why flag-like elements such as
    `<trigger_quiz/>` and `<show_attack_numbers/>` work while empty.
  - The xpaths are `//messages` and `//attack`, which are **document-wide**, so use one `<hackerbot>` per file.
- State (current attack, quiz, accumulated outputs) is **per bot, not per user**. That suits one student
  per hackerbot server.

## Top level

```xml
<hackerbot xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xsi:schemaLocation="http://www.github/cliffe/SecGen/hackerbot">
  <name>Hackerbot</name>                         <!-- IRC nick; joins #Hackerbot and #bots -->
  <AIML_chatbot_rules>config/AIML</AIML_chatbot_rules>   <!-- small-talk fallback (ProgramR) -->
  <get_shell>ssh -i /opt/hackerbot/keys/id_rsa -oStrictHostKeyChecking=no root@{{chat_ip_address}} /bin/bash</get_shell>
  <messages>...</messages>
  <tutorial_info>...</tutorial_info>             <!-- legacy, unused at runtime -->
  <attack>...</attack>  ...
</hackerbot>
```

## `<messages>`

| Element | Count | Used when |
|---|---|---|
| `show_attack_numbers` (empty) | 0/1 | its presence prefixes each prompt with `** #N **` |
| `greeting` | exactly 1 | on `hello` |
| `say_ready`, `next`, `previous`, `goto`, `last_attack`, `first_attack`, `getting_shell`, `got_shell`, `repeat` | **≥ 2** (`.sample`) | navigation, and the start and end of `ready` |
| `help`, `say_answer`, `no_quiz`, `correct_answer`, `incorrect_answer`, `invalid`, `non_answer` | exactly 1 | the respective event |
| `shell_fail_message` | 1 | default when shell fails; an attack can override it |

A sampled message with only one alternative is a String, so `.sample` raises NoMethodError. Cinch logs it
to the console and the student sees the bot go quiet partway through a reply.

## Chat commands (hb:139-466)

Cinch runs **every** handler whose pattern matches, and String patterns are anchored (`'next'` matches only
`next`).

| Student says | Handler |
|---|---|
| anything containing `hello` | greeting, then the current attack's prompt, then `say_ready` |
| anything containing `help` | `help`. Note this is unanchored, so `answer ... help ...` also triggers it |
| `next` / `previous` | moves ±1 (and calls `update_bot_state`, which clears `current_quiz` but **keeps** accumulated outputs) |
| `goto N` / `attack N` | jumps to N (1-based) |
| `list` | every prompt, with `-->` marking the current one |
| `answer X`, `the answer is X`, `answer: X` | quiz check (below). It does **not** require the quiz to have been asked |
| `ready` | runs the attack (below) |
| anything else | AIML reply; if empty and the message contains `?`, `non_answer` |

## `<attack>`

Allowed children (anything else is ignored):

| Element | Meaning |
|---|---|
| `prompt` | announced on arrival at the attack, and listed by `list` |
| `pre_shell` | shell command run **locally on the hackerbot server** (backticks) before getting shell; output goes through the conditions |
| `get_shell` | per-attack override of the top-level `get_shell`; `false` means skip the shell step |
| `post_command` | written to the shell's stdin once it is obtained, so it runs **on the target**. Empty means nothing is sent |
| `post_shell` | local command run after the shell step; output goes through the conditions |
| `suppress_command_output_feedback` (empty) | stops the `FYI: <output>` echo (needed whenever output contains an answer) |
| `condition` (≥ 2) / `else_condition` | output matching (below) |
| `shell_fail_message` | per-attack shell-failure message |
| `quiz` | `question`, `answer`, `correct_answer_response`, optional `trigger_next_attack` |
| `tutorial` | legacy lab-sheet text, unused |

Placeholder `{{chat_ip_address}}` is substituted in `pre_shell`, `get_shell`, `post_command`, `post_shell`
with the IRC host of the sender, which is the desktop IP.

### `ready` sequence (hb:313-467)

1. Reply with `getting_shell`.
2. If `pre_shell` exists: run it, reply `FYI: out` (unless suppressed), append the output to
   `pre_shell_command_outputs`, and run the conditions on it.
3. Shell command = the attack's `get_shell`, else the bot's. If it is not `false`:
   - `Open3.popen2e(cmd + ';')`. Every 5 s, send `echo shelltest` and read stdout. Up to 60 tries, with an
     overall **240 s timeout**. During the wait the student sees `...`, and on timeout `Took too long...`.
   - Got shell: reply `got_shell`, send `post_command`, sleep 3 s, close stdin, and read up to **15 s** more
     (after that the process is killed). Store the output as `post_command_output(s)`, `FYI` it, and run the
     conditions.
   - No shell: reply `shell_fail_message`. If the output contains `command not found`, it is shown too.
   - Child processes are `kill -9`'d afterwards, so long-running background attacks die.
4. If `post_shell` exists: run it locally, `FYI` it, and run the conditions.
5. Reply with `repeat`.

So conditions can be evaluated **up to three times** per `ready` (pre_shell, post_command, post_shell), each
time with only that step's output. The `post_command` output also contains the `shelltest` line(s) and
anything the shell printed. Design regexes with that in mind, or have the command print a marker (e.g. `echo --$?`
and match `^--0`).

### Conditions (`check_output_conditions`, hb:17-67)

For each `<condition>`, in document order, the first that matches wins:

- `output_matches` matches when `output =~ /re/m`. It is **unanchored**; with `/m`, `.` also matches newlines.
- `output_not_matches` matches when the output does **not** match.
- `output_equals` compares against `output.chomp`, so it works only when the output is exactly that.

On a match, the bot replies `message` (usually containing `<%= $flags.pop %>` for the reward), then:

- `trigger_next_attack`: advances and prompts the next attack, or `last_attack` if this was the last.
- `trigger_quiz`: asks `quiz/question` + `say_answer`. **Requires a `<quiz>`**, otherwise it crashes.

With no match, the bot replies with `else_condition/message`, if one exists.

### Quiz answers (hb:200-252)

```
correct = quiz.answer
  .gsub('{{post_command_output}}',            all post_command outputs, stripped, joined '|')
  .gsub('{{shell_command_output_first_line}}', first line of every output that went through the
                                               conditions (pre_shell, post_command, post_shell), joined '|';
                                               only substituted once a get_shell has been attempted)
  .gsub('{{pre_shell_command_output_first_line}}', first line of each pre_shell output, joined '|')
match if student_answer.strip =~ /^(?:correct)$/i
```

- The answer is a **regex** and is case-insensitive. Literal answers containing `. + * ? ( ) [ ] |` must be
  escaped, or written as patterns deliberately (`^/etc/.*sh$` is used in practice).
- Substituted output is **not** escaped. A path like `/etc/a.b` also accepts `/etc/aXb`. That is usually fine.
- Outputs accumulate across every `ready` of that attack (they are never cleared), and any earlier
  run's answer is still accepted.
- A multi-line output can never match, since `^...$` must cover the whole answer. Make the command print
  exactly one line, or print `alt1|alt2` to accept several answers (see `integrity_detection`'s
  mv-swap attack: `echo "$mv1 $mv2|$mv2 $mv1"`).
- Each placeholder only works if its step actually ran: `{{post_command_output}}` needs a shell,
  `{{pre_shell_command_output_first_line}}` needs a `pre_shell`. Avoid `{{shell_command_output_first_line}}`: only the unused legacy
  `integrity_detection/templates/hackerbot_intro.xml.erb` uses it, and on an attack the bot never navigated to,
  `shell_command_outputs` is still nil, so the answer handler crashes.
- On a correct answer the bot replies `correct_answer`, then `correct_answer_response` (usually the flag), then
  `trigger_next_attack` if present. On a wrong one it replies `incorrect_answer (their answer)`.

## Multi-step challenges: staging several `ready`s inside one `<attack>`

`current_attack` (the persistent per-bot position) only moves in five places: `next`, `previous`,
`goto/attack N`, a matched `<condition>` carrying `<trigger_next_attack/>`, and a correct quiz answer
carrying `<trigger_next_attack/>`. A matched condition with **neither** `<trigger_quiz/>` nor
`<trigger_next_attack/>` just replies its `<message>` and stays put — so the **next `ready` re-runs the same
attack**. `trigger_quiz` also stays put (it only asks the question). This is the lever for multi-step
challenges: the `<attack>` XML is fixed, but the shell command reads/writes a **stage file** and does a
different thing — echoing a **distinct marker per stage** — on each successive `ready`, and different
`<condition>`s match each marker. This gives a whole prepare → baseline → attack → quiz → flag flow in **one
challenge, with the student only ever saying `ready` then `answer`** (no `next`). It is proven working on the
real bot (`integrity_detection` challenges 1 and 6-9).

Pattern (used for the "detect the change" integrity challenges):

- `ready` #1 → stage file absent → **prepare**: roll the target back to a known-good state, create the stage
  file, `echo PREPARED`. A condition matching `PREPARED` (no trigger) replies "take your baseline of <dir>,
  then say 'ready'".
- `ready` #2 → stage file present → **attack**: make the change, remove the stage file, `echo` a change marker.
  A condition matching that marker carries `<trigger_quiz/>`.
- `answer` → `<quiz>` with `<trigger_next_attack/>` advances.
- Re-attempts fall out for free: after the attack the stage file is gone, so the next `ready` re-prepares
  (rolls back), letting the student re-baseline a clean state.

Rules that matter for staged attacks:

- **Markers must not be substrings of one another** (`output_matches` is an unanchored regex). `PREPARED` vs
  `CREATED` vs `ADDED0` are safe; order the prepare condition first.
- **The `<answer>` must be a build-time-known value** (e.g. `^<%= Regexp.escape($path) %>$`), **not**
  `{{post_command_output}}`/`{{pre_shell_command_output_first_line}}`. Outputs accumulate across every `ready`
  (prepare *and* attack), so a placeholder answer becomes `PREPARED|CREATED …` — wrong. Choose/plant a
  build-time-random target so the answer is known. (If the target can only be picked at runtime — e.g. the
  swap pair — keep the placeholder but make sure only the attack branch echoes a path, and accept that the
  prepare marker also becomes an accepted answer; harmless.)
- **`pre_shell` captures stdout only** (`pre_output = ` backticks, hb:321), so a remote `ssh` error such as
  "No route to host" goes to **stderr and is dropped** — the "couldn't reach your system" condition never
  fires and you fall through to `else_condition`. Append `2>&1` to the `ssh` (not to a stdout-redirected
  pick) so those conditions work. `post_command`/`get_shell` use `popen2e`, which already merges stderr.
- `getting_shell` (and `got_shell`, when `get_shell` is not `false`) fire on **every** `ready`, including the
  prepare one — so a prepare step shows "Gaining shell access…"/"You are pwned.". Use `pre_shell` +
  `<get_shell>false</get_shell>` to avoid the `got_shell` line on a prepare step.
