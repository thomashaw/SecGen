require_relative '../../../../../lib/post_provision_test'

class LinuxkiRceTest < PostProvisionTest
  def initialize
    self.module_name = 'linuxki_rce'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_local_command('linuxki package installed?', 'dpkg -l linuxki | grep -E "^ii"', 'linuxki')
      test_local_command('php installed?', 'php --version', 'PHP')
      # LinuxKI PHP scripts are served via Apache
      test_local_command('apache2 installed?', 'dpkg -l apache2 | grep -E "^ii"', 'apache2')
    end
    tier(2) do
      test_service_up
      # kivis.php is the entry point for the LinuxKI web interface
      test_http('/linuxki/kivis.php', match: nil)
    end
    # TODO tier 3 (exploit): deferred — wire up test_msf_exploit when attacker VM is available
    # msf_module: exploit/linux/http/linuxki_rce (CVE-2020-7209)
  end
end

LinuxkiRceTest.new.run
