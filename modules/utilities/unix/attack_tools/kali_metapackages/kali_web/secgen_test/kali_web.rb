require_relative '../../../../../lib/post_provision_test'

class KaliWebTest < PostProvisionTest
  def initialize
    self.module_name = 'kali_web'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      # kali-tools-web is not installed directly; check representative packages
      test_local_command('nikto installed', 'dpkg -l nikto | grep -E "^ii"', 'nikto')
      test_local_command('sqlmap installed', 'dpkg -l sqlmap | grep -E "^ii"', 'sqlmap')
      test_local_command('wpscan installed', 'dpkg -l wpscan | grep -E "^ii"', 'wpscan')
    end
    tier(2) { test_command_succeeds('nikto runs', 'nikto -Version') }
  end
end

KaliWebTest.new.run
