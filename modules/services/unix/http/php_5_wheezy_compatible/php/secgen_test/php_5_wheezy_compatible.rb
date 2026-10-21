require_relative '../../../../../lib/post_provision_test'

class Php5WheezyCompatibleTest < PostProvisionTest
  def initialize
    self.module_name = 'php_5_wheezy_compatible'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) { test_local_command('php installed?', 'php --version', 'PHP 5') }
    %w[php5 mysql-client php5-mysql phpmyadmin].each do |pkg|
      tier(1) { test_local_command("#{pkg} installed", "dpkg -s #{pkg}", 'Status: install ok installed') }
    end
    tier(1) { test_local_command('phpmyadmin files present', 'test -d /usr/share/phpmyadmin && echo ok', 'ok') }
    tier(2) { test_command_succeeds('php -r works', 'php -r "echo phpversion();"') }
    tier(2) { test_local_command('php runs as 5.x', 'php -r "echo PHP_MAJOR_VERSION;"', '5') }
    tier(2) { test_local_command('php mysql extension loads', 'php -m', 'mysql') }
    tier(2) { test_command_succeeds('mysql client runs', 'mysql --version') }
  end
end

Php5WheezyCompatibleTest.new.run
