require_relative '../../../../../lib/post_provision_test'

class KibanaTest < PostProvisionTest
  def initialize
    self.module_name = 'kibana'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) { test_local_command('kibana installed?', 'dpkg -l kibana | grep -E "^ii"', 'kibana') }
    tier(2) { test_service_up(port: 5601) }
    tier(2) { test_http('/', port: 5601) }
  end
end

KibanaTest.new.run
