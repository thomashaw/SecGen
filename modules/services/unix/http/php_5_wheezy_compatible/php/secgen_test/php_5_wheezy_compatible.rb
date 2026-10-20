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
    tier(2) { test_command_succeeds('php -r works', 'php -r "echo phpversion();"') }
  end
end

Php5WheezyCompatibleTest.new.run
