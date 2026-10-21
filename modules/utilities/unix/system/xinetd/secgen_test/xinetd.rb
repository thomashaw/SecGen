require_relative '../../../../../lib/post_provision_test'

class XinetdTest < PostProvisionTest
  def initialize
    self.module_name = 'xinetd'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_local_command('xinetd installed', 'dpkg -l xinetd | grep -E "^ii"', 'xinetd')
      test_local_command('xinetd binary present', 'which xinetd', 'xinetd')
      test_local_command('xinetd.conf present', 'test -f /etc/xinetd.conf && echo ok', 'ok')
      test_local_command('xinetd.d directory present', 'test -d /etc/xinetd.d && echo ok', 'ok')
      test_local_command('xinetd.conf includes xinetd.d', 'grep includedir /etc/xinetd.conf', '/etc/xinetd.d')
    end
    tier(2) do
      # xinetd only listens when other modules configure services, so check the daemon itself
      test_local_command('xinetd service enabled', 'systemctl is-enabled xinetd', 'enabled')
      test_command_succeeds('xinetd service active', 'systemctl is-active --quiet xinetd')
      test_command_succeeds('xinetd process running', 'pgrep -x xinetd')
    end
  end
end

XinetdTest.new.run
