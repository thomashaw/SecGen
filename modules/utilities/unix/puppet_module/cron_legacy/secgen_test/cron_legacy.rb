require_relative '../../../../../lib/post_provision_test'

class CronLegacyTest < PostProvisionTest
  def initialize
    self.module_name = 'cron_legacy'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_local_command('cron package installed?', 'dpkg -l cron | grep -E "^ii"', 'cron')
      test_local_command('crontab binary present', 'which crontab', 'crontab')
    end
    tier(2) { test_command_succeeds('cron service running', 'systemctl is-active cron') }
  end
end

CronLegacyTest.new.run
