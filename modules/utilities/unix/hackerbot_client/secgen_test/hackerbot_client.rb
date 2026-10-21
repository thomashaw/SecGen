require_relative '../../../../../lib/post_provision_test'

class HackerbotClientTest < PostProvisionTest
  def initialize
    self.module_name = 'hackerbot_client'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_local_command('/root/.ssh directory exists', 'test -d /root/.ssh', '/root/.ssh')
      test_local_command('authorized_keys present', 'test -f /root/.ssh/authorized_keys', '/root/.ssh/authorized_keys')
    end
    tier(2) do
      # The file must be non-empty and contain a public key injected by hackerbot
      test_local_command('authorized_keys non-empty', 'test -s /root/.ssh/authorized_keys', '')
      test_local_command('authorized_keys has ssh key', "grep -qE '^(ssh-rsa|ssh-ed25519|ecdsa)' /root/.ssh/authorized_keys", '')
    end
  end
end

HackerbotClientTest.new.run
