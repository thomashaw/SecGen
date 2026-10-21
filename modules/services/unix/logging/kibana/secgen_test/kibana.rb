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
    # kibana_ip is skipped: the Puppet never applies it (kibana.yml sets server.host
    # from elasticsearch_ip), so there is nothing separate to check on the box.
    tier(1) do
      test_local_command('kibana installed?', 'dpkg -l kibana | grep -E "^ii"', 'kibana')
      # kibana.yml carries the configured listen port and Elasticsearch endpoint
      if (kp = json_inputs['kibana_port']&.first)
        test_local_command('kibana.yml server.port', 'cat /etc/kibana/kibana.yml', "server.port: #{kp}")
      end
      es_ip = json_inputs['elasticsearch_ip']&.first
      es_port = json_inputs['elasticsearch_port']&.first
      if es_ip && es_port
        test_local_command('kibana.yml elasticsearch.hosts', 'cat /etc/kibana/kibana.yml',
                           "http://#{es_ip}:#{es_port}")
      end
    end
    tier(2) { test_service_up }
    tier(2) { test_http('/', port: self.port) }
  end
end

KibanaTest.new.run
