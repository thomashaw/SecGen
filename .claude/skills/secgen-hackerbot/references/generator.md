# Writing a hackerbot_config generator

A generator lives at `modules/generators/structured_content/hackerbot_config/<lab>/`:

```
<lab>/
  secgen_metadata.xml        # <generator>, type hackerbot_config, read_facts + defaults
  <lab>.pp                   # empty, plus manifests/.no_puppet (it's a generator, not a Puppet module)
  secgen_local/local.rb      # subclass of HackerbotConfigGenerator; extra options; names the template
  templates/lab.xml.erb      # the bot config; may ERB-include per-attack *.xml.erb files
```

`labsheet.html.erb`, `shared/`, and `*.md.erb` files in existing generators are left over from when SecGen hosted
the lab sheets. A new lab doesn't need them. Its sheet goes to Hacktivity (see the
`convert_hackerbot_to_hacktivity_lab_sheets` skill). Leave `<tutorial>`s out, or keep them minimal.

## secgen_metadata.xml

```xml
<?xml version="1.0"?>
<generator xmlns="http://www.github/cliffe/SecGen/generator"
           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xsi:schemaLocation="http://www.github/cliffe/SecGen/generator">
  <name>Hackerbot config for a <topic> lab</name>
  <author>...</author>
  <module_license>GPLv3</module_license>
  <description>Generates a config file for a hackerbot for a <topic> lab. Topics covered: ...</description>

  <type>hackerbot_config</type>
  <platform>linux</platform>

  <read_fact>accounts</read_fact>
  <read_fact>flags</read_fact>
  <read_fact>root_password</read_fact>
  <read_fact>server_ip</read_fact>          <!-- one per extra local.rb option -->

  <default_input into="accounts">
    <generator type="account"><input into="username"><value>vagrant</value></input></generator>
  </default_input>
  <default_input into="flags">              <!-- one flag_generator per $flags.pop in the template -->
    <generator type="flag_generator"/>
    <generator type="flag_generator"/>
  </default_input>
  <default_input into="root_password"><value>puppet</value></default_input>

  <output_type>hackerbot</output_type>
</generator>
```

SecGen refuses to start if a `default_input` has no matching `read_fact` (`lib/readers/module_reader.rb`).
Each `read_fact` must also be an option `local.rb` accepts, or any scenario/default that passes it aborts
the generator with `GetoptLong::InvalidOption`. `hb_check.rb` reports drift in both directions.

## secgen_local/local.rb

```ruby
#!/usr/bin/ruby
require_relative '../../../../../../lib/objects/local_hackerbot_config_generator.rb'

class MyLab < HackerbotConfigGenerator
  attr_accessor :server_ip             # one accessor per extra option, initialised to []

  def initialize
    super
    self.module_name = 'Hackerbot Config Generator MyLab'
    self.title = 'My lab'
    self.local_dir = File.expand_path('../../', __FILE__)
    self.templates_path = "#{self.local_dir}/templates/"
    self.config_template_path = "#{self.local_dir}/templates/lab.xml.erb"
    self.server_ip = []
  end

  def get_options_array
    super + [['--server_ip', GetoptLong::REQUIRED_ARGUMENT]]
  end

  def process_options(opt, arg)
    super
    case opt
    when '--server_ip'
      self.server_ip << arg      # the SAME accessor (hacker_vs_hackerbot_1 and hb_labtainer get this wrong)
    end
  end
end

MyLab.new.run
```

What the base class gives you (`lib/objects/local_hackerbot_config_generator.rb`):

- `accounts` (array of account JSON strings), `flags` (array), and `root_password` (String; values are `<<`
  appended, so two values concatenate).
- `generate` renders `config_template_path` with `ERB.new(src, 0, '<>-')`, so `<% -%>` trims newlines.
  It outputs `{"xml_config": ...}` as JSON, which SecGen base64s. `iterations` is always 1 here.
- Every option value is an **array** of strings: use `self.server_ip.first`. Helpers that the template calls
  (e.g. `encrypt_rsa` in `asymmetric_enc_rsa`) can be defined in `local.rb`.
