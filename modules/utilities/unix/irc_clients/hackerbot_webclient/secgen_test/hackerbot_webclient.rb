require_relative '../../../../../lib/post_provision_test'

class HackerbotWebclientTest < PostProvisionTest
  def initialize
    self.module_name = 'hackerbot_webclient'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_local_command('python3 installed?', 'which python3', '/usr/bin/python3')
      test_local_command('/opt/hackerbot_webclient directory exists?', 'test -d /opt/hackerbot_webclient && echo ok', 'ok')
    end
    tier(2) { test_command_succeeds('python3 runs', 'python3 --version') }
  end
end

HackerbotWebclientTest.new.run
