require_relative '../../../../../lib/post_provision_test'


class SudoRootFindTest < PostProvisionTest
  def initialize
    self.module_name = 'sudo_root_find'
    self.module_path = get_module_path(__FILE__)
    super
  end

  # Inputs: leaked_filenames and strings_to_leak are both checked below.
  def test_module
    super
    leaked = Array(self.json_inputs['leaked_filenames']).map(&:to_s).reject(&:empty?)
    strings = Array(self.json_inputs['strings_to_leak']).map(&:to_s)

    tier(1) do
      test_local_command('sudoers rule file root-owned 0440', "stat -c '%U:%G %a' /etc/sudoers.d/10_users_sudo_find", 'root:root 440')
      test_local_command('sudoers rule allows find as root with no password',
                         'cat /etc/sudoers.d/10_users_sudo_find', 'ALL  ALL=(root) NOPASSWD: /bin/find *, /usr/bin/find *')
      test_local_command('sudoers rule allows sudo -l with no password',
                         'cat /etc/sudoers.d/10_users_sudo_list_find', 'ALL  ALL=(root) NOPASSWD: /usr/bin/sudo -l')
      test_local_command('sudoers syntax is valid', 'visudo -c 2>&1', 'parsed OK')
      test_command_succeeds('find is a real binary at /usr/bin/find', 'test -x /usr/bin/find')

      leaked.each do |f|
        test_local_command("#{f} root-only in /root", "stat -c '%U:%G %a' #{Shellwords.escape("/root/#{f}")}", 'root:root 600')
      end
    end

    tier(2) do
      # what a student sees with sudo -l
      test_local_command('unprivileged user can list the find sudo rule', 'sudo -n -l', '/usr/bin/find *', user: 'nobody')
      # the flag is not readable without escalating
      strings.each_with_index do |s, i|
        next if s.strip.empty?
        fname = leaked[i] || leaked.first
        next unless fname
        test_command_succeeds("unprivileged user cannot read leaked #{fname} directly",
                              "! cat #{Shellwords.escape("/root/#{fname}")} 2>/dev/null", user: 'nobody')
      end
    end

    # TODO tier 3 (exploit): hand-written — with_throwaway_ssh_user runs the GTFOBins find sudo technique (find -exec) as root, check uid=0 and the root-only flag
  end

end

SudoRootFindTest.new.run