- Status messages go to stderr (`Print.err`, `Print.local`), because stdout carries the output.

## Template header

Copy this shape. It is what every current lab uses.

```erb
<%
  require 'json'
  require 'securerandom'
  require 'erb'

  if self.accounts.empty?
    abort('Sorry, you need to provide an account')
  end
  $first_account = JSON.parse(self.accounts.first)
  $main_user     = $first_account['username'].to_s
  $files = $first_account['leaked_filenames'].to_a
  $files = ['myfile', 'afile'] if $files.empty?          # fall back when the scenario leaks nothing

  $server_ip     = self.server_ip.first
  $root_password = self.root_password
  $flags         = self.flags

  REQUIRED_FLAGS = 4                                    # == number of $flags.pop below
  while $flags.length < REQUIRED_FLAGS
    $flags << "flag{#{SecureRandom.hex}}"
    Print.err "Warning: Not enough flags provided to hackerbot_config generator, some flags won't be tracked/marked!"
  end

  def get_binding
    binding
  end
-%>
<?xml version="1.0"?>
<hackerbot xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xsi:schemaLocation="http://www.github/cliffe/SecGen/hackerbot">
  <name>Hackerbot</name>
  <AIML_chatbot_rules>config/AIML</AIML_chatbot_rules>
  <get_shell>ssh -i /opt/hackerbot/keys/id_rsa -oStrictHostKeyChecking=no root@{{chat_ip_address}} /bin/bash</get_shell>
  <messages>
    <!-- copy the whole block from integrity_detection/templates/integrity_lab.xml.erb -->
  </messages>
  <!-- attacks -->
</hackerbot>
```

- End the header with `-%>`. Plain `%>` leaves a newline before `<?xml`. That is harmless (recovered), but
  the checker warns about it.
- Globals (`$x`) are the convention because included sub-templates are rendered with `self.get_binding`,
  a fresh binding that doesn't see header locals. Per-attack values are set inline:
  `<% $random_user = 'user' + SecureRandom.hex(3) -%>` just inside the `<attack>`.
- Using `JSON.parse(self.accounts[1])` requires the scenario to pass at least 2 accounts.
  Guard it, or document it in the metadata `description`.
- Composing attacks from separate files (`hacker_vs_hackerbot_*`):
  `<%= ERB.new(File.read(self.templates_path + 'snort_rule_1.xml.erb')).result(self.get_binding) %>`.
  Shuffle with `[...].shuffle` in the header for per-instance ordering.

## Attack patterns

All of these come from shipped labs. Escape `&` as `&amp;` and `<` as `&lt;` in every command.

**Verify a defence** (student hardens, bot attacks, the correct-defence branch pays):

```xml
<attack>
  <% $log_file = $log_files.sample -%>
  <prompt>An attempt to delete /home/<%= $main_user %>/<%= $log_file %> is coming. Stop it using file attributes.</prompt>
  <post_command>rm --interactive=never /home/<%= $main_user %>/<%= $log_file %>; echo $?</post_command>
  <condition>
    <output_matches>Operation not permitted</output_matches>      <!-- the intended defence (chattr +i) -->
    <message>:) Well done! <%= $flags.pop %></message>
    <trigger_next_attack />
  </condition>
  <condition>
    <output_matches>Permission denied</output_matches>            <!-- defended, but the wrong way -->
    <message>:( You protected the file, but not using file attributes.</message>
  </condition>
  <condition>
    <output_matches>No such file or directory</output_matches>
    <message>:( The file should exist!</message>
  </condition>
  <else_condition><message>:( We deleted your file!</message></else_condition>
</attack>
```

**Let it happen, then quiz** (detection labs). Note the `suppress` and the one-line output:

