require_relative '../../../../../lib/post_provision_test'

class HandyCliToolsTest < PostProvisionTest
  def initialize
    self.module_name = 'handy_cli_tools'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_local_command('vim installed', 'which vim', 'vim')
      test_local_command('curl installed', 'which curl', 'curl')
      test_local_command('rsync installed', 'which rsync', 'rsync')
    end
    tier(2) do
      test_command_succeeds('vim --version', 'vim --version')
      test_command_succeeds('curl --version', 'curl --version')
    end
  end
end

HandyCliToolsTest.new.run
