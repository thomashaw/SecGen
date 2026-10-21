require_relative '../../../../../lib/post_provision_test'

class SambaTest < PostProvisionTest
  def initialize
    self.module_name = 'samba'
    self.module_path = get_module_path(__FILE__)
    super
    self.port = 139
  end

  def test_module
    super
    tier(1) { test_local_command('samba installed?', 'dpkg -l samba | grep -E "^ii"', 'samba') }
    tier(1) { test_local_command('smbd active?', 'systemctl is-active smbd | grep -x active', 'active') }
    tier(2) { test_service_up }
  end
end

SambaTest.new.run
