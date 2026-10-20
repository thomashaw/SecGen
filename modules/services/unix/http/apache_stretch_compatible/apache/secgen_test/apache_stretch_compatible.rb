require_relative '../../../../../lib/post_provision_test'

class ApacheStretchCompatibleTest < PostProvisionTest
  def initialize
    self.module_name = 'apache_stretch_compatible'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) { test_local_command('apache2 installed?', 'dpkg -l apache2 | grep -E "^ii"', 'apache2') }
    tier(2) { test_service_up(port: 80) }
    tier(2) { test_http('/', port: 80) }
  end
end

ApacheStretchCompatibleTest.new.run
