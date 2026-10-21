require_relative '../../../../../lib/post_provision_test'

class AugeasTest < PostProvisionTest
  def initialize
    self.module_name = 'augeas'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) { test_local_command('augeas-tools installed?', 'dpkg -l augeas-tools | grep -E "^ii"', 'augeas-tools') }
    tier(2) { test_command_succeeds('augtool runs', 'echo "quit" | augtool') }
  end
end

AugeasTest.new.run
