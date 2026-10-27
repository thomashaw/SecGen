require_relative '../../../../../lib/post_provision_test'

class KibanaTest < PostProvisionTest
  def initialize
    self.module_name = 'kibana'
    self.module_path = get_module_path(__FILE__)
    super
    # the module's port input is kibana_port, not port
    kp = json_inputs['kibana_port']&.first
    self.port = kp.to_i if kp
  end

  def test_module
    super
    # kibana_ip is applied as server.host in kibana.yml (it used to be set from
    # elasticsearch_ip), so it is checked below.
    tier(1) do
      test_local_command('kibana installed?', 'dpkg -l kibana | grep -E "^ii"', 'kibana')
      # kibana.yml carries the configured listen port and Elasticsearch endpoint
      if (kp = json_inputs['kibana_port']&.first)
        test_local_command('kibana.yml server.port', 'cat /etc/kibana/kibana.yml', "server.port: #{kp}")
      end
      if (ki = json_inputs['kibana_ip']&.first)
        test_local_command('kibana.yml server.host', 'cat /etc/kibana/kibana.yml', "server.host: \"#{ki}\"")
      end
      es_ip = json_inputs['elasticsearch_ip']&.first
      es_port = json_inputs['elasticsearch_port']&.first
      if es_ip && es_port
        test_local_command('kibana.yml elasticsearch.hosts', 'cat /etc/kibana/kibana.yml',
                           "http://#{es_ip}:#{es_port}")
      end
    end
    tier(1) do
      test_local_command('kibana binary present', 'ls /usr/share/kibana/bin/kibana', 'kibana')
      test_local_command('kibana service enabled', 'systemctl is-enabled kibana', 'enabled')
      test_local_command('kibana.yml owner and mode', 'stat -c "%U:%G %a" /etc/kibana/kibana.yml', 'kibana:kibana 660')
    end
    tier(2) do
      test_command_succeeds('kibana service active', 'systemctl is-active kibana')
      # the server process runs as the kibana account
      test_local_command('kibana process owner', 'ps -eo user:20,args | grep [k]ibana', 'kibana')
      test_service_up
      test_http('/api/status', port: self.port, match: 'overall')
    end
  end
end

KibanaTest.new.run
