require_relative '../../../../../lib/post_provision_test'

class ReversingToolsTest < PostProvisionTest
  def initialize
    self.module_name = 'reversing_tools'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) { test_local_command('gdb installed?', 'which gdb', 'gdb') }
    tier(2) do
      test_command_succeeds('gdb reports version', 'gdb --version')
      test_command_succeeds('strace available', 'strace --version')
      test_local_command('upx installed?', 'test -x /usr/local/bin/upx && echo "ok"', 'ok')
    end
  end
end

ReversingToolsTest.new.run
