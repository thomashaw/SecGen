require_relative '../../../../../lib/post_provision_test'

class ChkrootkitVulnTest < PostProvisionTest
  def initialize
    self.module_name = 'chkrootkit'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_local_command('Chkrootkit binary exists?', 'sudo ls -la /usr/sbin/chkrootkit', 'chkrootkit-0.49')

      # leaked_filenames/strings_to_leak: leak_files writes these into /root, where
      # the local privilege-escalation payload (running as root via cron) can read them.
      self.json_inputs['leaked_filenames']&.each_with_index do |filename, i|
        leaked_string = self.json_inputs['strings_to_leak']&.at(i)
        leak_path = "/root/#{filename}"
        test_local_command("leaked file #{filename} present", "sudo test -f #{leak_path} && echo ok", 'ok')
        if leaked_string
          test_local_command("leaked file #{filename} contains expected string", "sudo cat #{leak_path}", leaked_string)
        end
      end

      # cron_frequency: confirm root's crontab was configured to run chkrootkit at the
      # chosen frequency (this is what makes the privilege escalation exploitable without
      # a long wait).
      minute_frequency = case self.json_inputs['cron_frequency']&.first
                          when '5_minutes' then '*/5'
                          when '15_minutes' then '*/15'
                          when '30_minutes' then '*/30'
                          else '*'
                          end
      test_local_command('root cron job for chkrootkit configured',
                          'sudo crontab -u root -l',
                          "#{minute_frequency} * * * * /usr/sbin/chkrootkit")
    end

    tier(2) do
      test_local_command('Chkrootkit runs?', 'sudo /usr/sbin/chkrootkit -V', 'chkrootkit version 0.49')
    end

    # Tier 3 (exploit/unix/local/chkrootkit) is a LOCAL privilege-escalation module: it needs
    # an existing low-privilege session on the target, not a remote RHOSTS call, so the
    # driver's generate-t3-test declines it and this stays at tier 2 for the automated pipeline.
    # TODO tier 3 (exploit): deferred — wire up test_msf_exploit when attacker VM is available
    # msf_module: exploit/unix/local/chkrootkit
  end

end
ChkrootkitVulnTest.new.run