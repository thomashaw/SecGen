require_relative '../../../../../lib/post_provision_test'

class HackerbotTest < PostProvisionTest
  def initialize
    self.module_name = 'hackerbot'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_local_command('hackerbot directory exists', 'test -d /opt/hackerbot', '/opt/hackerbot')
      test_local_command('hackerbot.rb present', 'test -f /opt/hackerbot/hackerbot.rb', '/opt/hackerbot/hackerbot.rb')
      test_local_command('cinch gem installed', 'gem list | grep -q "^cinch"', '')
      test_local_command('nokogiri gem installed', 'gem list | grep -q "^nokogiri"', '')
    end
    tier(2) do
      # Hackerbot runs as a systemd service; it connects outbound via IRC so there is no inbound port to probe
      test_command_succeeds('hackerbot service enabled', 'systemctl is-enabled hackerbot')
      test_local_command('hackerbot service unit present', 'test -f /etc/systemd/system/hackerbot.service', '/etc/systemd/system/hackerbot.service')
    end
  end
end

HackerbotTest.new.run
