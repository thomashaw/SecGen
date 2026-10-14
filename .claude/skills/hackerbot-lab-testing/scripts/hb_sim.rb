#!/usr/bin/env ruby
# Replays a Hackerbot attack's <condition> matching offline, exactly as
# hackerbot.rb's check_output_conditions does (unanchored /regex/m, in order,
# first match wins, else_condition otherwise).
#
# Usage:
#   ruby hb_sim.rb --xml bot.xml --list
#   ruby hb_sim.rb --xml bot.xml --attack 8 --output 'some text\n1112'
#   ruby hb_sim.rb --xml bot.xml --attack 8 --root /tmp/fakefs
#
# --output  : literal post_command output (\n is turned into a newline)
# --root    : run the attack's real post_command (or, if it has none, the first base64 script shipped
#             in its pre_shell) with bash, rewriting "/home/" to "<root>/home/", stderr merged into
#             stdout (like popen2e). Use a --root path with no digits in it.
#             Only meaningful for read-only check commands (not sudo -u ...).
#
# Get bot.xml from a build:      ruby .claude/skills/secgen-hackerbot/scripts/hb_check.rb \
#                                  modules/generators/structured_content/hackerbot_config/backups --out OUTDIR
# or from a deployed server:     /opt/hackerbot/config/bot_0.xml on hackerbot_server
require 'nokogiri'
require 'optparse'
require 'open3'
require 'base64'

opts = {}
OptionParser.new do |o|
  o.on('--xml FILE') { |v| opts[:xml] = v }
  o.on('--attack N', Integer) { |v| opts[:attack] = v }
  o.on('--output STR') { |v| opts[:output] = v.gsub('\n', "\n") }
  o.on('--root DIR') { |v| opts[:root] = File.expand_path(v) }
  o.on('--list') { opts[:list] = true }
end.parse!
abort 'need --xml' unless opts[:xml]

doc = Nokogiri::XML(File.read(opts[:xml])) { |c| c.recover }
attacks = doc.xpath('//hackerbot/attack')

if opts[:list]
  attacks.each_with_index do |a, i|
    puts "##{i + 1}: #{a.at_xpath('prompt')&.text.to_s.strip[0, 110]}"
  end
  exit
end

n = opts[:attack] or abort 'need --attack N (1-based, as the bot shows it)'
attack = attacks[n - 1] or abort "no attack ##{n}"
cmd = attack.at_xpath('post_command')&.text.to_s
# labs that check from the hackerbot_server ship the check as base64 inside pre_shell (get_shell false)
if cmd.strip.empty? && (b64 = attack.at_xpath('pre_shell')&.text.to_s[%r{echo ([A-Za-z0-9+/=]{20,}) \| base64 -d}, 1])
  cmd = Base64.decode64(b64)
end

lines =
  if opts.key?(:output)
    opts[:output]
  elsif opts[:root]
    out, _st = Open3.capture2e('bash', '-c', cmd.gsub('/home/', "#{opts[:root]}/home/"))
    out.chomp
  else
    abort 'need --output or --root'
  end

puts "== attack ##{n} post_command:\n#{cmd}\n\n== output (what the bot shows as FYI):\n#{lines}\n\n"
attack.xpath('condition').each_with_index do |c, i|
  hit = false
  if (re = c.at_xpath('output_matches'))
    hit = lines =~ /#{re.text}/m
  end
  if !hit && (re = c.at_xpath('output_not_matches'))
    hit = lines !~ /#{re.text}/m
  end
  if !hit && (eq = c.at_xpath('output_equals'))
    hit = lines.chomp == eq.text
  end
  next unless hit
  rule = c.at_xpath('output_matches|output_not_matches|output_equals')
  trig = %w[trigger_next_attack trigger_quiz].select { |t| c.at_xpath(t) }
  puts "== MATCHED condition #{i + 1}  <#{rule.name}>#{rule.text}</#{rule.name}>"
  puts "   bot says: #{c.at_xpath('message')&.text}"
  puts "   triggers: #{trig.empty? ? '(none - stays on this attack)' : trig.join(', ')}"
  exit
end
puts "== no condition matched -> else_condition"
puts "   bot says: #{attack.at_xpath('else_condition/message')&.text}"
