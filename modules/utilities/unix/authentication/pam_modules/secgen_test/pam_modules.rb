require_relative '../../../../../lib/post_provision_test'

class PamModulesTest < PostProvisionTest
  def initialize
    self.module_name = 'pam_modules'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_local_command('libpam-google-authenticator installed',
                         'dpkg -l libpam-google-authenticator | grep -E "^ii"',
                         'libpam-google-authenticator')
      test_local_command('pamtester installed', 'dpkg -l pamtester | grep -E "^ii"', 'pamtester')
      test_local_command('libpam-pwquality installed',
                         'dpkg -l libpam-pwquality | grep -E "^ii"',
                         'libpam-pwquality')
    end
    tier(2) do
      test_local_command('pamtester on PATH', 'which pamtester', 'pamtester')
      test_command_succeeds('google-authenticator binary present', 'test -f /usr/bin/google-authenticator')
    end
  end
end

PamModulesTest.new.run
