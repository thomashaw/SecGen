require_relative '../../../../../lib/post_provision_test'

class ApparmorTest < PostProvisionTest
  def initialize
    self.module_name = 'apparmor'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) { test_local_command('apparmor installed?', 'dpkg -l apparmor | grep -E "^ii"', 'apparmor') }
    tier(2) do
      test_command_succeeds('apparmor service active', 'systemctl is-active apparmor')
      test_local_command('aa-status reports profiles', 'aa-status 2>&1 | grep -i "profile"', 'profile')
    end
  end
end

ApparmorTest.new.run
