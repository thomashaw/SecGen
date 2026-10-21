require_relative '../../../../../lib/post_provision_test'

class LampTest < PostProvisionTest
  PHP_PROBE = '/var/www/html/secgen_lamp_probe.php'

  def initialize
    self.module_name = 'lamp'
    self.module_path = get_module_path(__FILE__)
    super
    # The module has no port input; Apache listens on the default.
    self.port = 80
  end

  def test_module
    super
    tier(1) do
      test_local_command('apache installed', 'which apache2 apachectl', 'apache2')
      test_local_command('mysql/mariadb server installed', 'which mysqld mariadbd', 'mysqld')
      test_local_command('apache php module enabled', 'ls /etc/apache2/mods-enabled/', 'php')
    end
    tier(2) do
      test_service_up
      test_banner('Server: Apache', send: 'HEAD / HTTP/1.0')
      test_command_succeeds('mysql server answers', 'mysqladmin ping')
      # PHP is executed by Apache, not served as text: drop a probe, fetch it, remove it.
      run_command("echo '<?php echo \"lamp-\" . (6 * 7); ?>' > #{PHP_PROBE}")
      test_http('/secgen_lamp_probe.php', match: 'lamp-42')
      run_command("rm -f #{PHP_PROBE}")
    end
  end
end

LampTest.new.run
