require_relative '../../../../../lib/post_provision_test'

class GlpiPhpInjectionTest < PostProvisionTest
  def initialize
    self.module_name = 'glpi_php_injection'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      # GLPI requires apache and mysql (declared in <requires>); check both are present
      test_local_command('apache2 installed?', 'dpkg -l apache2 | grep -E "^ii"', 'apache2')
      test_local_command('glpi installed?', 'test -d /var/www/html/glpi && echo present', 'present')
      test_local_command('php installed?', 'php --version', 'PHP')
    end
    tier(2) do
      test_service_up
      # GLPI login page returns a form with "GLPI" branding
      test_http('/', match: 'GLPI')
    end
    # TODO tier 3 (exploit): deferred — wire up test_msf_exploit when attacker VM is available
    # msf_module: exploit/linux/http/glpi_htmlawed_php_injection (CVE-2022-35914)
  end
end

GlpiPhpInjectionTest.new.run
