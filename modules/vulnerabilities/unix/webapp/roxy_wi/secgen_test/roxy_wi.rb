require_relative '../../../../../lib/post_provision_test'

# Post-provision test for the Roxy-WI unauthenticated RCE (CVE-2022-31137,
# exploit/linux/http/roxy_wi_exec). The module bundles Roxy-WI v6.1.0.0
# (files/roxy-wi-6.1.0.0.zip, a release before the June-2022 6.1.1.0 fix),
# served by Apache as Python CGI over HTTPS on port 443. CVE-2022-31137 is an
# unauthenticated command injection in app/options.py, which concatenates
# request parameters into a shell command, giving execution as www-data on
# affected versions (< 6.1.1.0).
#
# Tiers 1 and 2 are verified on a real Debian 12 build: the module provisions
# cleanly and Apache serves the Roxy-WI site over HTTPS on port 443.
#
# Tiers:
#   1  Apache, Python and the extracted Roxy-WI app are present on the target.
#   2  Apache serves the Roxy-WI site over HTTPS (port 443) and renders its page.
#   3  the Metasploit exploit, run from the attacker VM, achieves RCE; the flag
#      leaked into /var/www (outside the web root) is captured alongside the
#      sentinel as evidence. With no attacker VM in the project, tier 3 SKIPs
#      and the lower tiers still run.
class RoxyWiTest < PostProvisionTest
  def initialize
    self.module_name = 'roxy_wi'
    self.module_path = get_module_path(__FILE__)
    super
    self.port = 443
  end

  def test_module
    super

    # Tier 1 — provisioned on the target.
    tier(1) do
      test_command_succeeds('apache2 installed', 'test -x /usr/sbin/apache2')
      test_command_succeeds('python3 installed', 'command -v python3')
      test_command_succeeds('roxy-wi app extracted', 'test -d /var/www/haproxy-wi/app')
      test_command_succeeds('roxy-wi config installed', 'test -f /etc/roxy-wi/roxy-wi.cfg')
    end

    # Tier 2 — Apache serves the Roxy-WI site over HTTPS. The vhost is SSL-only
    # (*:443) and uses a self-signed certificate; the test helper's curl runs
    # with -k so the cert is accepted.
    tier(2) do
      test_service_up                                            # port 443 listening
      test_http('/', match: 'Roxy-WI', scheme: 'https')          # index renders
    end

    # Tier 3 — unauthenticated RCE from the attacker VM. roxy_wi_exec is a
    # linux/unix command-injection exploit, so the helper's default no-session
    # cmd/unix/generic payload is compatible (the command runs on the target
    # directly, no reverse/bind shell to race) and the command runs
    # synchronously, so the sentinel is present as soon as msf returns.
    # SSL is on and the service is on 443; TARGETURI is the document root.
    #
    # KNOWN LIMITATION (verified on a live build): this tier FAILs with the module
    # as shipped. The msf module's own AutoCheck returns "not-vulnerable" (the
    # target does not return the expected response), so the exploit aborts before
    # sending a payload. The bundled roxy-wi-master.zip installs app/options.py
    # dated Oct 2022, i.e. at or after the CVE-2022-31137 fix (6.1.1.0, June 2022),
    # so the injection sink the exploit targets is patched. To reach tier 3 the
    # module must bundle a confirmed-vulnerable release (< 6.1.1.0) and route its
    # CGI endpoint; tiers 1-2 prove the service provisions and serves. (The module
    # provisions cleanly on Debian 12 / PHP-free Python CGI, so the blocker is the
    # bundled version, not the base.)
    test_msf_exploit('exploit/linux/http/roxy_wi_exec',
                     rport: 443,
                     options: { 'SSL' => true, 'TARGETURI' => '/' },
                     collect: 'cat /var/www/* 2>/dev/null',
                     force: true,
                     tier: 3)
  end
end

RoxyWiTest.new.run
