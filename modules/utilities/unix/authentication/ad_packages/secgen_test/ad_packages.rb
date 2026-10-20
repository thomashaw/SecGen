require_relative '../../../../../lib/post_provision_test'

class AdPackagesTest < PostProvisionTest
  def initialize
    self.module_name = 'ad_packages'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_local_command('realmd installed', 'dpkg -l realmd | grep -E "^ii"', 'realmd')
      test_local_command('sssd installed', 'dpkg -l sssd | grep -E "^ii"', 'sssd')
      test_local_command('adcli installed', 'dpkg -l adcli | grep -E "^ii"', 'adcli')
    end
    tier(2) do
      test_command_succeeds('realm command available', 'realm --version')
      test_command_succeeds('samba-common tools available', 'net --version')
    end
  end
end

AdPackagesTest.new.run
