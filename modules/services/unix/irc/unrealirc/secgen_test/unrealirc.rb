require_relative '../../../../../lib/post_provision_test'

class UnrealircTest < PostProvisionTest
  def initialize
    self.module_name = 'unrealirc'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      # Built from source to a custom path, so check the binary directly.
      test_command_succeeds('unrealirc binary present', 'test -x /var/lib/unreal/unreal')
      test_command_succeeds('unrealirc config present', 'test -f /var/lib/unreal/unrealircd.conf')
      test_local_command('irc user exists', 'id irc', 'irc')
    end
    tier(2) do
      test_service_up
      # IRC protocol: server sends a colon-prefixed message on connect without waiting.
      test_banner(':')
    end
  end
end

UnrealircTest.new.run
