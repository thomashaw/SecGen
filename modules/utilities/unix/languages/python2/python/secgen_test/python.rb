require_relative '../../../../../lib/post_provision_test'

class PythonTest < PostProvisionTest
  def initialize
    self.module_name = 'python'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) { test_local_command('python2 on PATH?', 'which python2 || which python', 'python') }
    tier(2) { test_command_succeeds('python2 version', 'python2 --version 2>&1 || python --version 2>&1') }
  end
end

PythonTest.new.run
