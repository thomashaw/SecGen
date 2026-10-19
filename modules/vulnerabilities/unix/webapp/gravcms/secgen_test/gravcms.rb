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
    # KNOWN LIMITATION (Debian 12 / PHP 8.2): this tier FAILs on our standard
    # base, and the failure is upstream, not a module defect. Verified on a live
    # build: the vuln is confirmed, the scheduler job is implanted and fires, but
    # it dies with "PHP Parse error: unexpected token '?', expecting end of file
    # ... eval()'d code". gravcms_exec runs `php -r eval(base64_decode("<payload>"))`
    # and every msf php payload ends in a `?>` close tag; PHP 8.0+ rejects a
    # trailing `?>` inside eval() (PHP 7 tolerated it). The only target-side fix is
    # pointing /usr/bin/php at PHP 7, a de-facto system downgrade that would break
    # other PHP-8 modules composed into the same VM, so it is deliberately not done.
    # Tier 3 therefore passes only on a PHP<8 base or with a patched exploit; on
    # Debian 12 the module is a genuine, confirmed vuln exercised to tier 2.
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
