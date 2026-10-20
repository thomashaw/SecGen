require_relative '../../../../../lib/post_provision_test'

class PidginTest < PostProvisionTest
  def initialize
    self.module_name = 'pidgin'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) { test_local_command('pidgin installed?', 'dpkg -l pidgin | grep -E "^ii"', 'pidgin') }
    tier(2) { test_command_succeeds('pidgin binary exists', 'test -x /usr/bin/pidgin') }
  end
end

PidginTest.new.run
