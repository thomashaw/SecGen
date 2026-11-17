require_relative '../../../../../lib/post_provision_test'


class CapDacReadSearchTarTest < PostProvisionTest
  def initialize
    self.module_name = 'cap_dac_read_search_tar'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    # Inputs: leaked_filenames and strings_to_leak are both checked below
    # (file name, owner/mode and content). Nothing is skipped.
    leaked = Array(self.json_inputs['leaked_filenames']).map(&:to_s).reject(&:empty?)
    strings = Array(self.json_inputs['strings_to_leak']).map(&:to_s)

    tier(1) do
      test_local_command('tar has cap_dac_read_search', '/usr/sbin/getcap /usr/bin/tar', 'cap_dac_read_search=ep')
      leaked.each do |f|
        test_local_command("#{f} root-only in /root", "stat -c '%U:%G %a' #{Shellwords.escape("/root/#{f}")}", 'root:root 600')
      end
      strings.each_with_index do |s, i|
        next if s.strip.empty?
        fname = leaked[i] || leaked.first
        next unless fname
        test_local_command("leaked #{fname} holds expected string", "sudo cat #{Shellwords.escape("/root/#{fname}")}", s.strip.lines.first.to_s.strip)
      end
    end

    tier(2) do
      # what a student sees: getcap lists tar with the capability
      test_local_command('unprivileged user finds tar via getcap', '/usr/sbin/getcap -r /usr/bin 2>/dev/null', '/usr/bin/tar', user: 'nobody')
      leaked.each_with_index do |f, i|
        path = Shellwords.escape("/root/#{f}")
        # control: a plain read is refused, so any success below is down to the capability
        test_local_command("unprivileged user cannot cat #{f} directly", "cat #{path} 2>&1", 'Permission denied', user: 'nobody')
        s = strings[i].to_s.strip.lines.first.to_s.strip
        next if s.empty?
        # the misconfiguration: archive the root-only file and extract it to stdout
        test_local_command("unprivileged user reads #{f} via tar", "tar -cf - #{path} 2>/dev/null | tar -xOf - 2>/dev/null", s, user: 'nobody')
      end
    end
    # TODO tier 3 (exploit): hand-written — with_throwaway_ssh_user, tar cap_dac_read_search technique reads the root-only flag
  end

end

CapDacReadSearchTarTest.new.run
