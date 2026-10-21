require_relative '../../../../../lib/post_provision_test'

class KibanaTest < PostProvisionTest
  KIBANA_PORT = 5601

  def initialize
    self.module_name = 'kibana'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_local_command('kibana package installed', 'dpkg -s kibana', 'Status: install ok installed')
      test_local_command('kibana config present', 'ls /etc/kibana/kibana.yml', '/etc/kibana/kibana.yml')
      test_local_command('kibana configured port', 'grep ^server.port /etc/kibana/kibana.yml', "server.port: #{KIBANA_PORT}")
    end
    tier(2) do
      # Kibana takes a while to start; wait for the listener before asserting.
      run_vagrant_ssh('for i in $(seq 1 36); do ss -ltn | grep -q ":5601 " && break; sleep 5; done', timeout: 200)
      test_service_up(port: KIBANA_PORT)
      # Any HTTP response carrying Kibana's kbn-name header proves Kibana itself
      # is answering (it replies 503 until Elasticsearch is reachable).
      test_command_succeeds('kibana answers HTTP',
                            "curl -s -i --max-time 20 http://localhost:#{KIBANA_PORT}/ | grep -qi '^kbn-name'")
    end
    # Service module: no intended vulnerability, so no tier 3.
  end
end

KibanaTest.new.run
