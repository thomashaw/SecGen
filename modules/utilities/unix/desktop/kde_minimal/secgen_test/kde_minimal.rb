require_relative '../../../../../lib/post_provision_test'

class KdeMinimalTest < PostProvisionTest
  def initialize
    self.module_name = 'kde_minimal'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) { test_local_command('kde-plasma-desktop installed?', 'dpkg -l kde-plasma-desktop | grep -E "^ii"', 'kde-plasma-desktop') }
    tier(2) { test_command_succeeds('plasmashell binary exists', 'test -x /usr/bin/plasmashell') }
  end
end

KdeMinimalTest.new.run
