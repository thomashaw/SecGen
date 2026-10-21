require_relative '../../../../../lib/post_provision_test'

class SudoTest < PostProvisionTest
  def initialize
    self.module_name = 'sudo'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) { test_local_command('sudo binary present', 'which sudo', '/usr/bin/sudo') }
    tier(2) do
      test_command_succeeds('sudo version runs', 'sudo --version 2>&1 | head -1')
      test_local_command('sudoers file present', 'test -f /etc/sudoers && echo ok', 'ok')
    end
  end
end

SudoTest.new.run
