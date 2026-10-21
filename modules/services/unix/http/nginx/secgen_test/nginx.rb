require_relative '../../../../../lib/post_provision_test'

class NginxTest < PostProvisionTest
  def initialize
    self.module_name = 'nginx'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) { test_local_command('nginx installed?', 'dpkg -l nginx | grep -E "^ii"', 'nginx') }
    tier(2) { test_service_up(port: 80) }
    tier(2) { test_http('/', port: 80) }
  end
end

NginxTest.new.run
