require_relative '../../../../../lib/post_provision_test'


class SudoRootTeeTest < PostProvisionTest
  def initialize
    self.module_name = 'sudo_root_tee'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    # Inputs: strings_to_leak and leaked_filenames are both checked below (flag file
    # present and root-only in tier 1, content readable via the sudo rule in tier 2).
    leaked = Array(self.json_inputs['leaked_filenames']).map(&:to_s).reject(&:empty?)
    strings = Array(self.json_inputs['strings_to_leak']).map(&:to_s)

    tier(1) do
      test_local_command('sudoers rule file root-owned 0440', "stat -c '%U:%G %a' /etc/sudoers.d/10_users_sudo_tee", 'root:root 440')
      test_local_command('sudoers rule allows tee as root with no password',
                         'cat /etc/sudoers.d/10_users_sudo_tee', 'ALL  ALL=(root) NOPASSWD: /bin/tee *, /usr/bin/tee *')
      test_local_command('sudoers rule allows sudo -l with no password',
                         'cat /etc/sudoers.d/10_users_sudo_list', 'ALL  ALL=(root) NOPASSWD: /usr/bin/sudo -l')
      # visudo -c prints "<file>: parsed OK" for each file it checks
      test_local_command('sudoers configuration parses', 'visudo -c 2>&1', 'parsed OK')

      leaked.each do |f|
        test_local_command("#{f} root-only in /root", "stat -c '%U:%G %a' #{Shellwords.escape("/root/#{f}")}", 'root:root 600')
      end
    end

    tier(2) do
      # what a student sees first: sudo -l shows the tee rule
      test_local_command('unprivileged user can list the tee sudo rule', 'sudo -n -l', '/usr/bin/tee *', user: 'nobody')
      # the misconfiguration: tee run through sudo writes as root. The unprivileged user writes a
      # probe file; ownership is then checked as root (nobody cannot stat inside /root, which is
      # 0700) and the probe removed so nothing is left behind.
      probe = '/root/.secgen_tee_probe'
      write = run_as_user('nobody', "echo probe | sudo -n /usr/bin/tee #{probe} >/dev/null")
      self.outputs.push write[:stderr] unless write[:stderr].to_s.empty?
      test_local_command('tee run via sudo by an unprivileged user wrote a root-owned file', "stat -c '%U' #{probe}", 'root')
      run_vagrant_ssh("rm -f #{probe}")
      # the flag itself stays unreadable to an unprivileged user
      leaked.each do |f|
        test_local_command("unprivileged user cannot read #{f} directly",
                           "cat #{Shellwords.escape("/root/#{f}")} 2>&1 || true", 'Permission denied', user: 'nobody')
      end
    end

    # TODO tier 3 (exploit): hand-written — as a throwaway user, use the GTFOBins tee sudo technique to write a sudoers drop-in, read the root-only flag, then remove the drop-in
  end

end

SudoRootTeeTest.new.run
