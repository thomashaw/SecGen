require 'fileutils'
require_relative 'gem_exec'
require_relative 'proxmox'
require_relative 'print'

# Destroys a project's VMs. Proxmox VMs are hard-stopped and deleted through
# the API (whatever state vagrant thinks they are in, e.g. after net0 teardown);
# other providers use `vagrant destroy -f`.
class DestroyFunctions

  # Names of the VMs vagrant has state for in project_dir.
  def self.project_vm_names(project_dir)
    machines = "#{project_dir}/.vagrant/machines"
    File.directory?(machines) ? Dir.children(machines).sort : []
  end

  # Was project_dir built on Proxmox? (vagrant-proxmox writes machines/<vm>/proxmox/id)
  def self.proxmox_project?(project_dir)
    !Dir.glob("#{project_dir}/.vagrant/machines/*/proxmox/id").empty?
  end

  # Destroys vm_names (default: all of the project's VMs). Returns the names of
  # VMs that may still exist.
  def self.destroy_vms(project_dir, options, vm_names = nil)
    vm_names ||= project_vm_names(project_dir)
    if ProxmoxFunctions.provider_proxmox?(options)
      remaining = ProxmoxFunctions.destroy_vms(project_dir, vm_names, options)
      # Like `vagrant destroy`, forget the machine id once the VM is gone.
      (vm_names - remaining).each do |vm_name|
        FileUtils.rm_f("#{project_dir}/.vagrant/machines/#{vm_name}/proxmox/id")
      end
      remaining
    else
      args = vm_names.empty? ? 'destroy -f' : "destroy #{vm_names.join(' ')} -f"
      GemExec.exe('vagrant', project_dir, args)[:status] == 0 ? [] : vm_names
    end
  end

  # Destroys all of the project's VMs, then removes project_dir unless
  # remove_project is false. The directory is kept if any VM may still exist,
  # since it holds their ids. Returns the names of VMs that may still exist.
  def self.destroy_project(project_dir, options, remove_project: true)
    remaining = destroy_vms(project_dir, options)
    if !remaining.empty?
      Print.err "VMs not destroyed: #{remaining.join(', ')}. Keeping #{project_dir} for manual cleanup."
    elsif remove_project
      FileUtils.rm_rf(project_dir)
      Print.info "Removed #{project_dir}"
    end
    remaining
  end
end
