require_relative '../../../../../lib/post_provision_test'

# ovirt_agents installs the oVirt guest agent and SPICE vdagent and enables both services.
# On Proxmox the ovirt-guest-agent daemon cannot reach its oVirt virtio channel, so
# only the packages, their files and the unit definitions are checked, not "running".
class OvirtAgentsTest < PostProvisionTest
  def initialize
    self.module_name = 'ovirt_agents'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_local_command('ovirt-guest-agent installed',
                         'dpkg -l ovirt-guest-agent | grep -E "^ii"',
                         'ovirt-guest-agent')
      test_local_command('spice-vdagent installed',
                         'dpkg -l spice-vdagent | grep -E "^ii"',
                         'spice-vdagent')
      test_local_command('spice-vdagent binaries shipped', 'dpkg -L spice-vdagent', 'bin/spice-vdagent')
      test_local_command('ovirt-guest-agent daemon shipped', 'dpkg -L ovirt-guest-agent', 'ovirt-guest-agent.py')
    end
    tier(2) do
      # the agents themselves can't run without a hypervisor channel, but the binary starts and prints usage
      test_local_command('spice-vdagent runs', 'spice-vdagent --help 2>&1', 'spice-vdagent')
      test_local_command('spice-vdagentd runs', 'spice-vdagentd --help 2>&1', 'spice-vdagentd')
      # Puppet enables both services, so systemd must know the units
      test_local_command('ovirt-guest-agent unit known', 'systemctl cat ovirt-guest-agent 2>&1', 'ovirt-guest-agent')
    end
  end
end

OvirtAgentsTest.new.run
