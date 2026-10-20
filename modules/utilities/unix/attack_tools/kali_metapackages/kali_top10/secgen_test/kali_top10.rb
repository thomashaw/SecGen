require_relative '../../../../../lib/post_provision_test'

class KaliTop10Test < PostProvisionTest
  def initialize
    self.module_name = 'kali_top10'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      # kali-tools-top10 is not installed directly; these are the explicit packages
      test_local_command('nmap installed', 'dpkg -l nmap | grep -E "^ii"', 'nmap')
      test_local_command('metasploit-framework installed', 'dpkg -l metasploit-framework | grep -E "^ii"', 'metasploit-framework')
      test_local_command('sqlmap installed', 'dpkg -l sqlmap | grep -E "^ii"', 'sqlmap')
      test_local_command('wireshark installed', 'dpkg -l wireshark | grep -E "^ii"', 'wireshark')
    end
    tier(2) { test_command_succeeds('nmap runs', 'nmap --version') }
  end
end

KaliTop10Test.new.run
