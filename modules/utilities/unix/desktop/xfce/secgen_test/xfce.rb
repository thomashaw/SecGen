require_relative '../../../../../lib/post_provision_test'

class XfceTest < PostProvisionTest
  def initialize
    self.module_name = 'xfce'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) { test_local_command('xfce4 installed?', 'dpkg -l xfce4 | grep -E "^ii"', 'xfce4') }
    tier(2) { test_command_succeeds('xfce4-session binary exists', 'test -x /usr/bin/xfce4-session') }
  end
end

XfceTest.new.run
