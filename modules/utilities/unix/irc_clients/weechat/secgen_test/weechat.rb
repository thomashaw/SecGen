require_relative '../../../../../lib/post_provision_test'

class WeechatTest < PostProvisionTest
  def initialize
    self.module_name = 'weechat'
    self.module_path = get_module_path(__FILE__)
    super
  end

  # No read_facts or default inputs: nothing to parameterise.
  def test_module
    super
    tier(1) do
      test_local_command('weechat installed?', 'dpkg -l weechat | grep -E "^ii"', 'weechat')
      test_local_command('weechat on PATH', 'which weechat', '/usr/bin/weechat')
    end
    tier(2) do
      test_command_succeeds('weechat binary exists', 'test -x /usr/bin/weechat')
      # weechat --version prints the version and exits without a terminal
      test_command_succeeds('weechat runs from a login shell', 'weechat --version', user: 'root')
    end
  end
end

WeechatTest.new.run
