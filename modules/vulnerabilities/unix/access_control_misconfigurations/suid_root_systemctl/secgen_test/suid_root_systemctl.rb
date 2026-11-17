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

    # TODO tier 3 (exploit): hand-written — with_throwaway_ssh_user runs the GTFOBins systemctl SUID technique (link + start a unit) to read the root-only flag, then disables and removes the unit
  end

end

SuidRootSystemctlTest.new.run
