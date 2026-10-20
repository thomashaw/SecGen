require_relative '../../../../../lib/post_provision_test'

# The 'firewall' module is the upstream puppetlabs/firewall Puppet module that
# provides the firewall resource type used by other SecGen modules. Its only
# observable effect on the system is ensuring iptables is present and loaded.
class FirewallTest < PostProvisionTest
  def initialize
    self.module_name = 'firewall'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_local_command('iptables installed', 'which iptables', 'iptables')
    end
    tier(2) do
      test_command_succeeds('iptables runs', 'iptables --version')
    end
  end
end

FirewallTest.new.run
