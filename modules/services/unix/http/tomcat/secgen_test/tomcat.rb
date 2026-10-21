require_relative '../../../../../lib/post_provision_test'

class TomcatTest < PostProvisionTest
  def initialize
    self.module_name = 'tomcat'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) { test_local_command('tomcat installed?', 'test -d /opt/tomcat', '/opt/tomcat') }
    tier(1) { test_local_command('catalina.sh exists?', 'test -x /opt/tomcat/bin/catalina.sh && echo ok', 'ok') }
    tier(2) { test_service_up(port: 8080) }
    tier(2) { test_http('/', port: 8080) }
  end
end

TomcatTest.new.run
