require_relative '../../../../../lib/post_provision_test'

class KaliForensicTest < PostProvisionTest
  def initialize
    self.module_name = 'kali_forensic'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) { test_local_command('kali-tools-forensics installed', 'dpkg -l kali-tools-forensics | grep -E "^ii"', 'kali-tools-forensics') }
    tier(2) { test_command_succeeds('foremost available', 'foremost -V') }
  end
end

KaliForensicTest.new.run
