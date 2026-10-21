require_relative '../../../../../lib/post_provision_test'

class KaliReverseEngineeringTest < PostProvisionTest
  def initialize
    self.module_name = 'kali_reverse_engineering'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) { test_local_command('kali-tools-reverse-engineering installed', 'dpkg -l kali-tools-reverse-engineering | grep -E "^ii"', 'kali-tools-reverse-engineering') }
    tier(2) { test_command_succeeds('radare2 available', 'radare2 -v') }
  end
end

KaliReverseEngineeringTest.new.run
