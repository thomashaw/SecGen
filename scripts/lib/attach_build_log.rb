# Copies a secgen-run build log into test_results/<project-id>/build.log with the
# Proxmox password masked (read from SECGEN_PROXMOX_PASS, never a command-line
# argument), and records it in that run's summary.json.
#
# Usage: SECGEN_PROXMOX_PASS=... ruby attach_build_log.rb <secgen dir> <log file> <project id>
require 'json'
require 'fileutils'

secgen_dir, log, project_id = ARGV
abort 'Usage: attach_build_log.rb <secgen dir> <log file> <project id>' unless project_id && File.file?(log.to_s)
out = File.join(secgen_dir, 'test_results', project_id)
summary_path = File.join(out, 'summary.json')
exit 0 unless File.exist?(summary_path) # not a test run

data = File.binread(log)
secret = ENV['SECGEN_PROXMOX_PASS'].to_s.b
count = secret.empty? ? 0 : data.scan(secret).size
data = data.gsub(secret, '********') if count > 0
File.binwrite(File.join(out, 'build.log'), data)

summary = JSON.parse(File.read(summary_path))
summary['build'] ||= {}
summary['build']['log'] = 'build.log'
summary['masked_secrets'] = summary['masked_secrets'].to_i + count
File.write(summary_path, JSON.pretty_generate(summary) + "\n")
warn "Masked #{count} occurrence(s) of the Proxmox password in build.log" if count > 0
puts "Build log: #{File.join('test_results', project_id, 'build.log')}"
