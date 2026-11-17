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
    # TODO tier 3 (exploit): hand-written — with_throwaway_ssh_user, then the GTFOBins perl sudo technique as that user reads the root-only flag
  end

end

SudoRootPerlTest.new.run
