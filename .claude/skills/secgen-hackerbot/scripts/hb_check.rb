#!/usr/bin/env ruby
# Hackerbot config checker.
#
# Runs a hackerbot_config generator's real secgen_local/local.rb the same way
# SecGen does (base64 args on stdin), with stub values for its inputs, then
# checks the bot XML it outputs against what /opt/hackerbot/hackerbot.rb
# actually needs at runtime. Can also check an already-rendered bot XML (e.g.
# /opt/hackerbot/config/bot_0.xml copied off a deployed hackerbot server).
#
# Usage:
#   ruby hb_check.rb <generator_module_dir> [--scenario scenario.xml]
#                    [--input key=value ...] [--flags N] [--accounts N] [--out DIR]
#   ruby hb_check.rb --xml path/to/bot_0.xml      (raw bot XML, or the {"xml_config": ...} JSON)
#
# Without --scenario, every option local.rb accepts is passed a stub value (this
# exercises all of process_options). With --scenario, only the inputs that
# scenario wires into this generator are passed (plus metadata defaults), as
# SecGen would; literal <value>s are used as-is.
# --input may be repeated, also for the same key (each becomes one array element).
# accounts/flags/root_password, metadata <value> defaults and *ip* facts are
# stubbed automatically; anything else must be given with --input.
#
# Examples:
#   ruby hb_check.rb modules/generators/structured_content/hackerbot_config/integrity_detection
#   ruby hb_check.rb modules/.../hacker_vs_hackerbot_2 --input desktop_ip=172.16.0.2 --input ids_server_ip=172.16.0.3
#
# Writes bot.xml and generator.log to --out (default: a temp dir).
# Exit status: 0 = no errors (warnings allowed), 1 = errors found.

require 'json'
require 'base64'
require 'open3'
require 'nokogiri'
require 'fileutils'
require 'tmpdir'

SECGEN_ROOT = File.expand_path('../../../..', __dir__)
PRELUDE = File.join(__dir__, 'hb_prelude.rb')

# Messages the bot calls .sample on -> must be repeated (>=2) or Nori yields a String
MULTI_MESSAGES = %w[say_ready next previous goto last_attack first_attack getting_shell got_shell repeat].freeze
# Messages replied directly -> must appear exactly once (repeated becomes an Array)
SINGLE_MESSAGES = %w[greeting help say_answer no_quiz correct_answer incorrect_answer invalid non_answer shell_fail_message].freeze
ATTACK_ELEMENTS = %w[prompt pre_shell get_shell post_command post_shell suppress_command_output_feedback
                     condition else_condition quiz tutorial shell_fail_message].freeze
CONDITION_TESTS = %w[output_matches output_not_matches output_equals].freeze
PLACEHOLDER_SOURCES = {
  '{{post_command_output}}' => 'post_command',
  '{{shell_command_output_first_line}}' => 'get_shell',
  '{{pre_shell_command_output_first_line}}' => 'pre_shell'
}.freeze

