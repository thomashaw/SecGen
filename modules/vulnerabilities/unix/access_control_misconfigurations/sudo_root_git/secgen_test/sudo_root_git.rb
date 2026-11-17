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
    # TODO tier 3 (exploit): hand-written — with_throwaway_ssh_user, then as that user run the GTFOBins git sudo technique and read the root-only flag
  end

end

SudoRootGitTest.new.run
