require_relative '../../../../../lib/post_provision_test'

class IptablesRulesTest < PostProvisionTest
  def initialize
    self.module_name = 'iptables_rules'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    rules = self.json_inputs['rules'] || []
    forwarding = self.json_inputs['ip_forwarding']&.first

    tier(1) do
      test_local_command('iptables-persistent installed',
                         'dpkg -l iptables-persistent | grep -E "^ii"',
                         'iptables-persistent')
      test_local_command('iptables installed', 'which iptables', 'iptables')
      # every configured rule is written to the persistent rules file
      # ('-t nat ' rules are placed in the nat section without the prefix)
      rules.each do |rule|
        written = rule.sub(/^-t nat /, '')
        test_local_command("rule in rules.v4: #{rule}", 'cat /etc/iptables/rules.v4', written)
      end
      # ip_forwarding only changes anything when true
      if forwarding == 'true'
        test_local_command('ip forwarding persisted', 'cat /etc/sysctl.conf', 'net.ipv4.ip_forward=1')
      end
    end
    tier(2) do
      test_local_command('netfilter-persistent available', 'which netfilter-persistent', 'netfilter-persistent')
      test_command_succeeds('iptables runs', 'iptables --version')
      # the saved rules file parses: iptables-restore --test validates without applying
      test_command_succeeds('rules.v4 is valid', 'iptables-restore --test < /etc/iptables/rules.v4')
      if forwarding == 'true'
        test_local_command('ip forwarding active', 'sysctl -n net.ipv4.ip_forward', '1')
      end
    end
  end
end

IptablesRulesTest.new.run
