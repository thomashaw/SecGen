require_relative '../../../../../lib/post_provision_test'

class BinaryScriptContainerTest < PostProvisionTest
  def initialize
    self.module_name = 'binary_script_container'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_command_succeeds('suid binary installed', 'test -x /usr/local/bin/suid')
    end
    tier(2) do
      # The binary is installed setuid root; verify the SUID bit is set
      test_local_command('SUID bit set on binary', 'ls -la /usr/local/bin/suid', 'rws')
    end
  end
end

BinaryScriptContainerTest.new.run
