require_relative '../../../../../lib/post_provision_test'

class KaliPwtoolsTest < PostProvisionTest
  def initialize
    self.module_name = 'kali_pwtools'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) { test_local_command('kali-tools-passwords installed', 'dpkg -l kali-tools-passwords | grep -E "^ii"', 'kali-tools-passwords') }
    tier(2) { test_command_succeeds('john available', 'john --list=formats') }
  end
end

KaliPwtoolsTest.new.run
