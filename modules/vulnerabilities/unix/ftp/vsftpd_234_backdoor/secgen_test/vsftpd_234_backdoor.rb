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
# the first to use its session mode: the vsftpd backdoor opens a root shell on
# port 6200, so the Metasploit module uses a shell-session payload
# (cmd/unix/interact) rather than distcc's no-session cmd/unix/generic. Passing
# session: true makes the helper open the session, run the proof-of-RCE sentinel
# command inside it (sessions -c) and tear it down; RCE is proven the same way
# as distcc, by a sentinel the target's own guest agent reads back. With no
# attacker system (e.g. a single-VM module run) tier 3 SKIPs.
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

    # Tier 3 — exploit from the attacker VM via the session-payload path.
    test_msf_exploit('exploit/unix/ftp/vsftpd_234_backdoor',
                     rport: 21,
                     session: true,
                     tier: 3)
  end
end

Vsftpd234BackdoorTest.new.run
