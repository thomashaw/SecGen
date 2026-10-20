require_relative '../../../../../lib/post_provision_test'

class SecurityShepherdTest < PostProvisionTest
  def initialize
    self.module_name = 'security_shepherd'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_local_command('tomcat9 installed', 'dpkg -l tomcat9 | grep -E "^ii"', 'tomcat9')
      test_local_command('shepherd ROOT deployed', 'test -d /var/lib/tomcat9/webapps/ROOT/admin && echo ok', 'ok')
    end
    tier(2) do
      test_service_up(port: 8080)
      test_http('/', port: 8080)
    end
    # TODO tier 3 (exploit): deferred — no automated MSF module; manual credential-based attack
  end
end

SecurityShepherdTest.new.run
