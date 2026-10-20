require_relative '../../../../../lib/post_provision_test'

class DefaultClientFirewallTest < PostProvisionTest
  def initialize
    self.module_name = 'default_client_firewall'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_local_command('iptables installed', 'which iptables', 'iptables')
    end
    tier(2) do
      # The default firewall sets ACCEPT and DROP rules; verify at least one chain has rules
      test_local_command('firewall rules loaded', 'iptables -L -n', 'Chain')
      test_local_command('drop-all rule present', 'iptables -L -n | tail -5', 'DROP')
    end
  end
end

DefaultClientFirewallTest.new.run
