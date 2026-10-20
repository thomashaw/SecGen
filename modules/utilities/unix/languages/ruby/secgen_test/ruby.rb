require_relative '../../../../../lib/post_provision_test'

class RubyTest < PostProvisionTest
  def initialize
    self.module_name = 'ruby'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) { test_local_command('ruby on PATH?', 'which ruby', 'ruby') }
    tier(2) { test_command_succeeds('ruby version', 'ruby --version') }
  end
end

RubyTest.new.run
