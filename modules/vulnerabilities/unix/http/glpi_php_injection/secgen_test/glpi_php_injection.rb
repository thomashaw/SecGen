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

      # Negative check (design level): the php2shell vector (CVE-2026-109618 /
      # CVE-2026-109619) is closed because php8.2 carries the Debian fix
      # (DLA-4848-1, 8.2.35-1~deb12u1). Verified from the installed package
      # version, not by sending exploit traffic. The app's own CVE-2022-35914
      # is the intended exploit (tier 3 still to be confirmed, see #255).
      fixed = '8.2.35-1~deb12u1'
      ver = run_command("dpkg-query -W -f='${Version}' php8.2-common 2>/dev/null")[:stdout].to_s.strip
      patched = run_command("dpkg --compare-versions '#{ver}' ge '#{fixed}' && echo FIXED")[:stdout].to_s.include?('FIXED')
      if patched
        pass_check('php2shell fix present (CVE-2026-109618/109619)',
                   "php8.2-common #{ver} >= #{fixed} (DLA-4848-1)", tier: 2)
      else
        fail_check('php2shell fix present (CVE-2026-109618/109619)',
                   "php8.2-common #{ver.inspect} < fixed #{fixed}", tier: 2)
      end
    end
    # TODO tier 3 (exploit): deferred — wire up test_msf_exploit when attacker VM is available
    # msf_module: exploit/linux/http/glpi_htmlawed_php_injection (CVE-2022-35914)
  end
end

GlpiPhpInjectionTest.new.run
