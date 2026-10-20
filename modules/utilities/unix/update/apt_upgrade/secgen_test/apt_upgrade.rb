require_relative '../../../../../lib/post_provision_test'

class AptUpgradeTest < PostProvisionTest
  def initialize
    self.module_name = 'apt_upgrade'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) { test_local_command('apt binary present', 'which apt-get', 'apt-get') }
    # apt-get upgrade runs idempotently; after provisioning the system should be at latest versions
    tier(2) { test_command_succeeds('apt-get check passes', 'apt-get check') }
  end
end

AptUpgradeTest.new.run
