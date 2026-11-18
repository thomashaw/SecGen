require_relative '../../../../../lib/post_provision_test'


class SudoEnvKeepLdPreloadTest < PostProvisionTest
  def initialize
    self.module_name = 'sudo_env_keep_ld_preload'
    self.module_path = get_module_path(__FILE__)
    super
  end

  # Inputs: leaked_filenames and strings_to_leak are both checked below.
  def test_module
    super
    leaked = Array(self.json_inputs['leaked_filenames']).map(&:to_s).reject(&:empty?)
    strings = Array(self.json_inputs['strings_to_leak']).map(&:to_s)

    tier(1) do
      test_local_command('env_keep default keeps LD_PRELOAD',
                         'cat /etc/sudoers.d/10_ld_preload_env_keep', 'Defaults env_keep += "LD_PRELOAD"')
      test_local_command('sudoers rule allows the status script as root with no password',
                         'cat /etc/sudoers.d/10_ld_preload_status_script',
                         'ALL  ALL=(root) NOPASSWD: /usr/local/bin/system_status')
      test_local_command('sudoers rule allows sudo -l with no password',
                         'cat /etc/sudoers.d/10_ld_preload_sudo_list', 'ALL  ALL=(root) NOPASSWD: /usr/bin/sudo -l')
      test_local_command('sudoers configuration passes visudo -c', 'visudo -c 2>&1', 'parsed OK')
      test_local_command('status script root-owned and executable',
                         "stat -c '%U:%G %a' /usr/local/bin/system_status", 'root:root 755')

      leaked.each do |f|
        test_local_command("#{f} root-only in /root", "stat -c '%U:%G %a' #{Shellwords.escape("/root/#{f}")}", 'root:root 600')
      end
    end

    tier(2) do
      # what a student sees: sudo -l lists LD_PRELOAD as a kept variable and the allowed command
      test_local_command('unprivileged user sees LD_PRELOAD kept by sudo', 'sudo -n -l', 'env_keep+="LD_PRELOAD"', user: 'nobody')
      test_local_command('unprivileged user sees the allowed root command', 'sudo -n -l', '/usr/local/bin/system_status', user: 'nobody')
      test_command_succeeds('unprivileged user runs the status script as root',
                            'sudo -n /usr/local/bin/system_status', user: 'nobody')
      # the flag is not readable without escalating
      strings.each_with_index do |s, i|
        next if s.strip.empty?
        fname = leaked[i] || leaked.first
        next unless fname
        test_command_succeeds("unprivileged user cannot read leaked #{fname} directly",
                              "! cat #{Shellwords.escape("/root/#{fname}")} 2>/dev/null", user: 'nobody')
      end
    end

    # TODO tier 3 (exploit): hand-written — throwaway user preloads its own shared library through sudo's kept LD_PRELOAD (GTFOBins sudo "LD_PRELOAD") and reads the root-only flag; build the library on the attacker VM if the target has no compiler
  end

end

SudoEnvKeepLdPreloadTest.new.run
