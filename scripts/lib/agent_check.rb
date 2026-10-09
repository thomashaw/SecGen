# Body of scripts/agent-check (run via that wrapper, which sets SECGEN_DIR,
# SECGEN_CONF and the bundler environment).
require 'json'
require File.join(ENV.fetch('SECGEN_DIR'), 'lib/helpers/proxmox_connection')

vm_id = ARGV[0] or abort 'Usage: scripts/agent-check <vmid> [node]'
conf = File.read(ENV.fetch('SECGEN_CONF')).split.each_slice(2).to_h
node = ARGV[1] || conf['--proxmox-node'] or abort 'No node given and no --proxmox-node in config'

def show(label, value)
  puts format('  %-28s %s', label, value)
end

puts "SecGen dir: #{ENV['SECGEN_DIR']}"
puts "VM #{vm_id} on #{node}"
conn = Proxmox::Connection.new(conf['--proxmox-url'])
conn.login(username: conf['--proxmoxuser'], password: conf['--proxmoxpass'])

show 'agent option enabled?', conn.qemu_agent_enabled?(vm_id, node)
running = conn.qemu_agent_running?(vm_id, node)
show 'agent answering ping?', running
exit 1 unless running

[
  ['id'],
  ['hostname'],
  ['sh', '-c', 'ip -4 -o addr show scope global | awk "{print \$2, \$4}"'],
  ['sh', '-c', 'echo to-stdout; echo to-stderr >&2; exit 3'],
].each do |cmd|
  started = Process.clock_gettime(Process::CLOCK_MONOTONIC)
  begin
    r = conn.exec_qemu_guest(vm_id, node, cmd, timeout: 15)
    secs = Process.clock_gettime(Process::CLOCK_MONOTONIC) - started
    puts "  $ #{cmd.join(' ')}   (exit #{r[:exitcode]}, #{format('%.1f', secs)}s#{r[:truncated] ? ', TRUNCATED' : ''})"
    r[:stdout].each_line { |l| puts "      out: #{l}" }
    r[:stderr].each_line { |l| puts "      err: #{l}" }
  rescue => e
    puts "  $ #{cmd.join(' ')}   ERROR #{e.class}: #{e.message}"
  end
end

show 'qemu_agent_get_ip', conn.qemu_agent_get_ip(vm_id).inspect
