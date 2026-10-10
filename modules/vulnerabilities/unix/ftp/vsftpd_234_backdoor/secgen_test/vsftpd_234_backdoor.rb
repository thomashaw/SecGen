require_relative '../../../../../lib/post_provision_test'

# Post-provision test for the vsftpd 2.3.4 backdoor (OSVDB-73573,
# exploit/unix/ftp/vsftpd_234_backdoor).
#
# Tiers:
#   1  the backdoored vsftpd 2.3.4 binary is installed on the target.
#   2  the FTP daemon is listening (port 21).
#   3  a real exploit from the attacker VM achieves remote command execution.
#
# Tier 3 is the second consumer of the framework helper test_msf_exploit, and
# exercises a different payload shape from distcc. In current Metasploit this
# module's only compatible payloads are command-stager "fetch" payloads
# (cmd/linux/http/*), not a cmd/unix/interact shell, so we drive the no-session
# cmd/linux/http/x64/exec: it runs the proof-of-RCE command on the target (the
# stager fetches a runner over HTTP from the attacker, which the helper wires up
# via FETCH_SRVHOST). The module's automatic check is inconclusive, so force:
# sets ForceExploit. RCE is proven the same way as distcc, by a sentinel the
# target's own guest agent reads back. With no attacker system (e.g. a single-VM
# module run) tier 3 SKIPs.
class Vsftpd234BackdoorTest < PostProvisionTest
  def initialize
    self.module_name = 'vsftpd_234_backdoor'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super

    # Tier 1 — the self-compiled backdoored binary is in place.
    test_command_succeeds('vsftpd 2.3.4 installed', 'test -x /usr/local/sbin/vsftpd', tier: 1)

    # Tier 2 — FTP daemon listening on the target.
    test_service_up(tier: 2)

    # Tier 3 — exploit from the attacker VM via a no-session fetch payload.
    test_msf_exploit('exploit/unix/ftp/vsftpd_234_backdoor',
                     rport: 21,
                     payload: 'cmd/linux/http/x64/exec',
                     force: true,
                     tier: 3)
  end
end

Vsftpd234BackdoorTest.new.run
