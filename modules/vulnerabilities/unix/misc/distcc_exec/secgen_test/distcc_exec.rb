require_relative '../../../../../lib/post_provision_test'

# Post-provision test for the DistCC Daemon Command Execution vulnerability
# (CVE-2004-2687, exploit/unix/misc/distcc_exec).
#
# Tiers:
#   1  distccd is installed on the target.
#   2  the distcc daemon is listening (port 3632).
#   3  a real exploit from the attacker VM achieves remote command execution.
#
# Tier 3 is the first exploit test driven from a second VM (Phase 1C). It uses
# the framework helper test_msf_exploit, which runs Metasploit on the project's
# attacker VM (any sibling whose base is type 'attack', e.g. Kali/MSF) against
# this target and proves RCE via a sentinel the target-side guest agent reads
# back. With no attacker system (e.g. a single-VM module run) tier 3 SKIPs, so
# the module test still works on its own.
class DistCCExecTest < PostProvisionTest
  def initialize
    self.module_name = 'distcc_exec'
    self.module_path = get_module_path(__FILE__)
    super
    self.port = 3632
  end

  def test_module
    super

    # Tier 1 — installed on the target.
    test_command_succeeds('distccd installed', 'command -v distccd', tier: 1)

    # Tier 2 — daemon listening on the target.
    test_service_up(tier: 2)

    # Tier 3 — exploit from the attacker VM; also captures the leaked flag
    # from /home/distccd into the sentinel as evidence.
    test_msf_exploit('exploit/unix/misc/distcc_exec',
                     rport: 3632,
                     collect: 'cat /home/distccd/*',
                     tier: 3)
  end
end

DistCCExecTest.new.run
