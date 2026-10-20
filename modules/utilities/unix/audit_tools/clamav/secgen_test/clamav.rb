require_relative '../../../../../lib/post_provision_test'

class ClamavTest < PostProvisionTest
  def initialize
    self.module_name = 'clamav'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) { test_local_command('clamscan installed?', 'which clamscan', 'clamscan') }
    tier(2) { test_command_succeeds('clamscan reports version', 'clamscan --version') }
  end
end

ClamavTest.new.run