# Options the generator's Ruby actually accepts (GetoptLong rejects anything else).
def accepted_options(dir)
  src = File.read(File.join(dir, 'secgen_local', 'local.rb'))
  opts = src.scan(/\[\s*'--([A-Za-z0-9_]+)'/).flatten
  opts += %w[accounts flags root_password] if src.include?('local_hackerbot_config_generator')
  opts.uniq - %w[help b64 iterations]
end

def stub_value(name, defaults, flag_count, account_count)
  if name == 'accounts'
    names = %w[alice bob carol dave erin]
    (0...account_count).map do |i|
      { username: names[i % names.size], password: "pass#{i}word", groups: [], super_user: (i.zero? ? 'true' : 'false'),
        strings_to_leak: [], leaked_filenames: [], data_to_leak: [] }.to_json
    end
  elsif name == 'flags'
    n = flag_count || defaults[name]&.xpath('generator')&.size.to_i
    (1..n).map { |i| format('flag{stub%02d%s}', i, rand(16**8).to_s(16).rjust(8, '0')) }
  elsif defaults[name] && defaults[name].xpath('value').any?
    defaults[name].xpath('value').map(&:text)
  elsif name =~ /ip_addresses/i
    (10..25).map { |i| "172.16.0.#{i}" }
  elsif name =~ /(^|_)ip(_|$)|ip_address/i
    ["172.16.0.#{20 + name.sum % 200}"]
  elsif name == 'root_password'
    ['puppet']
  end
end

# The generator's direct <input into=...> children in a scenario, with literal
# <value>s where given (datastore/generator-fed inputs get stubbed by name).
def scenario_inputs(scenario, dir)
  doc = Nokogiri::XML(File.read(scenario))
  doc.remove_namespaces!
  rel = dir.sub("#{SECGEN_ROOT}/", '')
  gen = doc.xpath('//generator[@module_path]').find { |g| rel =~ Regexp.new(g['module_path']) }
  abort "No <generator module_path=...> in #{scenario} matches #{rel}" unless gen
  gen.xpath('input').to_h { |i| [i['into'], i.xpath('value').map(&:text)] }
end

# Builds the inputs SecGen would pass to local.rb.
def build_inputs(dir, overrides, flag_count, account_count, scenario)
  meta = Nokogiri::XML(File.read(File.join(dir, 'secgen_metadata.xml')))
  meta.remove_namespaces!
  read_facts = meta.xpath('/*/read_fact').map(&:text)
  defaults = meta.xpath('/*/default_input').to_h { |d| [d['into'], d] }
  accepted = accepted_options(dir)
  notes = []

  wanted = if scenario
             from_scn = scenario_inputs(scenario, dir)
             # SecGen also passes defaults for any read_fact the scenario left unset
             from_scn.keys | (defaults.keys & read_facts)
           else
             accepted
           end

  inputs = {}
  missing = []
  wanted.each do |name|
    v = overrides[name] if overrides.key?(name)
    v ||= from_scn[name] if scenario && from_scn[name]&.any?
    v ||= stub_value(name, defaults, flag_count, account_count)
    v ? inputs[name] = v : missing << name
  end
  (overrides.keys - wanted).each { |k| inputs[k] = overrides[k] }

  (read_facts - accepted).each do |f|
    notes << "read_fact '#{f}' is not accepted by local.rb; a scenario (or default_input) passing it makes the generator exit with GetoptLong::InvalidOption"
  end
  (accepted - read_facts).each do |f|
    notes << "local.rb accepts --#{f} but secgen_metadata.xml has no <read_fact>#{f}</read_fact>"
  end
  [inputs, missing, notes]
end

def run_generator(dir, inputs)
  local = File.join(dir, 'secgen_local', 'local.rb')
  abort "No #{local}" unless File.exist?(local)
  args = '--b64 ' + inputs.flat_map { |k, vs| vs.map { |v| "--#{k}=#{Base64.strict_encode64(v)} " } }.join
  Open3.capture3('ruby', '-r', PRELUDE, local, stdin_data: args, chdir: SECGEN_ROOT)
end

class Checker
  # Line ranges hackerbot.rb never reads: <tutorial>/<tutorial_info> (lab sheets
  # now live on Hacktivity) and XML comments.
  def inert_line_ranges(xml)
    ranges = []
    xml.to_enum(:scan, %r{<(tutorial|tutorial_info)>.*?</\1>|<!--.*?-->}m).each do
      m = Regexp.last_match
      first = xml[0...m.begin(0)].count("\n") + 1
      ranges << (first..(first + m[0].count("\n")))
    end
    ranges
  end

  attr_reader :errors, :warnings

  def initialize
    @errors = []
    @warnings = []
  end

  def err(msg) = (@errors << msg unless @errors.include?(msg))
  def warn(msg) = (@warnings << msg unless @warnings.include?(msg))

  def check(xml)
    doc = Nokogiri::XML(xml)
    # hackerbot.rb parses in Nokogiri's default RECOVER mode: it logs these and
    # carries on with whatever libxml2 salvaged.
    lines = xml.lines
    inert = inert_line_ranges(xml)
    doc.errors.each do |e|
      msg = e.message.strip
      src = lines[e.line - 1].to_s.strip[0, 120]
      in_inert = inert.any? { |r| r.cover?(e.line) }
      if msg.include?('XML declaration allowed only at the start')
        warn 'XML: whitespace before <?xml ...?> (ERB header not trimmed with -%>); harmless, recovered'
      elsif msg.include?('EntityRef')
        text = "XML: line #{e.line}: bare '&' - libxml2 recovery silently deletes it (so 'a && b' runs as 'a  b'). Use &amp; or CDATA. > #{src}"
        in_inert ? warn("#{text} [in <tutorial>/comment: unused at runtime, cosmetic]") : err(text)
      else
        err "XML: line #{e.line}: #{msg}#{in_inert ? ' [in <tutorial>/comment; may still restructure the document]' : ''}  > #{src}"
      end
    end
    doc.remove_namespaces!

    bots = doc.xpath('/hackerbot')
    if bots.empty?
      err 'No root <hackerbot> element'
      return doc
    end
    warn 'More than one <hackerbot>: hackerbot.rb uses //attack and //messages, so bots in one file share them' if doc.xpath('//hackerbot').size > 1
    bot = bots.first

    %w[name AIML_chatbot_rules get_shell].each do |el|
      err "<#{el}> missing (hackerbot.rb calls .text on it)" if bot.at_xpath(el).nil?
    end
    name = bot.at_xpath('name')&.text.to_s
    warn "Bot name '#{name}' contains characters that are awkward as an IRC nick/channel" if name =~ /[^A-Za-z0-9_\-\[\]\\^{}|`]/

    check_messages(bot)

    attacks = bot.xpath('//attack')
    err 'No <attack> elements' if attacks.empty?
    attacks.each_with_index { |a, i| check_attack(a, i + 1, bot) }
    doc
  end

  def check_messages(bot)
    msgs = bot.at_xpath('messages')
    return err('<messages> missing') unless msgs

    MULTI_MESSAGES.each do |m|
      n = msgs.xpath(m).size
      if n.zero?
        err "<messages><#{m}> missing"
      elsif n == 1
        err "<messages><#{m}> appears once; hackerbot.rb calls .sample on it, which needs >=2 alternatives (Nori turns a single element into a String)"
      end
    end
    SINGLE_MESSAGES.each do |m|
      n = msgs.xpath(m).size
      if n.zero?
        (m == 'shell_fail_message' ? method(:warn) : method(:err)).call("<messages><#{m}> missing (bot replies with an empty line)")
      elsif n > 1
        warn "<messages><#{m}> appears #{n} times; it is replied directly so the bot will print an Array"
      end
    end
  end

  def check_attack(a, n, bot)
    where = "attack ##{n}"
    a.element_children.each do |c|
      warn "#{where}: unknown element <#{c.name}> (ignored by hackerbot.rb - typo?)" unless ATTACK_ELEMENTS.include?(c.name)
    end
    err "#{where}: <prompt> missing" if a.at_xpath('prompt').nil? || a.at_xpath('prompt').text.strip.empty?

    get_shell = (a.at_xpath('get_shell') || bot.at_xpath('get_shell'))&.text.to_s.strip
    shell_used = get_shell != 'false'
    conditions_evaluated = shell_used || a.at_xpath('pre_shell') || a.at_xpath('post_shell')

    conds = a.xpath('condition')
    if conditions_evaluated
      if conds.empty?
        err "#{where}: no <condition>; check_output_conditions calls .each on nil after 'ready'"
      elsif conds.size == 1
        err "#{where}: exactly one <condition>; Nori parses it as a Hash so .each yields pairs and crashes. Add a second condition"
      end
    end
    warn "#{where}: no <else_condition>; unmatched output gets no feedback" if conditions_evaluated && a.at_xpath('else_condition').nil?
    if shell_used && a.at_xpath('post_command').nil?
      warn "#{where}: gets a shell but has no <post_command>; conditions only see the shell banner/'shelltest' output"
    end

    conds.each_with_index do |c, ci|
      tests = CONDITION_TESTS.select { |t| c.at_xpath(t) }
      err "#{where} condition #{ci + 1}: needs one of #{CONDITION_TESTS.join('/')}" if tests.empty?
      warn "#{where} condition #{ci + 1}: has #{tests.join(' + ')}; only the first matching test type counts" if tests.size > 1
      err "#{where} condition #{ci + 1}: <message> missing" if c.at_xpath('message').nil?
      %w[output_matches output_not_matches].each do |t|
        next unless (node = c.at_xpath(t))
        begin
          Regexp.new(node.text, Regexp::MULTILINE)
        rescue RegexpError => e
          err "#{where} condition #{ci + 1}: <#{t}> is not a valid Ruby regex: #{e.message}"
        end
      end
    end

    triggers_quiz = a.xpath('condition/trigger_quiz').any?
    quiz = a.at_xpath('quiz')
    if quiz
      %w[question answer correct_answer_response].each do |el|
        err "#{where}: <quiz><#{el}> missing" if quiz.at_xpath(el).nil?
      end
      warn "#{where}: has a <quiz> but no condition has <trigger_quiz/>; the question is never asked" unless triggers_quiz
      ans = quiz.at_xpath('answer')&.text.to_s
      PLACEHOLDER_SOURCES.each do |ph, src|
        next unless ans.include?(ph)
        if src == 'get_shell'
          err "#{where}: quiz uses #{ph} but this attack does not get a shell" unless shell_used
          warn "#{where}: #{ph} crashes the answer handler (nil.map) if the attack was reached without next/goto; prefer {{post_command_output}}"
        elsif a.at_xpath(src).nil?
          err "#{where}: quiz uses #{ph} but the attack has no <#{src}>"
        end
      end
      if ans =~ /\{\{(post_command_output|pre_shell_command_output_first_line)\}\}/ && a.at_xpath('suppress_command_output_feedback').nil?
        warn "#{where}: quiz answer comes from command output but there is no <suppress_command_output_feedback/>; the bot replies 'FYI: <output>', revealing the answer"
      end
      unknown_ph = ans.scan(/\{\{[^}]*\}\}/) - PLACEHOLDER_SOURCES.keys
      err "#{where}: quiz answer has unsupported placeholder(s) #{unknown_ph.join(', ')}" if unknown_ph.any?
      begin
        Regexp.new("^(?:#{ans.gsub(/\{\{[^}]*\}\}/, 'X')})$", Regexp::IGNORECASE)
      rescue RegexpError => e
        err "#{where}: quiz <answer> is used as a regex and does not compile: #{e.message}"
      end
      if ans.include?('{{') && ans !~ /\A\^?\{\{/
        warn "#{where}: answer mixes text with a placeholder; command output is not regex-escaped, so '.' etc. in paths act as wildcards"
      end
    elsif triggers_quiz
      err "#{where}: a condition has <trigger_quiz/> but there is no <quiz>; hackerbot.rb will crash reading quiz['question']"
    end

    %w[post_command pre_shell post_shell get_shell].each do |el|
      txt = a.at_xpath(el)&.text.to_s
      warn "#{where}: <#{el}> contains unrendered ERB" if txt.include?('<%')
    end
  end
end

# -- main --
args = ARGV.dup
overrides = Hash.new { |h, k| h[k] = [] }
flag_count = nil
account_count = 3
xml_path = out_dir = scenario = nil
positional = []
while (a = args.shift)
  case a
  when '--input' then k, v = args.shift.split('=', 2); overrides[k] << v
  when '--flags' then flag_count = args.shift.to_i
  when '--accounts' then account_count = args.shift.to_i
  when '--xml' then xml_path = args.shift
  when '--out' then out_dir = args.shift
  when '--scenario' then scenario = File.expand_path(args.shift)
  when '-h', '--help' then puts File.read(__FILE__)[/^# Usage:.*?(?=^\nrequire)/m]; exit
  else positional << a
  end
end

out_dir ||= File.join(ENV['CLAUDE_JOB_DIR'] ? "#{ENV['CLAUDE_JOB_DIR']}/tmp" : Dir.tmpdir, 'hb_check')
FileUtils.mkdir_p(out_dir)
checker = Checker.new
summary = []

if xml_path
  xml = File.read(xml_path)
  # also accept the generator's JSON output ({"xml_config": ...}) as found in a project's scenario.xml
  xml = JSON.parse(xml)['xml_config'].to_s if xml.lstrip.start_with?('{')
else
  dir = File.expand_path(positional.first || abort('Pass a generator module dir or --xml FILE'))
  inputs, missing, notes = build_inputs(dir, overrides, flag_count, account_count, scenario)
  stdout, stderr, status = run_generator(dir, inputs)
  File.write(File.join(out_dir, 'generator.log'), stderr)

  summary << "Module:   #{dir.sub("#{SECGEN_ROOT}/", '')}"
  summary << "Scenario: #{scenario.sub("#{SECGEN_ROOT}/", '')}" if scenario
  summary << "Inputs:   " + inputs.map { |k, v| "#{k}(#{v.size})" }.join(' ')
  summary << "Unset:    #{missing.join(', ')} (no stub; pass --input NAME=VALUE if the template needs it)" if missing.any?
  notes.each { |n| checker.warn n }

  unless status.success? && !stdout.strip.empty?
    puts summary
    checker.warnings.each { |w| puts "WARNING #{w}" }
    puts "\nERROR   local.rb failed (exit #{status.exitstatus}). Error:"
    errlines = stderr.lines.reject { |l| l.include?('[36m') }
    i = errlines.index { |l| l =~ /Error|\(erb\):\d|undefined|unrecognized|abort|Sorry/ } || 0
    puts errlines[i, 8].map { |l| "  #{l}" }
    puts "  (a crash in (erb):N is line N of the .xml.erb template; NoMethodError on nil usually means an input the template reads was not passed)"
    puts "\nFull log: #{out_dir}/generator.log"
    exit 1
  end

  first = stdout.lines.first.strip
  begin
    payload = JSON.parse(Base64.strict_decode64(first))
  rescue StandardError => e
    puts summary
    puts "\nERROR   output is not base64 JSON (#{e.class}); hackerbot's config.pp parsejson()s each hackerbot_configs value"
    exit 1
  end
  checker.err "output JSON has no 'xml_config' key (hackerbot config.pp reads ['xml_config'])" unless payload.key?('xml_config')
  xml = payload['xml_config'].to_s

  if stderr.include?('Not enough flags')
    checker.warn "template padded flags with random ones (REQUIRED_FLAGS > #{inputs['flags']&.size} flags supplied); padded flags are not tracked by SecGen. Add flag_generator default_inputs or pass more flags in the scenario"
  end
  if inputs['flags']
    placed = inputs['flags'].count { |f| xml.include?(f) }
    summary << "Flags:    #{placed}/#{inputs['flags'].size} supplied flags appear in the XML"
    checker.warn "#{inputs['flags'].size - placed} supplied flag(s) never placed (REQUIRED_FLAGS/pop count lower than flags supplied)" if placed < inputs['flags'].size
  end
end

doc = checker.check(xml)
File.write(File.join(out_dir, 'bot.xml'), xml)

attacks = doc.xpath('//attack')
summary << "Bot:      #{doc.at_xpath('/hackerbot/name')&.text}  (#{attacks.size} attacks, #{attacks.count { |a| a.at_xpath('quiz') }} with a quiz)"
summary << "Output:   #{out_dir}/bot.xml" + (xml_path ? '' : ", #{out_dir}/generator.log")
puts summary
puts
checker.errors.each { |e| puts "ERROR   #{e}" }
checker.warnings.each { |w| puts "WARNING #{w}" }
puts "\n#{checker.errors.size} error(s), #{checker.warnings.size} warning(s)"
exit(checker.errors.empty? ? 0 : 1)
