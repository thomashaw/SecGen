require_relative '../../../../../lib/post_provision_test'

class WeechatTest < PostProvisionTest
  def initialize
    self.module_name = 'weechat'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) { test_local_command('weechat installed?', 'dpkg -l weechat | grep -E "^ii"', 'weechat') }
    tier(2) { test_command_succeeds('weechat binary exists', 'test -x /usr/bin/weechat') }
  end
end

WeechatTest.new.run
