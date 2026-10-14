# Body of scripts/guest-exec (run via that wrapper, which sets SECGEN_DIR, SECGEN_CONF and bundler).
require 'json'
require File.join(ENV.fetch('SECGEN_DIR'), 'lib/helpers/proxmox_connection')

vm_id, cmd, timeout = ARGV
abort 'usage: guest-exec <vmid> <command> [timeout]' unless vm_id && cmd
vm_id = vm_id.split('/').last # accept "pmox01/6817491" straight from the .vagrant id file
conf = File.read(ENV.fetch('SECGEN_CONF')).split.each_slice(2).to_h
conn = Proxmox::Connection.new(conf['--proxmox-url'])
conn.login(username: conf['--proxmoxuser'], password: conf['--proxmoxpass'])
node = (conn.get_vm_info(vm_id) || {})[:node] || conf['--proxmox-node']
r = conn.exec_qemu_guest(vm_id, node, ['sh', '-c', cmd], timeout: (timeout || 60).to_i)
print r[:stdout]
$stderr.print r[:stderr]
$stderr.puts "[exit #{r[:exitcode]}#{r[:truncated] ? ', output TRUNCATED' : ''}]"
exit(r[:exitcode].to_i)
