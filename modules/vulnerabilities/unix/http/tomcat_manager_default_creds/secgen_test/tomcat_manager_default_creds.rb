require_relative '../../../../../lib/post_provision_test'

class TomcatManagerDefaultCredsTest < PostProvisionTest
  # The connector is fixed at 8080 by the required tomcat service module, which
  # this module does not reconfigure, so port is not a read_fact here.
  TOMCAT_PORT = 8080

  def initialize
    self.module_name = 'tomcat_manager_default_creds'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super

    # read_facts: tomcat_manager_username / tomcat_manager_password drive the
    # weak account written into tomcat-users.xml and used to authenticate below.
    username = (json_inputs['tomcat_manager_username'] || ['tomcat']).first
    password = (json_inputs['tomcat_manager_password'] || ['tomcat']).first

    users_xml   = '/opt/tomcat/conf/tomcat-users.xml'
    manager_ctx = '/opt/tomcat/webapps/manager/META-INF/context.xml'

    tier(1) do
      test_local_command('manager app present', 'test -d /opt/tomcat/webapps/manager && echo ok', 'ok')
      test_file_contains(users_xml, 'manager-gui', label: 'manager-gui role configured')
      test_file_contains(users_xml, "username=\"#{username}\"", label: 'weak manager user configured')
      # The stock localhost-only RemoteAddrValve must be gone for remote reach.
      # Match the actual <Valve className="...RemoteAddrValve"> element, not any
      # mention of the name in a comment, so the check reflects the real config.
      test_local_command('RemoteAddrValve removed from manager context',
                         "grep -Eq 'className=\"[^\"]*RemoteAddrValve\"' #{manager_ctx} && echo present || echo absent", 'absent')
    end

    tier(2) do
      test_service_up(port: TOMCAT_PORT)
      # the Manager app enforces auth: no credentials => 401
      test_http('/manager/html', status: 401, port: TOMCAT_PORT, label: 'manager requires authentication')
      # and the weak account authenticates (manager-script text interface)
      test_local_command('weak manager creds authenticate',
                         "curl -s -o /dev/null -w '%{http_code}' -u #{username}:#{password} " \
                         "http://localhost:#{TOMCAT_PORT}/manager/text/serverinfo",
                         '200')
    end

    # Tier 3: prove the misconfiguration with the stock MSF auxiliary module.
    # run_msf_module runs it from the attacker VM (RHOSTS set automatically) and
    # returns :msf_output. The assertion below is a generic success heuristic (an
    # MSF "[+]" finding line). TODO t3-worker: tighten it to a module-specific
    # token from the msf_output_tomcat_mgr_login evidence, and add any options the module
    # needs (credentials, RPORT) from the module's json_inputs.
    tier(3) do
      aux = run_msf_module('auxiliary/scanner/http/tomcat_mgr_login',
                           options: {},
                           label: 'tomcat_manager_default_creds')
      next unless aux

      out = aux[:msf_output].to_s
      if out.include?('[+]')
        pass_check('tomcat_manager_default_creds proves the misconfiguration',
                   "auxiliary/scanner/http/tomcat_mgr_login on #{aux[:attacker]} reported a finding against #{aux[:rhost]}",
                   tier: 3)
      else
        fail_check('tomcat_manager_default_creds proves the misconfiguration',
                   'No MSF "[+]" success line in the auxiliary output; see msf_output evidence.',
                   tier: 3)
      end
    end
  end
end

TomcatManagerDefaultCredsTest.new.run
