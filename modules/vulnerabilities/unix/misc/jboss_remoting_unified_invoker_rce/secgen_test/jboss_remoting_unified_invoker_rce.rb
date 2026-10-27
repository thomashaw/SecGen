require_relative '../../../../../lib/post_provision_test'

# JBoss EAP 6.x uses fixed ports: HTTP on 8080, JBoss Remoting on 4447
JBOSS_HTTP_PORT = 8080

class JbossRemotingUnifiedInvokerRceTest < PostProvisionTest
  def initialize
    self.module_name = 'jboss_remoting_unified_invoker_rce'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    username = self.json_inputs['unix_username']&.first

    tier(1) do
      if username
        test_local_command("#{username} user exists", "id #{username}", username)
      end
      test_local_command('java available', 'java -version 2>&1', 'version')
      test_local_command('jboss install exists', 'test -d /opt/jboss && echo present', 'present')
    end
    tier(2) do
      # JBoss EAP HTTP management/application port — no randomised port for this module
      test_service_up(port: JBOSS_HTTP_PORT)
      test_http('/', port: JBOSS_HTTP_PORT, match: 'JBoss')
    end
    # TODO tier 3 (exploit): deferred — wire up test_msf_exploit when attacker VM is available
    # msf_module: exploit/multi/misc/jboss_remoting_unified_invoker_rce (targets remoting port 4447)
  end
end

JbossRemotingUnifiedInvokerRceTest.new.run
