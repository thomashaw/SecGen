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
      test_local_command('mod_php installed', 'dpkg -l libapache2-mod-php | grep -E "^ii"', 'libapache2-mod-php')
      test_local_command('mysql 5.7 built', 'test -x /usr/local/mysql/bin/mysqld && echo ok', 'ok')
    end
    tier(2) do
      test_service_up
      # identifies as Drupal, not just "a web server" (R4c)
      test_http('/', port: self.port, match: 'Drupal')
      test_local_command('mysql service active', 'systemctl is-active mysql', 'active')

      # strings_to_pre_leak: written as a comment into the (statically served,
      # pre-authentication) robots.txt - a dirbuster-style clue reachable
      # before exploitation.
      if (pre_leak = self.json_inputs['strings_to_pre_leak']&.first)
        test_http('/robots.txt', port: self.port, match: pre_leak)
      end

      # strings_to_leak is NOT checked here: it is written as a PHP comment
      # inside settings.php, which Apache executes rather than serves as
      # text, so it is unreachable without the RCE this module exists to
      # demonstrate - checking it is tier 3's job (reading the file via the
      # exploited shell), not tier 2's.
    end
    test_msf_exploit('exploit/unix/webapp/drupal_drupalgeddon2',
                     rport: self.port,
                     expect_privilege: 'user_rwx',
                     tier: 3)
  end
end

Drupal850Test.new.run
