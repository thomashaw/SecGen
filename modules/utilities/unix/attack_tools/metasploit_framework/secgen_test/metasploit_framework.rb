require_relative '../../../../../lib/post_provision_test'

class MetasploitFrameworkTest < PostProvisionTest
  def initialize
    self.module_name = 'metasploit_framework'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) { test_local_command('metasploit-framework package installed', 'dpkg -l metasploit-framework | grep -E "^ii"', 'metasploit-framework') }
    tier(2) { test_command_succeeds('msfconsole present', 'test -x /usr/bin/msfconsole') }
  end
end

MetasploitFrameworkTest.new.run
