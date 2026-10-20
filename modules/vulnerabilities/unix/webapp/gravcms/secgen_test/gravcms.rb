require_relative '../../../../../lib/post_provision_test'

# Post-provision test for the GravCMS Admin Plugin unauthenticated RCE
# (CVE-2021-21425, exploit/linux/http/gravcms_exec). The module ships Grav core
# 1.7.8 with Admin Plugin 1.10.7 (vulnerable), served by Apache at /grav-admin/.
#
# Tiers:
#   1  Apache, PHP and the extracted Grav app are present on the target.
#   2  Apache serves the Grav site over HTTP (port 80, /grav-admin/).
#   3  the Metasploit exploit, run from the attacker VM, achieves RCE; the
#      flag leaked into the webroot is captured alongside the sentinel as
#      evidence. With no attacker VM in the project, tier 3 SKIPs and the
#      lower tiers still run.
class GravcmsTest < PostProvisionTest
  def initialize
    self.module_name = 'gravcms'
    self.module_path = get_module_path(__FILE__)
    super
    self.port = 80
  end

  def test_module
    super

    # Tier 1 — provisioned on the target.
    tier(1) do
      test_command_succeeds('apache2 installed', 'test -x /usr/sbin/apache2')
      test_command_succeeds('php installed', 'command -v php')
      test_command_succeeds('grav app extracted', 'test -d /var/www/html/grav-admin')
    end

    # Tier 2 — Apache serves the Grav site.
    tier(2) do
      test_service_up                               # port 80 listening
      test_http('/grav-admin/', match: 'Grav')      # Grav renders its home page
    end

    # Tier 3 — unauthenticated RCE from the attacker VM.
    # gravcms_exec is a PHP-platform exploit, so the helper's default
    # cmd/unix/generic is rejected ("not a compatible payload"). php/unix/cmd/generic
    # is its PHP-platform twin: a no-session command payload that runs CMD and
    # drops the sentinel, so there's no reverse/bind shell to race.
    #
    # The exploit implants the command as a Grav scheduler job, which the module's
    # cron runs within ~60s rather than immediately, so poll for the sentinel
    # (sentinel_wait) instead of reading it once. collect reads the flag the
    # module leaks into /var/www (outside the web root) as evidence of access.
    #
    # PHP 8 / Debian 12 NOTE (resolved): Debian 12's stock PHP 8.2 rejects a
    # trailing `?>` close tag inside eval() (PHP 7 tolerated it), and every msf
    # php payload ends in one, so the exploit's implanted Grav scheduler job
    # ("PHP Parse error: unexpected token '?', expecting end of file") used to
    # die before it could run -- the vuln fired but tier 3 FAILed on this base.
    # Fixed by requiring modules/utilities/unix/languages/php7 (see gravcms's
    # secgen_metadata.xml), which installs PHP 7.4 alongside php8.2 (additional
    # software, not a downgrade) and makes 7.4 the default `php`/apache handler.
    # Verified PASS to tier 3 on a live Debian 12 Bookworm build (2026-10-20):
    # sentinel confirmed RCE as www-data via exploit/linux/http/gravcms_exec.
    # php7 carries a <conflict> list so SecGen won't compose it onto a system
    # with another module that needs PHP 8 -- see that module's manifests for
    # exactly what the conflict does and does not catch (residual risk noted
    # there: a PHP-dependent module not on that list could still be composed
    # alongside this one and have PHP silently switched to 7.4 under it).
    test_msf_exploit('exploit/linux/http/gravcms_exec',
                     rport: 80,
                     payload: 'php/unix/cmd/generic',
                     options: { 'TARGETURI' => '/grav-admin/' },
                     collect: 'cat /var/www/* 2>/dev/null',
                     sentinel_wait: 150,
                     tier: 3)
  end
end

GravcmsTest.new.run