```xml
<attack>
  <prompt>Going to edit one of your files in /etc/. First, create hashes of /etc/.</prompt>
  <post_command>x=`find /etc/ -type f -name '*.sh' | sort -R | head -n 1`; echo '' >> $x; echo $x</post_command>
  <suppress_command_output_feedback />
  <condition>
    <output_matches>/etc.*</output_matches>
    <message>Good. Now answer this...</message>
    <trigger_quiz />
  </condition>
  <condition>
    <output_matches>Permission denied|Operation not permitted|Read-only</output_matches>
    <message>:( You stopped the attack, rather than monitor for changes...</message>
  </condition>
  <else_condition><message>:( Something was not right...</message></else_condition>
  <quiz>
    <question>What is the file that changed?</question>
    <answer>{{post_command_output}}</answer>
    <correct_answer_response>:) <%= $flags.pop %></correct_answer_response>
    <trigger_next_attack />
  </quiz>
</attack>
```

Watch the order: a success regex like `0` or `/etc.*` also matches error text that contains it. Put failure
branches first, or anchor the success branch (`^--0`), if the error output can contain the success pattern.

**Block the bot's shell** (`integrity_protection` attack 5). The reward goes in the per-attack
`shell_fail_message`. Still give it two conditions for the losing path. Without them, a successful shell
reaches `check_output_conditions` with no conditions and the bot goes silent instead of saying the student
failed:

```xml
<attack>
  <prompt>Finally, try to prevent me from obtaining shell access to your system</prompt>
  <post_command>echo --pwned</post_command>
  <shell_fail_message>:) Failed to get shell... <%= $flags.pop %></shell_fail_message>
  <condition><output_matches>--pwned</output_matches><message>:( We got in. Try again.</message></condition>
  <condition><output_matches>shelltest</output_matches><message>:( We got in. Try again.</message></condition>
  <else_condition><message>:( Something was not right...</message></else_condition>
</attack>
```

**Act from the hackerbot server / against another VM** (IDS, network, and backup labs):

```xml
<attack>
  <pre_shell>scp -i /opt/hackerbot/keys/id_rsa -oStrictHostKeyChecking=no root@<%= $ids_server_ip %>:/var/log/snort/alert /tmp/before; s1=$?; nmap -sT -p <%= $port %> <%= $web_server_ip %> > /dev/null; s2=$?; echo --$s1$s2; ...</pre_shell>
  <get_shell>false</get_shell>
  ...
  <condition><output_matches>^--1</output_matches><message>:( Failed to scp from the IDS.</message></condition>
  ...
</attack>
```

or override the shell target per attack (`backups`):
`<get_shell>ssh -i /opt/hackerbot/keys/id_rsa -oStrictHostKeyChecking=no root@<%= $server_ip %> /bin/bash</get_shell>`.
Every VM the bot SSHes into needs `hackerbot_client` fed the same `hackerbot_key` datastore.

Tooling on the hackerbot server: the scenario installs `metasploit_framework`, `nmap`, and `handy_cli_tools`
on the Kali base. Add a utility if an attack needs something else. A missing binary shows up in chat as
"Looks like there is some software missing" only when it breaks the shell step. Otherwise it's just
non-matching output.

**Staged multi-step challenge in ONE attack** (prepare → baseline → attack → quiz → flag, no `next`). The
shell step toggles on a stage file and echoes a distinct marker per stage; see `references/bot-xml.md` for the
runtime rules. Used by every `integrity_detection` "detect the change" challenge. The change (here a new file)
is a build-time-random **hidden dotfile** so the target is stable per student and the answer is build-known:

