require_relative '../../../../../lib/post_provision_test'
require 'shellwords'

class CronPathHijackTest < PostProvisionTest
  # Account with no special rights, standing in for the student.
  LOW_PRIV_USER = 'nobody'
  CRON_FILE = '/etc/cron.d/cron_path_hijack'
  WRITABLE_DIR = '/usr/local/cron-bin'

  def initialize
    self.module_name = 'cron_path_hijack'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super

    # Inputs: leaked_filenames and strings_to_leak are both checked below. The
    # module has no other read_facts.
    filenames = (json_inputs['leaked_filenames'] || []).map(&:to_s).reject(&:empty?)
    strings   = json_inputs['strings_to_leak'] || []

    tier(1) do
      test_local_command('writable directory is first in the cron PATH', "grep '^PATH=' #{CRON_FILE}", "PATH=#{WRITABLE_DIR}:")
      test_local_command('root runs the job by bare name', "grep -v '^PATH=' #{CRON_FILE}", '* * * * * root backup-check')
      test_local_command('real script lives later in PATH', 'stat -c "%a %U" /usr/local/sbin/backup-check', '755 root')
      test_local_command('writable directory is world-writable', "stat -c '%a' #{WRITABLE_DIR}", '777')

      # leak_files pairs strings with filenames; extra strings go in the first file.
      strings.each_with_index do |str, i|
        file = filenames[i] || filenames.first
        next unless file
        path = Shellwords.escape("/root/#{file}")
        test_local_command("#{file} root-only in /root", "stat -c '%U:%G %a' #{path}", 'root:root 600')
        test_local_command("leaked string #{i + 1} is in /root/#{file}", "cat #{path}", str.to_s.strip.lines.first.to_s.strip)
      end
    end

    tier(2) do
      # The misconfiguration as a student sees it: read the crontab and write into the first PATH entry.
      test_local_command('unprivileged user can read the crontab', "cat #{CRON_FILE}", "PATH=#{WRITABLE_DIR}:", user: LOW_PRIV_USER)
      test_command_succeeds('unprivileged user can create files in the PATH directory',
                            "f=#{WRITABLE_DIR}/.secgen_probe_$$; : > $f && rm -f $f", user: LOW_PRIV_USER)
      # The flag is not readable without escalating.
      filenames.each do |f|
        test_command_succeeds("unprivileged user cannot read /root/#{f}",
                              "! cat #{Shellwords.escape("/root/#{f}")} 2>/dev/null", user: LOW_PRIV_USER)
      end
      # Cron is running and loaded the job: the legitimate script logs each run.
      found = false
      8.times do
        if run_command('test -s /var/log/backup-check.log && echo ran')[:stdout].include?('ran')
          found = true
          break
        end
        sleep 20
      end
      if found
        pass_check('cron runs the job from cron.d', 'backup-check ran under cron')
      else
        fail_check('cron runs the job from cron.d', '/var/log/backup-check.log empty after 160s',
                   evidence: run_command("systemctl is-active cron; cat #{CRON_FILE}; journalctl -u cron -n 20 --no-pager 2>&1")[:stdout])
      end
    end

    # TODO tier 3 (exploit): hand-written, as a throwaway SSH user plant an executable named backup-check in the writable PATH directory, wait for cron with a sentinel, read the root-only flag, then remove the planted file
  end
end

CronPathHijackTest.new.run
