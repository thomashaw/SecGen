require_relative '../../../../../lib/post_provision_test'

# Post-provision test for the Roxy-WI unauthenticated RCE (CVE-2022-31137,
# exploit/linux/http/roxy_wi_exec). The module bundles Roxy-WI v6.1.0.0
# (files/roxy-wi-6.1.0.0.zip, a release before the June-2022 6.1.1.0 fix),
# served by Apache as Python CGI over HTTPS on port 443. CVE-2022-31137 is an
# unauthenticated command injection in app/options.py, which concatenates
# request parameters into a shell command, giving execution as www-data on
# affected versions (< 6.1.1.0).
#
# All three tiers are verified on a real Debian 12 + Kali/MSF build: the module
# provisions cleanly, Apache serves the Roxy-WI site over HTTPS on port 443, and
# roxy_wi_exec achieves RCE as www-data from the attacker VM.
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

    # Tier 3 — unauthenticated RCE from the attacker VM, verified on a live
    # Debian 12 build. roxy_wi_exec injects a shell command into the `ipbackend`
    # POST parameter of app/options.py, which v6.1.0.0 concatenates into a
    # `subprocess_execute(..., shell=True)` call (the sink the June-2022 6.1.1.0
    # release fixed). The msf AutoCheck confirms the target vulnerable ("responded
    # to exploitation with a 200 OK and test command successfully executed"), so
    # the bundled 6.1.0.0 release is genuinely exploitable. The helper's default
    # no-session cmd/unix/generic payload fits (the command runs on the target
    # directly, no reverse/bind shell to race), and subprocess_execute blocks on
    # communicate(), so the sentinel is present as soon as msf returns.
    #
    # The injected command runs as the Apache CGI process, and Debian's
    # apache2.service ships with systemd `PrivateTmp=true`, giving Apache its own
    # /tmp namespace. A sentinel dropped in /tmp is therefore invisible to the
    # root guest-agent read that verifies it, so the proof file goes in /dev/shm
    # (shared, world-writable, outside PrivateTmp's /tmp + /var/tmp isolation).
    # `force: true` is kept so a momentarily inconclusive AutoCheck can't block
    # the exploit. SSL is on, the service is on 443, and TARGETURI is the root.
    #
    # `collect` captures the post-exploitation flag leaked into /var/www (owned
    # www-data, readable only via the RCE) as evidence alongside the uid= proof.
    # `head -c 4096` caps each file at 4 KB: the leaked flag is small text, and
    # the cap keeps the sentinel small so the guest-agent exec API does not stall
    # reading it back (megabytes of output over that API never report as exited).
    # The command is kept free of quotes: the helper wraps the whole command set
    # for `msfconsole -x`, and an embedded quote breaks its parser.
    test_msf_exploit('exploit/linux/http/roxy_wi_exec',
                     rport: 443,
                     options: { 'SSL' => true, 'TARGETURI' => '/' },
                     collect: 'head -c 4096 /var/www/* 2>/dev/null',
                     force: true,
                     sentinel_dir: '/dev/shm',
                     tier: 3)
  end
end

RoxyWiTest.new.run
