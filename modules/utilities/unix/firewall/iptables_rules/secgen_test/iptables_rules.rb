require_relative '../../../../../lib/post_provision_test'

class IptablesRulesTest < PostProvisionTest
  def initialize
    self.module_name = 'iptables_rules'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_local_command('iptables-persistent installed',
                         'dpkg -l iptables-persistent | grep -E "^ii"',
                         'iptables-persistent')
    end
    tier(2) do
      test_local_command('netfilter-persistent available', 'which netfilter-persistent', 'netfilter-persistent')
      test_command_succeeds('iptables runs', 'iptables --version')
    end
  end
end

IptablesRulesTest.new.run
