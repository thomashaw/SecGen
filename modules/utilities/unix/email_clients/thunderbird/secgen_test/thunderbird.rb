require_relative '../../../../../lib/post_provision_test'

class ThunderbirdTest < PostProvisionTest
  def initialize
    self.module_name = 'thunderbird'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) { test_local_command('thunderbird installed?', 'dpkg -l thunderbird | grep -E "^ii"', 'thunderbird') }
    tier(2) { test_command_succeeds('thunderbird binary exists', 'test -x /usr/bin/thunderbird') }
  end
end

ThunderbirdTest.new.run
