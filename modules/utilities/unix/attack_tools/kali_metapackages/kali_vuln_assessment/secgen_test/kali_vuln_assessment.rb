require_relative '../../../../../lib/post_provision_test'

class KaliVulnAssessmentTest < PostProvisionTest
  def initialize
    self.module_name = 'kali_vuln_assessment'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) { test_local_command('kali-tools-vulnerability installed', 'dpkg -l kali-tools-vulnerability | grep -E "^ii"', 'kali-tools-vulnerability') }
    tier(2) { test_command_succeeds('nmap available', 'nmap --version') }
  end
end

KaliVulnAssessmentTest.new.run
