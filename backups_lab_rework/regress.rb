#!/usr/bin/env ruby
# Offline regression tests for the backups lab bot (ROADMAP F3).
#
# Builds fake desktop/backup_server directory trees for each scenario (correct answer and typical student
# mistakes), runs the bot's real check scripts against them (decoded from each attack's base64 pre_shell,
# with /home/ rewritten into a temp dir), then evaluates the attack's <condition>s exactly like
# hackerbot.rb#check_output_conditions, and asserts which reply the student would get.
#
#   ruby .claude/skills/secgen-hackerbot/scripts/hb_check.rb modules/generators/structured_content/hackerbot_config/backups \
#     --scenario scenarios/labs/response_and_investigation/3_backups_and_recovery.xml --accounts 2 --out /tmp/hbout
#   ruby backups_lab_rework/regress.rb /tmp/hbout/bot.xml
require 'nokogiri'
require 'base64'
require 'open3'
require 'tmpdir'
require 'fileutils'

xml = ARGV[0] or abort 'usage: regress.rb bot.xml'
doc = Nokogiri::XML(File.read(xml)) { |c| c.recover }
ATTACKS = doc.xpath('//hackerbot/attack').to_a

def scripts(n)
  ATTACKS[n - 1].at_xpath('pre_shell').text.scan(/echo ([A-Za-z0-9+\/=]{20,}) \| base64 -d/).flatten.map { |b| Base64.decode64(b) }
end

