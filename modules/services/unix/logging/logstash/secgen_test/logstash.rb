require_relative '../../../../../lib/post_provision_test'

class LogstashTest < PostProvisionTest
  def initialize
    self.module_name = 'logstash'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) { test_local_command('logstash installed?', 'dpkg -l logstash | grep -E "^ii"', 'logstash') }
    tier(2) { test_local_command('logstash process running?', 'ps -ef | grep logstash', 'logstash') }
    # Logstash default beats input listens on port 5044; no HTTP endpoint by default
    tier(2) { test_service_up(port: 5044) }
  end
end

LogstashTest.new.run
