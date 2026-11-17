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

    # TODO tier 3 (exploit): hand-written — with_throwaway_ssh_user, GTFOBins python3 SUID (os.execl sh -p) reads the root-only flag
  end

end

SUIDRootPython3Test.new.run
