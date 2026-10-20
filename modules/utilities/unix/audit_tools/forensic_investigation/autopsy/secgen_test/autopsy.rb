require_relative '../../../../../lib/post_provision_test'

class AutopsyTest < PostProvisionTest
  def initialize
    self.module_name = 'autopsy'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) { test_local_command('autopsy installed?', 'dpkg -l autopsy | grep "^ii"', 'autopsy') }
    tier(2) { test_local_command('autopsy binary present', 'test -x /usr/bin/autopsy && echo "ok"', 'ok') }
  end
end

AutopsyTest.new.run
