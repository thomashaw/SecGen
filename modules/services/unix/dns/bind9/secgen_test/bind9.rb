require_relative '../../../../../lib/post_provision_test'

class Bind9Test < PostProvisionTest
  def initialize
    self.module_name = 'bind9'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) { test_local_command('bind9 installed?', 'dpkg -l bind9 | grep -E "^ii"', 'bind9') }
    tier(2) { test_local_command('named running?', 'ps -ef | grep named', 'named') }
    tier(2) { test_service_up(port: 53) }
  end
end

Bind9Test.new.run
