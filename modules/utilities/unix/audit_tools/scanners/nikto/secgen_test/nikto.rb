require_relative '../../../../../lib/post_provision_test'

class NiktoTest < PostProvisionTest
  def initialize
    self.module_name = 'nikto'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) { test_local_command('nikto installed?', 'which nikto', 'nikto') }
    tier(2) { test_command_succeeds('nikto reports version', 'nikto -Version') }
  end
end

NiktoTest.new.run
