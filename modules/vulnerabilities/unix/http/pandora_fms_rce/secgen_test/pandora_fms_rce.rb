require_relative '../../../../../lib/post_provision_test'

# Post-provision test for Pandora FMS 7.0NG.742 (unauthenticated SQLi to RCE,
# CVE-2021-32099). The module serves the console at /pandora_console/ on port 80
# using Apache, PHP 8 (with an app-scoped compatibility shim) and MariaDB.
#
# Tiers:
#   1  Apache, PHP, MariaDB, the extracted console and the loaded database exist.
#   2  Apache listens on port 80 and the console login page is returned.
# There is deliberately no tier 3 here.
class PandoraFmsRceTest < PostProvisionTest
  def initialize
    self.module_name = 'pandora_fms_rce'
    self.module_path = get_module_path(__FILE__)
    super
    self.port = 80
  end

  def test_module
    super

    tier(1) do
      test_command_succeeds('apache2 installed', 'test -x /usr/sbin/apache2')
      test_command_succeeds('php installed', 'command -v php')
      test_command_succeeds('mariadb installed', 'command -v mariadb || command -v mysql')
      test_command_succeeds('pandora console extracted', 'test -f /var/www/html/pandora_console/index.php')
      test_command_succeeds('pandora config written', 'test -f /var/www/html/pandora_console/include/config.php')
      test_command_succeeds('web installer removed', '! test -e /var/www/html/pandora_console/install.php')
      test_command_succeeds('pandora database loaded',
                            "mysql -N -e 'SELECT COUNT(*) FROM pandora.tconfig' | grep -qE '^[1-9]'")
    end

    tier(2) do
      test_service_up
      test_command_succeeds('mariadb running', 'systemctl is-active mariadb')
      test_http('/pandora_console/', match: 'Pandora')
    end
  end
end

PandoraFmsRceTest.new.run
