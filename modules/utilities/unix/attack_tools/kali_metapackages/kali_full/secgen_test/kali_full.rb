require_relative '../../../../../lib/post_provision_test'

class KaliFullTest < PostProvisionTest
  def initialize
    self.module_name = 'kali_full'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) { test_local_command('kali-linux-everything installed', 'dpkg -l kali-linux-everything | grep -E "^ii"', 'kali-linux-everything') }
    tier(2) { test_command_succeeds('nmap available', 'nmap --version') }
  end
end

KaliFullTest.new.run
