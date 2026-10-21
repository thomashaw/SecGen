require_relative '../../../../../lib/post_provision_test'

class OpenvpnServerTest < PostProvisionTest
  def initialize
    self.module_name = 'openvpn_server'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) { test_local_command('openvpn installed?', 'dpkg -l openvpn | grep -E "^ii"', 'openvpn') }
    tier(1) { test_local_command('server.conf present?', 'test -f /etc/openvpn/server/server.conf && echo ok', 'ok') }
    tier(2) { test_local_command('openvpn process running?', 'ps -ef | grep openvpn', 'openvpn') }
    # OpenVPN listens on UDP 1194 by default; TCP socket check may not apply
    tier(2) { test_command_succeeds('openvpn binary works', 'openvpn --version') }
  end
end

OpenvpnServerTest.new.run
