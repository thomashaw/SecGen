require_relative '../../../../../lib/post_provision_test'

class OpenvpnServerTest < PostProvisionTest
  def initialize
    self.module_name = 'openvpn_server'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_local_command('openvpn installed?', 'dpkg -l openvpn | grep -E "^ii"', 'openvpn')
      test_local_command('PKI and config files present?',
                         'cd /etc/openvpn/server && test -s ca.crt && test -s server.crt && test -s server.key && test -s dh.pem && test -s server.conf && echo ok',
                         'ok')
      test_local_command('server.key is root-only?', 'stat -c %a /etc/openvpn/server/server.key', '600')
    end
    tier(2) do
      test_command_succeeds('openvpn binary works', 'openvpn --version')
      test_command_succeeds('server cert chains to CA',
                            'openssl verify -CAfile /etc/openvpn/server/ca.crt /etc/openvpn/server/server.crt')
      # Test scenario sets start_on_boot=true, so the unit must be running and bound to UDP.
      test_local_command('openvpn-server@server active?', 'systemctl is-active openvpn-server@server', 'active')
      test_local_command('openvpn listening on UDP 1194?', 'ss -lunp | grep openvpn', ':1194')
    end
  end
end

OpenvpnServerTest.new.run
