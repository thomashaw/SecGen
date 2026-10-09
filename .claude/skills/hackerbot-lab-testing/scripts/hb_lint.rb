#!/usr/bin/env ruby
# Lint the shell in a rendered bot.xml the way the real bot will run it.
#
#   ruby hb_lint.rb bot.xml [attack_number_to_print]
#
# - Every pre_shell / post_command / post_shell is syntax-checked with dash (sh -n): hackerbot.rb runs
#   pre_shell/post_shell with Ruby backticks, i.e. /bin/sh, which is dash on the Kali hackerbot_server.
#   macOS's sh is bash and accepts things dash rejects (e.g. "$((" from "$( (" written without a space).
# - Any "echo <base64> | base64 -d" payload (scripts shipped to a VM and run with bash -s) is decoded
#   and checked with bash -n.
# - Flags found inside decoded payloads are listed: hb_check can't see them and reports "supplied flag
#   never placed" - this tells you whether that warning is a false positive.
# Exit 1 on any syntax error. Needs nokogiri (run with the repo's bundle).
require 'nokogiri'
require 'base64'
require 'open3'

xml = ARGV[0] or abort 'usage: hb_lint.rb bot.xml [attack_to_print]'
show = ARGV[1]&.to_i
dash = system('command -v dash >/dev/null 2>&1') ? 'dash' : 'sh'
warn "note: no dash here - checking with #{dash}, which may be more lenient than the bot server" unless dash == 'dash'
doc = Nokogiri::XML(File.read(xml)) { |c| c.recover }
bad = 0
hidden_flags = []
doc.xpath('//hackerbot/attack').each_with_index do |a, i|
  %w[pre_shell post_command post_shell].each do |el|
    cmd = a.at_xpath(el)&.text or next
    shell = el == 'post_command' ? 'bash' : dash # post_command is piped into the get_shell (bash on the target)
    _, err, st = Open3.capture3(shell, '-n', '-c', cmd)
    unless st.success?
      bad += 1
      puts "##{i + 1} #{el} (#{shell} -n): #{err.strip}"
    end
    cmd.scan(%r{echo ([A-Za-z0-9+/=]{20,}) \| base64 -d}).flatten.each_with_index do |b, j|
      script = Base64.decode64(b)
      _, err, st = Open3.capture3('bash', '-n', '-c', script)
      unless st.success?
        bad += 1
        puts "##{i + 1} #{el} payload #{j} (bash -n): #{err.strip}"
      end
      script.scan(/flag\\?\{[^}\\]*\\?\}/).each { |f| hidden_flags << "##{i + 1}: #{f.delete('\\')}" }
      puts "----- ##{i + 1} #{el} payload #{j} -----\n#{script}" if show == i + 1
    end
  end
end
puts "flags inside base64 payloads (invisible to hb_check): #{hidden_flags.empty? ? 'none' : hidden_flags.join(', ')}"
puts "#{bad} syntax problem(s) (outer commands checked with #{dash})"
exit(bad.zero? ? 0 : 1)
