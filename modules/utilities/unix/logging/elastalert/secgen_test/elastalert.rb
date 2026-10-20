require_relative '../../../../../lib/post_provision_test'

class ElastalertTest < PostProvisionTest
  def initialize
    self.module_name = 'elastalert'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) { test_local_command('elastalert installed?', 'which elastalert || python3 -m elastalert --help 2>&1 | head -1', 'elastalert') }
    tier(2) { test_command_succeeds('elastalert service running', 'systemctl is-active elastalert') }
  end
end

ElastalertTest.new.run
