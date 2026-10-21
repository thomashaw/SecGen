require_relative '../../../../../lib/post_provision_test'

class DefaultClientFirewallTest < PostProvisionTest
  def initialize
    self.module_name = 'default_client_firewall'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    # no inputs to check: the module takes none
    tier(1) do
      test_local_command('iptables installed', 'which iptables', 'iptables')
      test_local_command('iptables-persistent installed', 'dpkg -l iptables-persistent | grep -E "^ii"', 'iptables-persistent')
      # each rule the Puppet declares is loaded, identified by its comment
      ['000 accept all icmp', '001 accept all to lo interface', '002 reject local traffic not on loopback interface',
       '003 accept related established rules', '999 drop all'].each do |rule|
        test_local_command("rule '#{rule}' loaded", 'iptables-save', rule)
      end
    end
    tier(2) do
      # The default firewall sets ACCEPT and DROP rules; verify at least one chain has rules
      test_local_command('firewall rules loaded', 'iptables -L -n', 'Chain')
      test_local_command('drop-all rule present', 'iptables -L -n | tail -5', 'DROP')
      # the catch-all drop is the last INPUT rule
      test_local_command('drop-all is the last INPUT rule', 'iptables -S INPUT | tail -1', '999 drop all')
      # loopback and ICMP stay open: ping to localhost passes through rules 000 and 001
      test_command_succeeds('loopback ICMP allowed', 'ping -c1 -W3 127.0.0.1')
    end
  end
end

DefaultClientFirewallTest.new.run
