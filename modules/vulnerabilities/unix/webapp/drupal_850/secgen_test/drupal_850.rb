require_relative '../../../../../lib/post_provision_test'

# Drupal 8.5.0 (Drupalgeddon2, CVE-2018-7600) on patched PHP 7.4 (php7_patched).
# tier 2 proves the stack serves (a Drupal 200 means Apache -> php-fpm -> PHP ->
# MySQL all work) and that the php2shell fix is present, so only the app's own
# CVE is exploitable. tier 3 (the msf exploit) is deferred to a scenario with an
# attacker VM.
class Drupal850Test < PostProvisionTest
  FIX_COMMIT = '712e60f9edfd03f0960fc4fc3c2c375f42b90aa2'.freeze

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
      test_local_command('patched php present', 'test -x /opt/php-7.4-patched/bin/php && echo ok', 'ok')
    end
    tier(2) do
      test_service_up
      # A Drupal 200 over the guest-agent path proves Apache -> php-fpm -> PHP ->
      # MySQL end to end (Drupal 500s if the DB is unreachable).
      test_local_command('drupal serves (Apache+php-fpm+DB)',
                         "curl -s -o /dev/null -w '%{http_code}' http://127.0.0.1:#{self.port}/", '200')
      # php2shell closed: patched build is in place (provenance, not exploit).
      test_local_command('php2shell fix present (CVE-2026-109618/109619)',
                         'cat /opt/php-7.4-patched/PHP2SHELL_FIX_COMMIT', FIX_COMMIT)
    end
    # tier 3 (exploit): wire test_msf_exploit('exploit/unix/webapp/drupal_drupalgeddon2')
    # in a scenario with a type=attack VM.
  end
end

Drupal850Test.new.run
