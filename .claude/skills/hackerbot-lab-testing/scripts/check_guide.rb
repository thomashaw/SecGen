#!/usr/bin/env ruby
# Check a manual test guide (markdown) against the lab:
#   - every ```bash block passes bash -n (after substituting placeholders);
#   - every phrase you expect the bot to say exists verbatim in the bot template.
#
#   ruby check_guide.rb GUIDE.md lab.xml.erb [phrases.txt] [--sub YOURUSER=alice --sub ...]
#
# phrases.txt: one expected bot phrase per line (copy the distinctive part of each "Expect:" quote;
# leave out anything the template fills in, like usernames). Blank lines and #comments are ignored.
# Exit 1 on any problem.
require 'open3'

args = ARGV.dup
subs = {}
while (i = args.index('--sub'))
  k, v = args.delete_at(i + 1).split('=', 2)
  args.delete_at(i)
  subs[k] = v
end
guide, tpl, phrases_file = args
abort 'usage: check_guide.rb GUIDE.md lab.xml.erb [phrases.txt] [--sub KEY=VAL ...]' unless guide && tpl
md = File.read(guide)
template = File.read(tpl)
bad = 0

blocks = md.scan(/```(?:bash|sh)\n(.*?)```/m).flatten.map { |b| b.gsub(/^ {2,4}/, '') }
blocks.each_with_index do |b, i|
  subs.each { |k, v| b = b.gsub(k, v) }
  _, err, st = Open3.capture3('bash', '-n', '-c', b)
  next if st.success?
  bad += 1
  puts "block #{i}: #{err.strip}\n#{b.lines.first(3).join}"
end

phrases = phrases_file ? File.readlines(phrases_file).map(&:strip).reject { |l| l.empty? || l.start_with?('#') } : []
phrases.each do |p|
  next if template.include?(p)
  bad += 1
  puts "phrase not in the bot template: #{p}"
end
puts "#{blocks.size} command blocks, #{phrases.size} expected phrases, #{bad} problem(s)"
exit(bad.zero? ? 0 : 1)