p2 = ATTACKS[1].at_xpath('prompt').text
U = p2[%r{:/home/([^/]+)/remote-rsync-full-backup}, 1]
S = p2[/backups for (\S+) \(a user/, 1]
BIN = ATTACKS[0].at_xpath('prompt').text[/(remote-bin-backup-[0-9a-f]+)/, 1]
ORIGINALS = scripts(2)[0].scan(%r{\[ -e /home/#{U}/remote-rsync-full-backup/#{S}/(\S+) \]}).flatten
STEP_FILE = {}
[[4, 3], [6, 5], [8, 7], [10, 9]].each do |att, step|
  # the "changes-from-step-N" line names the marker file in the check that expects it present
  s = scripts(att)[0]
  m = s.match(%r{if \[ -e /home/#{U}/remote-rsync-\w+/#{S}/(\S+) \]; then echo 'OK changes-from-step-#{step}'})
  STEP_FILE[step] = m[1]
end

$root = Dir.mktmpdir('hbreg')   # no digits in this path, so nothing random leaks into matching
$root = File.join('/tmp', 'hbregress')
FileUtils.rm_rf($root)

def run(script)
  out, _ = Open3.capture2e('bash', '-c', script.gsub('/home/', "#{$root}/home/"))
  out
end

def verdict(n, out)
  ATTACKS[n - 1].xpath('condition').each do |c|
    re = c.at_xpath('output_matches')&.text
    return c.at_xpath('message').text if re && out =~ /#{re}/m
  end
  'ELSE: ' + ATTACKS[n - 1].at_xpath('else_condition/message').text
end

def fresh
  FileUtils.rm_rf($root)
  home = "#{$root}/home/#{S}"
  ORIGINALS.each { |f| FileUtils.mkdir_p(File.dirname("#{home}/#{f}")); File.write("#{home}/#{f}", "orig #{f}\n") }
  FileUtils.mkdir_p("#{$root}/home/#{U}")
end

def stage(step)
  run(scripts(step)[0])   # the stage script (2nd ssh in the pre_shell is the stage; tar part needs the server)
end

def stage_script(step)
  # attacks 3/5/7/9: the decoded script is the stage script (the tar stash/restore isn't base64)
  scripts(step).last
end

def apply(step)
  run(stage_script(step))
end

# copy SECONDUSER's home (or a subset of it) into a backup dir, as rsync would for /home/S -> dest/
def backup(dest, only: nil, layout: :normal)
  src = "#{$root}/home/#{S}"
  base = "#{$root}/home/#{U}/#{dest}"
  target = case layout
           when :normal then "#{base}/#{S}"
           when :contents then base
           when :nested then "#{base}/#{S}/#{S}"
           end
  FileUtils.mkdir_p(target)
  Dir.glob('**/*', File::FNM_DOTMATCH, base: src).each do |rel|
    next if rel.end_with?('.')
    p = "#{src}/#{rel}"
    if File.directory?(p)
      FileUtils.mkdir_p("#{target}/#{rel}")
    elsif only.nil? || only.include?(rel)
      FileUtils.mkdir_p(File.dirname("#{target}/#{rel}"))
      FileUtils.cp(p, "#{target}/#{rel}")
    end
  end
end

def steps(*ks)
  ks.map { |k| STEP_FILE[k] } + (ks.empty? ? [] : ['notes', 'logs/log2'])
end

$pass = $fail = 0
def expect(name, n, want)
  out = run(scripts(n)[0])
  v = verdict(n, out)
  ok = v.include?(want)
  ok ? $pass += 1 : $fail += 1
  puts "#{ok ? 'PASS' : 'FAIL'}  ##{n} #{name}\n      -> #{v[0, 150]}"
  puts "      want: #{want}\n      output: #{out.gsub("\n", ' | ')[0, 300]}" unless ok
end

puts "U=#{U} S=#{S} originals=#{ORIGINALS.size} step files=#{STEP_FILE}"

# ---- attack 1
fresh; expect('nothing copied', 1, "There's no")
fresh; FileUtils.mkdir_p("#{$root}/home/#{U}/#{BIN}"); FileUtils.touch(%W[#{$root}/home/#{U}/#{BIN}/ls #{$root}/home/#{U}/#{BIN}/mkdir])
expect('contents without bin/', 1, 'contents* of bin')
fresh; FileUtils.mkdir_p("#{$root}/home/#{U}/#{BIN}/bin"); FileUtils.touch(%W[#{$root}/home/#{U}/#{BIN}/bin/ls #{$root}/home/#{U}/#{BIN}/bin/mkdir])
expect('correct', 1, 'Well done')

# ---- attack 2
fresh; expect('no backup', 2, "can't find")
fresh; backup('remote-rsync-full-backup'); expect('correct', 2, 'Well done')
fresh; backup('remote-rsync-full-backup', layout: :contents); expect('contents at top', 2, 'contents* of')
fresh; backup('remote-rsync-full-backup', layout: :nested); expect('nested', 2, 'nested one level')
fresh; backup('remote-rsync-full-backup', only: ORIGINALS[0..1]); expect('missing files (no sudo)', 2, 'sudo')

# ---- attack 3 stage + attack 4
fresh; backup('remote-rsync-full-backup'); out = apply(3)
ok = out.include?('STAGE-3-APPLIED') && File.exist?("#{$root}/home/#{S}/#{STEP_FILE[3]}")
ok ? $pass += 1 : $fail += 1
puts "#{ok ? 'PASS' : 'FAIL'}  #3 stage script applies (#{out.lines.grep(/STAGE/).join.strip})"
base_full = -> { fresh; backup('remote-rsync-full-backup'); apply(3) }
base_full.call; expect('no differential yet', 4, "can't find")
base_full.call; backup('remote-rsync-differential1', only: steps(3)); expect('correct', 4, 'Well done')
base_full.call; backup('remote-rsync-differential1'); expect('full copy (no/wrong --compare-dest)', 4, '--compare-dest')
fresh; backup('remote-rsync-full-backup'); backup('remote-rsync-differential1', only: []); apply(3)
expect('taken before step 3', 4, "goto 3")

# ---- attack 5 stage + attack 6
upto5 = -> { fresh; backup('remote-rsync-full-backup'); apply(3); backup('remote-rsync-differential1', only: steps(3)); apply(5) }
upto5.call; backup('remote-rsync-differential2', only: steps(3, 5)); expect('correct', 6, 'Well done')
upto5.call; backup('remote-rsync-differential2', only: steps(3, 5), layout: :contents); expect('prompt path taken literally (contents)', 6, 'contents* of')
upto5.call; backup('remote-rsync-differential2', only: steps(5)); expect('incremental instead of differential', 6, "step 3's")
upto5.call; apply(7); backup('remote-rsync-differential2', only: steps(3, 5, 7)); expect('taken too late', 6, "goto 5")
upto5.call; backup('remote-rsync-differential2'); expect('full copy', 6, '--compare-dest')

# ---- attack 7 stage + attack 8
upto7 = lambda do
  upto5.call; backup('remote-rsync-differential2', only: steps(3, 5)); apply(7)
end
upto7.call; backup('remote-rsync-incremental1', only: steps(7)); expect('correct', 8, 'Well done')
upto7.call; backup('remote-rsync-incremental1', only: steps(5, 7)); expect('forgot diff2 --compare-dest (B4)', 8, 'second --compare-dest')
upto7.call; apply(9); backup('remote-rsync-incremental1', only: steps(7, 9)); expect('taken too late', 8, "goto 7")
upto7.call; backup('remote-rsync-incremental1', only: []); expect('taken before step 7', 8, "step 7")

# ---- attack 9 stage + attack 10
upto9 = lambda do
  upto7.call; backup('remote-rsync-incremental1', only: steps(7)); apply(9)
end
upto9.call; backup('remote-rsync-incremental2', only: steps(9)); expect('correct', 10, 'Well done')
upto9.call; backup('remote-rsync-incremental2', only: steps(7, 9)); expect('forgot incr1 --compare-dest', 10, 'incremental1')
upto9.call; backup('remote-rsync-incremental2', only: []); expect('incr1 retaken late -> empty incr2 (B5)', 10, 'redone after step 9')
quiz = ATTACKS[9].at_xpath('quiz/answer').text
eggs = File.read("#{$root}/home/#{U}/remote-rsync-incremental1/#{S}/notes").strip
qre = /^(?:#{quiz})$/i                 # how hackerbot.rb matches answers
code = eggs.split.last
qok = eggs.match?(qre) && code.match?(qre) && code.match?(/\A\h{8}\z/)
qok ? $pass += 1 : $fail += 1
puts "#{qok ? 'PASS' : 'FAIL'}  #10 quiz: incremental1 notes (#{eggs.inspect}) and its 8-hex code are accepted"
# nothing still on the desktop (any file name or line in SECONDUSER's home) may answer it
leaks = Dir.glob("#{$root}/home/#{S}/**/*", File::FNM_DOTMATCH).flat_map do |p|
  [File.basename(p)] + (File.file?(p) ? File.read(p).lines.map(&:strip) : [])
end.select { |s| s.match?(qre) }
leaks.empty? ? $pass += 1 : $fail += 1
puts "#{leaks.empty? ? 'PASS' : 'FAIL'}  #10 quiz: no file name or content on the desktop answers it#{leaks.empty? ? '' : ': ' + leaks.inspect}"

# ---- attack 11 gate (backup side only)
upto9.call; backup('remote-rsync-incremental2', only: steps(9))
g = run(scripts(11)[0]); ok = g.include?('GATE-PASS'); ok ? $pass += 1 : $fail += 1
puts "#{ok ? 'PASS' : 'FAIL'}  #11 gate passes with good backups"
upto9.call; backup('remote-rsync-incremental2', only: steps(7, 9))
g = run(scripts(11)[0]); ok = g.include?('GATE-FAIL') && g.include?('[incremental2] UNEXPECTED changes-from-step-7')
ok ? $pass += 1 : $fail += 1
puts "#{ok ? 'PASS' : 'FAIL'}  #11 gate refuses with a bad incremental2 (#{g.lines.grep(/incremental2\] (UNEXPECTED|MISSING)/).join.strip})"

# ---- attack 12 restore (desktop side)
restored = lambda do
  upto9.call   # desktop home now has originals + all steps + step-9 notes == a perfect restore
end
restored.call; expect('perfect restore', 12, 'Well done')
restored.call; apply(7); FileUtils.cp("#{$root}/home/#{S}/notes", '/tmp/hbregress_notes'); apply(9)
FileUtils.cp('/tmp/hbregress_notes', "#{$root}/home/#{S}/notes"); expect('wrong order (stale notes)', 12, 'Close')
restored.call; FileUtils.rm_f("#{$root}/home/#{S}/#{STEP_FILE[7]}"); expect('incremental1 not applied', 12, 'incremental1')
restored.call; FileUtils.rm_f(ORIGINALS.map { |f| "#{$root}/home/#{S}/#{f}" }); expect('full not applied', 12, 'full backup')
restored.call; FileUtils.mkdir_p("#{$root}/home/#{S}/#{S}"); expect('nested restore', 12, "/home/#{S}/#{S}/")
restored.call; FileUtils.rm_rf(Dir.glob("#{$root}/home/#{S}/*")); expect('nothing restored (B3)', 12, 'full backup')

# ---- skipping ahead: each backup first checks the backups it is based on exist
fresh; apply(3); backup('remote-rsync-differential1', only: steps(3)); expect('skipped the full backup', 4, "goto 2")
fresh; backup('remote-rsync-full-backup'); apply(3); apply(5); backup('remote-rsync-differential1', only: steps(3, 5))
expect('differential1 taken too late (holds step 5)', 4, "goto 3")
fresh; backup('remote-rsync-full-backup'); apply(3); apply(5); apply(7); backup('remote-rsync-incremental1', only: steps(3, 5, 7))
expect('skipped differential2', 8, "goto 5")
upto9.call; FileUtils.rm_rf("#{$root}/home/#{U}/remote-rsync-incremental1"); backup('remote-rsync-incremental2', only: steps(7, 9))
expect('skipped incremental1', 10, "goto 7")
upto9.call; backup('remote-rsync-incremental2', only: steps(9)); FileUtils.rm_rf("#{$root}/home/#{U}/remote-rsync-differential1")
g = run(scripts(11)[0]); ok = g.include?('GATE-FAIL') && g.include?('[differential1] NODIR')
ok ? $pass += 1 : $fail += 1
puts "#{ok ? 'PASS' : 'FAIL'}  #11 gate refuses without differential1 (attack 13 needs it)"

# ---- every pre_shell must parse in the shell the bot really uses: Ruby backticks run /bin/sh, which is dash on the
# Kali hackerbot_server (macOS's sh is bash, which is more forgiving - e.g. it accepted "$((" where dash didn't)
SH = system('command -v dash >/dev/null 2>&1') ? 'dash' : 'sh'
ATTACKS.each_with_index do |a, i|
  pre = a.at_xpath('pre_shell')&.text or next
  _, err, st = Open3.capture3(SH, '-n', '-c', pre)
  ok = st.success?
  ok ? $pass += 1 : $fail += 1
  puts "#{ok ? 'PASS' : 'FAIL'}  ##{i + 1} pre_shell parses with #{SH}#{ok ? '' : ': ' + err.strip}"
end

# ---- unreachable VM: run the real pre_shell (as the bot does, with sh) with ssh swapped for a failing one
def unreachable(n)
  pre = ATTACKS[n - 1].at_xpath('pre_shell').text
  fake = %q{sh -c 'cat >/dev/null; echo "ssh: connect to host 10.0.0.3 port 22: No route to host" >&2; exit 255'}
  pre = pre.gsub(%r{ssh -i /opt/hackerbot/keys/id_rsa -oStrictHostKeyChecking=no -oBatchMode=yes root@\S+( bash -s)?}, fake)
  out, _ = Open3.capture2e(SH, '-c', pre)
  out
end
[[1, "couldn't connect to the backup_server"], [4, "couldn't connect to the backup_server"],
 [3, "couldn't connect to your desktop"], [12, "couldn't connect to your desktop"],
 [11, "couldn't connect to the backup_server"]].each do |n, want|
  v = verdict(n, unreachable(n))
  ok = v.include?(want)
  ok ? $pass += 1 : $fail += 1
  puts "#{ok ? 'PASS' : 'FAIL'}  ##{n} VM unreachable\n      -> #{v[0, 150]}"
end

# ---- attack 13
[[3, 'Well done'], [5, 'differential2'], [7, 'later version'], [9, 'later version']].each do |k, want|
  fresh; apply(k); expect("notes from step #{k}", 13, want)
end

puts "\n#{$pass} passed, #{$fail} failed"
exit($fail.zero? ? 0 : 1)