```xml
<% $c6_path = ['/etc', '/etc/default', '/etc/security'].sample + "/." + SecureRandom.hex(5) -%>
<attack>
  <prompt>A new file is about to be planted in /etc. Say 'ready' and I'll first get your system to a known-good state.</prompt>
  <pre_shell>ST=/root/.hb_stage; mkdir -p "$ST"; F=$ST/c6_newfile; K=/opt/hackerbot/keys/id_rsa; H=root@{{chat_ip_address}}; if [ ! -e "$F" ]; then R=$(ssh -i $K -oStrictHostKeyChecking=no $H 'rm -f <%= $c6_path %>; echo RBOK' 2>&amp;1); if echo "$R" | grep -q RBOK; then touch "$F"; echo PREPARED; else echo "$R"; fi; else R=$(ssh -i $K -oStrictHostKeyChecking=no $H 'touch <%= $c6_path %>; if [ -e <%= $c6_path %> ]; then echo CREATED; fi' 2>&amp;1); if echo "$R" | grep -q CREATED; then rm -f "$F"; echo CREATED; else echo "$R"; fi; fi</pre_shell>
  <get_shell>false</get_shell>
  <suppress_command_output_feedback />
  <condition>                                           <!-- stage 1: prepare, no trigger, stays on this attack -->
    <output_matches>PREPARED</output_matches>
    <message>Known-good state restored. Take a baseline of hashes of /etc, then say 'ready' and I'll plant the file.</message>
  </condition>
  <condition>                                           <!-- stage 2: attack -->
    <output_matches>CREATED</output_matches>
    <message>A new file appeared in /etc. Compare a fresh set of hashes to find it.</message>
    <trigger_quiz />
  </condition>
  <condition>
    <output_matches>No route to host|Connection refused|Permission denied</output_matches>   <!-- works only with 2&gt;&amp;1 above -->
    <message>:( I couldn't reach your system.</message>
  </condition>
  <else_condition><message>:( Something was not right...</message></else_condition>
  <quiz>
    <question>What is the full path of the newly created file?</question>
    <answer>^<%= Regexp.escape($c6_path) %>$</answer>               <!-- build-known, NOT {{post_command_output}} -->
    <correct_answer_response>:) <%= $flags.pop %></correct_answer_response>
    <trigger_next_attack />
  </quiz>
</attack>
```

A **self-inverse revert** (the swap challenge) uses the same staging but tracks a `pair` + `swapped` + `stage`
file under `/root/.hb_swap` on the bot: prepare swaps back if currently swapped (restoring clean); attack
swaps. For a change that is *created* (file/copy), prepare is just `rm -f` the planted path.

## Scenario wiring

Copy from `scenarios/labs/response_and_investigation/2_integrity_detection.xml` (desktop + hackerbot_server). The
hackerbot-specific parts:

```xml
<!-- desktop -->
<input into_datastore="IP_addresses">
  <network_ip system="desktop"/>
  <network_ip system="hackerbot_server"/>
</input>
<input into_datastore="hackerbot_key"><generator type="ssh_key_pair"/></input>
<utility module_path=".*/hackerbot_client">
  <input into="ssh_key_pair"><datastore>hackerbot_key</datastore></input>
</utility>
<utility module_path=".*/hosts">
  <input into="hosts"><value>hackerbot</value></input>
  <input into="IP_addresses"><datastore access="1">IP_addresses</datastore></input>
</utility>
<utility module_path=".*/iceweasel">
  <input into="accounts"><datastore access="0">accounts</datastore></input>
  <input into="start_page"><value>hackerbot:8080</value></input>
</utility>

<!-- hackerbot_server -->
<service module_path=".*/ergochat"/>
<utility module_path=".*/hackerbot_webclient">
  <input into="username"><datastore access="0" access_json="['username']">accounts</datastore></input>
  <input into="irc_server_ip"><value>hackerbot</value></input>
  <!-- <input into="hackerbot_nick"><value>Bossbot</value></input>  only if <name> isn't Hackerbot -->
</utility>
<utility module_path=".*/hackerbot">
  <input into="ssh_key_pair"><datastore>hackerbot_key</datastore></input>
  <input into="hackerbot_configs">
    <generator module_path=".*/<lab>">
      <input into="accounts"><datastore>accounts</datastore></input>
      <input into="server_ip"><datastore access="1">IP_addresses</datastore></input>
    </generator>
  </input>
</utility>
```

Also: add `<type>hackerbot-lab</type>` to the scenario, a `<lab_sheet_url>` once the sheet is on Hacktivity,
and a `build type="cleanup"` root-password reset on each system. Don't add `into_datastore="hackerbot_instructions"`
or `<service type="httpd"/>` for the bot; those are left over from the Apache era.

Then:

```bash
ruby .claude/skills/secgen-hackerbot/scripts/hb_check.rb modules/generators/structured_content/hackerbot_config/<lab> \
     --scenario scenarios/labs/<category>/<lab>.xml
ruby secgen.rb --scenario scenarios/labs/<category>/<lab>.xml build-project   # full resolution, no VMs
```
