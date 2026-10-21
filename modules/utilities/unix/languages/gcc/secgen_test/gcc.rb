require_relative '../../../../../lib/post_provision_test'

class GccTest < PostProvisionTest
  def initialize
    self.module_name = 'gcc'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_local_command('gcc installed?', 'which gcc', 'gcc')
      test_local_command('build-essential installed?', 'dpkg -l build-essential | grep -E "^ii"', 'build-essential')
    end
    tier(2) { test_command_succeeds('gcc version', 'gcc --version') }
  end
end

GccTest.new.run
