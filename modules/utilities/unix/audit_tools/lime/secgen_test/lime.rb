require_relative '../../../../../lib/post_provision_test'

class LimeTest < PostProvisionTest
  def initialize
    self.module_name = 'lime'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    # LiME builds a kernel module (.ko) from source at /root/LiME/src
    tier(1) { test_local_command('LiME source cloned?', 'test -d /root/LiME/src && echo "ok"', 'ok') }
    tier(2) { test_local_command('LiME .ko built?', 'find /root/LiME/src -name "lime-*.ko" 2>/dev/null | head -1', '.ko') }
  end
end

LimeTest.new.run
