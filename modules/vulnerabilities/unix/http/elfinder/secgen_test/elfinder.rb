require_relative '../../../../../lib/post_provision_test'

class ElfinderTest < PostProvisionTest
  def initialize
    self.module_name = 'elfinder'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super

    tier(1) do
      test_local_command('apache running', 'systemctl is-active apache2', 'active')
      test_local_command('elfinder files exist', 'test -d /var/www/elFinder-2.1.58', '')
      test_local_command('connector present', 'test -f /var/www/elFinder-2.1.58/php/connector.minimal.php', '')
    end

    tier(2) do
      test_service_up
      test_http('/', match: 'elFinder')
      test_http('/php/connector.minimal.php', status: [400, 403, 200])
    end

    tier(3) do
      # CVE-2021-32682: command injection via elFinder archive functionality
      # MSF module: exploit/linux/http/elfinder_archive_cmd_injection
      if attack_system.nil?
        skip_check('msf exploit (elfinder_archive_cmd_injection)',
                   'no attacker VM in project; add a Kali base with type=attack to test tier 3',
                   tier: 3, evidence: nil)
        next
      end
      test_msf_exploit('exploit/linux/http/elfinder_archive_cmd_injection',
                       rport: self.port.to_i,
                       collect: 'id')
    end
  end
end

ElfinderTest.new.run
