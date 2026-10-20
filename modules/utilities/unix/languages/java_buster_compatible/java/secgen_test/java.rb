require_relative '../../../../../lib/post_provision_test'

class JavaTest < PostProvisionTest
  def initialize
    self.module_name = 'java'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) { test_local_command('java binary on PATH?', 'which java', 'java') }
    tier(2) { test_command_succeeds('java version', 'java -version') }
  end
end

JavaTest.new.run
