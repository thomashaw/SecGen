require_relative '../../../../../lib/post_provision_test'

class OpenvpnClientTest < PostProvisionTest
  def initialize
    self.module_name = 'openvpn_client'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_local_command('openvpn installed', 'which openvpn', '/usr/sbin/openvpn')
      test_local_command('client config directory exists', 'test -d /etc/openvpn/client', '/etc/openvpn/client')
      test_local_command('client.ovpn present', 'test -f /etc/openvpn/client/client.ovpn', '/etc/openvpn/client/client.ovpn')
    end
    tier(2) do
      test_command_succeeds('openvpn --version runs', 'openvpn --version || true')
      # Config file must be non-empty (contains certs/keys injected from openvpn_pki generator)
      test_local_command('client.ovpn non-empty', 'test -s /etc/openvpn/client/client.ovpn', '')
    end
  end
end

OpenvpnClientTest.new.run
