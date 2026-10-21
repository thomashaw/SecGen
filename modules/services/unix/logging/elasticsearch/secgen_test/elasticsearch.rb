require_relative '../../../../../lib/post_provision_test'

class ElasticsearchTest < PostProvisionTest
  def initialize
    self.module_name = 'elasticsearch'
    self.module_path = get_module_path(__FILE__)
    super
    self.port = json_inputs['elasticsearch_port']&.first&.to_i || 9200
  end

  def test_module
    super
    tier(1) { test_local_command('elasticsearch installed?', 'dpkg -l elasticsearch | grep -E "^ii"', 'elasticsearch') }
    tier(2) { test_service_up }
    tier(2) { test_http('/', port: self.port, match: 'cluster_name') }
  end
end

ElasticsearchTest.new.run
