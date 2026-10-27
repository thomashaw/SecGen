require_relative '../../../../../lib/post_provision_test'

# Gitlist 0.4.0 RCE (CVE-2018-1000533) on patched PHP 7.4 (php7_patched).
# tier 2 proves GitList serves via Apache -> php-fpm and that the php2shell fix
# is present, so only the app's own argument-injection CVE is exploitable.
# tier 3 (exploit/multi/http/gitlist_arg_injection) is deferred to a scenario
# with an attacker VM.
class Gitlist040Test < PostProvisionTest
  FIX_COMMIT = '712e60f9edfd03f0960fc4fc3c2c375f42b90aa2'.freeze

  def initialize
    self.module_name = 'gitlist_040'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_local_command('gitlist docroot exists', 'test -d /var/www/gitlist && echo ok', 'ok')
      test_local_command('git installed', 'which git', '/usr/bin/git')
      test_local_command('patched php present', 'test -x /opt/php-7.4-patched/bin/php && echo ok', 'ok')
    end
    tier(2) do
      test_service_up
      # Match the page body (not curl %{http_code}: the % is eaten by the
      # guest-agent exec path). Retry while GitList/php-fpm warm up.
      test_local_command('gitlist serves (Apache+php-fpm)',
                         "for i in $(seq 1 15); do curl -s http://127.0.0.1:#{self.port}/ | grep -q GitList && { echo SERVED_GITLIST; break; }; sleep 2; done", 'SERVED_GITLIST')
      test_local_command('php2shell fix present (CVE-2026-109618/109619)',
                         'cat /opt/php-7.4-patched/PHP2SHELL_FIX_COMMIT', FIX_COMMIT)
    end
  end
end

Gitlist040Test.new.run
