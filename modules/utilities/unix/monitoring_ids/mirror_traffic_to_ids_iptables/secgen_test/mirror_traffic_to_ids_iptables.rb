require_relative '../../../../../lib/post_provision_test'

class MirrorTrafficToIdsIptablesTest < PostProvisionTest
  def initialize
    self.module_name = 'mirror_traffic_to_ids_iptables'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) { test_local_command('iptables-persistent installed?', 'dpkg -l iptables-persistent | grep -E "^ii"', 'iptables-persistent') }
    tier(2) do
      # TEE rule in the mangle PREROUTING chain copies incoming traffic to the IDS host
      test_local_command('TEE rule present in mangle PREROUTING', 'iptables -t mangle -L PREROUTING | grep TEE', 'TEE')
    end
  end
end

MirrorTrafficToIdsIptablesTest.new.run
