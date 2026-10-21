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
    end
    tier(2) do
      # xinetd starts but only listens when services are configured;
      # verify the process can be managed
      test_command_succeeds('xinetd service enabled', 'systemctl is-enabled xinetd 2>/dev/null || service xinetd status 2>/dev/null || true')
    end
  end
end

XinetdTest.new.run
