require_relative '../../../../../lib/post_provision_test'

class ArmitageTest < PostProvisionTest
  def initialize
    self.module_name = 'armitage'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) { test_local_command('armitage package installed', 'dpkg -l armitage | grep -E "^ii"', 'armitage') }
    tier(2) { test_command_succeeds('armitage binary present', 'test -x /usr/bin/armitage') }
  end
end

ArmitageTest.new.run
