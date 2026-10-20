require_relative '../../../../../lib/post_provision_test'

class HandyNetworkingToolsTest < PostProvisionTest
  def initialize
    self.module_name = 'handy_networking_tools'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_local_command('curl installed', 'which curl', 'curl')
      test_local_command('tcpdump installed', 'which tcpdump', 'tcpdump')
      test_local_command('openssl installed', 'which openssl', 'openssl')
    end
    tier(2) do
      test_command_succeeds('curl runs', 'curl --version')
      test_command_succeeds('openssl runs', 'openssl version')
      test_local_command('traceroute available', 'which traceroute', 'traceroute')
    end
  end
end

HandyNetworkingToolsTest.new.run
