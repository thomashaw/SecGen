require_relative '../../../../../lib/post_provision_test'
require 'shellwords'

class WritableShadowTest < PostProvisionTest
  LEAK_DIR = '/root'.freeze # storage_directory in manifests/config.pp
  LOW_PRIV_USER = 'nobody'.freeze # exists on every base, not root, not a sudoer

  def initialize
    self.module_name = 'writable_shadow'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super

    # Inputs: strings_to_leak and leaked_filenames are both checked below
    # (leak_files pairs string N with filename N, and appends any surplus
    # strings to the first file).
    strings   = self.json_inputs['strings_to_leak'] || []
    filenames = self.json_inputs['leaked_filenames'] || []

    # Tier 1: /etc/shadow has the 0777 mode, still owned by root, and the
    # leaked files exist under /root with their strings.
    tier(1) do
      test_local_command('/etc/shadow mode is 777', "stat -c '%a' /etc/shadow", '777')
      test_local_command('/etc/shadow still owned by root', "stat -c '%U' /etc/shadow", 'root')

      filenames.reject(&:empty?).each do |name|
        test_command_succeeds("leaked file #{name} exists", "test -f #{Shellwords.escape("#{LEAK_DIR}/#{name}")}", tier: 1)
      end

      unless filenames.reject(&:empty?).empty?
        strings.each_with_index do |str, i|
          first_line = str.to_s.lines.first.to_s.chomp
          next if first_line.empty?
          name = filenames[i].to_s.empty? ? filenames.first : filenames[i]
          path = Shellwords.escape("#{LEAK_DIR}/#{name}")
          test_command_succeeds("leaked string #{i} is in #{name}",
                                "grep -qF -- #{Shellwords.escape(first_line)} #{path}", tier: 1)
        end
      end
    end

    # Tier 2: an unprivileged account can write /etc/shadow (without changing
    # it: appending nothing opens the file for writing and leaves it intact).
    tier(2) do
      uid = run_as_user(LOW_PRIV_USER, 'id -u')[:stdout].to_s.strip
      if uid =~ /\A[1-9]\d*\z/
        pass_check("#{LOW_PRIV_USER} is unprivileged", "#{LOW_PRIV_USER} has uid #{uid}", tier: 2)
      else
        fail_check("#{LOW_PRIV_USER} is unprivileged", "expected a non-zero uid, got '#{uid}'", tier: 2)
      end
      test_command_succeeds("#{LOW_PRIV_USER} can open /etc/shadow for writing", ': >> /etc/shadow', user: LOW_PRIV_USER, tier: 2)
      test_command_succeeds("#{LOW_PRIV_USER} can read /etc/shadow", 'head -c1 /etc/shadow >/dev/null', user: LOW_PRIV_USER, tier: 2)
    end

    # Tier 3 (exploit) is not part of this issue: it stays absent.
  end
end

WritableShadowTest.new.run
