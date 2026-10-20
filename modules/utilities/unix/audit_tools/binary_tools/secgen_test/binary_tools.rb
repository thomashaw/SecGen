require_relative '../../../../../lib/post_provision_test'

class BinaryToolsTest < PostProvisionTest
  def initialize
    self.module_name = 'binary_tools'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) { test_local_command('binutils installed?', 'dpkg -l binutils | grep "^ii"', 'binutils') }
    tier(2) { test_command_succeeds('strings available', 'strings --version') }
  end
end

BinaryToolsTest.new.run
