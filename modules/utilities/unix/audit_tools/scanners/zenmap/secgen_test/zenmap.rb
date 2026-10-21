require_relative '../../../../../lib/post_provision_test'

class ZenmapTest < PostProvisionTest
  def initialize
    self.module_name = 'zenmap'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) { test_local_command('zenmap installed?', 'dpkg -l zenmap | grep "^ii"', 'zenmap') }
    # zenmap is a GUI app; verify the binary is present without launching a display
    tier(2) { test_local_command('zenmap binary present', 'test -e /usr/bin/zenmap && echo "ok"', 'ok') }
  end
end

ZenmapTest.new.run
