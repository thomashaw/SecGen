require_relative '../../../../../lib/post_provision_test'

class Drupal850Test < PostProvisionTest
  def initialize
    self.module_name = 'drupal_850'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_local_command('drupal docroot exists', 'test -d /var/www/drupal-8.5.0 && echo ok', 'ok')
      test_local_command('apache2 installed', 'dpkg -l apache2 | grep -E "^ii"', 'apache2')
    end
    tier(2) do
      test_service_up
      test_http('/', port: self.port)
    end
    # TODO tier 3 (exploit): deferred — wire up test_msf_exploit (exploit/unix/webapp/drupal_drupalgeddon2) when attacker VM is available
  end
end

Drupal850Test.new.run
