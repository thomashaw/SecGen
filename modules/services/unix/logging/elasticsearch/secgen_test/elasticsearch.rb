require_relative '../../../../../lib/post_provision_test'

class ElasticsearchTest < PostProvisionTest
  def initialize
    self.module_name = 'elasticsearch'
    self.module_path = get_module_path(__FILE__)
    super
    # The module's port input is elasticsearch_port, not the generic 'port'.
    es_port = (json_inputs['elasticsearch_port'] || []).first
    self.port = es_port.to_i if es_port
    self.port = 9200 if port.nil? || port.to_i <= 0
  end

  def test_module
    super
    tier(1) do
      test_local_command('elasticsearch package installed', 'dpkg -s elasticsearch', 'Status: install ok installed')
      test_local_command('elasticsearch.yml configured', 'cat /etc/elasticsearch/elasticsearch.yml', "http.port: #{port}")
    end
    tier(2) do
      test_command_succeeds('elasticsearch service active', 'systemctl is-active elasticsearch')
      test_service_up
      # The root document returns the cluster banner as JSON.
      test_http('/', match: 'You Know, for Search')
      test_http('/_cluster/health', match: 'cluster_name')
    end
  end
end

ElasticsearchTest.new.run
