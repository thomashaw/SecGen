require_relative '../../../../../lib/post_provision_test'

class AdoreRootkitStaticTest < PostProvisionTest
  def initialize
    self.module_name = 'adore_rootkit_static'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_local_command('ls_a installed', 'test -f /bin/ls_a', '/bin/ls_a')
      test_local_command('netstat_a installed', 'test -f /bin/netstat_a', '/bin/netstat_a')
      test_local_command('ps_a installed', 'test -f /bin/ps_a', '/bin/ps_a')
    end
    tier(2) do
      test_command_succeeds('ls_a executable', '/bin/ls_a --help || /bin/ls_a /')
      test_command_succeeds('ps_a executable', '/bin/ps_a aux || true')
    end
  end
end

AdoreRootkitStaticTest.new.run
