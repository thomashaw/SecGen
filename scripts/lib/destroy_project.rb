# Body of scripts/secgen-destroy (run via that wrapper, which sets SECGEN_DIR,
# SECGEN_CONF and the bundler environment). Credentials are read from the
# options file and never printed.
require 'fileutils'
require File.join(ENV.fetch('SECGEN_DIR'), 'lib/helpers/proxmox')

keep_project = !ARGV.delete('--keep-project').nil?
abort 'Usage: scripts/secgen-destroy [--keep-project] <projects/<id>>...' if ARGV.empty?

conf = File.read(ENV.fetch('SECGEN_CONF')).split.each_slice(2).to_h
options = { proxmoxurl: conf['--proxmox-url'], proxmoxuser: conf['--proxmoxuser'], proxmoxpass: conf['--proxmoxpass'] }

status = 0
ARGV.each do |project|
  project = File.expand_path(project)
  unless File.directory?("#{project}/.vagrant/machines")
    warn "#{project}: no .vagrant/machines (not a built project?); skipping"
    status = 1
    next
  end
  vm_names = Dir.children("#{project}/.vagrant/machines").sort
  puts "#{File.basename(project)}: #{ProxmoxFunctions.vm_ids(project, vm_names).map { |n, node, id| "#{n}=#{node}/#{id}" }.join(' ')}"
  remaining = ProxmoxFunctions.destroy_vms(project, vm_names, options)
  if remaining.empty?
    puts "  all VMs gone"
    unless keep_project
      FileUtils.rm_rf(project)
      puts "  removed #{project}"
    end
  else
    warn "  still present: #{remaining.join(', ')}; keeping #{project}"
    status = 1
  end
end
exit status
