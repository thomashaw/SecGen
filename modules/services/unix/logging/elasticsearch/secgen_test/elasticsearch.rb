require_relative '../../../../../lib/post_provision_test'

class ElasticsearchTest < PostProvisionTest
  def initialize
    self.module_name = 'elasticsearch'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) { test_local_command('elasticsearch installed?', 'dpkg -l elasticsearch | grep -E "^ii"', 'elasticsearch') }
    tier(2) { test_service_up(port: 9200) }
    tier(2) { test_http('/', port: 9200, match: 'cluster_name') }
  end
end

ElasticsearchTest.new.run
