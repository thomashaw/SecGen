require_relative '../../../../../lib/post_provision_test'

# ovirt_agents installs the oVirt guest agent and SPICE vdagent.
# On Proxmox the ovirt-guest-agent service will not start (oVirt-specific virt channel),
# but the packages should be present.
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
    end
    tier(2) do
      # ovirt-guest-agent requires oVirt hypervisor channel; skip service check
      skip_check('ovirt-guest-agent service running',
                 'oVirt guest agent requires oVirt hypervisor — not applicable on Proxmox',
                 tier: 2, evidence: nil)
    end
  end
end

OvirtAgentsTest.new.run
