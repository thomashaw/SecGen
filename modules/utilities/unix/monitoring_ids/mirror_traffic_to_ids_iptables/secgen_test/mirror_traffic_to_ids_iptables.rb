require_relative '../../../../../lib/post_provision_test'

class MirrorTrafficToIdsIptablesTest < PostProvisionTest
  def initialize
    self.module_name = 'mirror_traffic_to_ids_iptables'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    ids_ip = json_inputs['ids_IP_address']&.first
    tier(1) do
      test_local_command('iptables-persistent installed?', 'dpkg -l iptables-persistent | grep -E "^ii"', 'iptables-persistent')
      test_command_succeeds('rules.v4 saved', 'test -s /etc/iptables/rules.v4')
      test_local_command('netfilter-persistent enabled at boot', 'systemctl is-enabled netfilter-persistent', 'enabled')
    end
    tier(2) do
      test_command_succeeds('netfilter-persistent service active', 'systemctl is-active netfilter-persistent')
      if ids_ip
        # ids_IP_address: the TEE rule actually mirrors to the configured IDS host, not just any gateway
        test_local_command('TEE rule in mangle PREROUTING targets ids_IP_address',
                            "iptables -t mangle -L PREROUTING -n | grep TEE", "gateway:#{ids_ip}")
        test_local_command('TEE rule in mangle FORWARD targets ids_IP_address',
                            "iptables -t mangle -L FORWARD -n | grep TEE", "gateway:#{ids_ip}")
        test_local_command('TEE rule persisted to rules.v4',
                            'cat /etc/iptables/rules.v4', "--gateway #{ids_ip}")
      else
        test_local_command('TEE rule present in mangle PREROUTING', 'iptables -t mangle -L PREROUTING | grep TEE', 'TEE')
        test_local_command('TEE rule present in mangle FORWARD', 'iptables -t mangle -L FORWARD | grep TEE', 'TEE')
      end
    end
  end
end

MirrorTrafficToIdsIptablesTest.new.run
