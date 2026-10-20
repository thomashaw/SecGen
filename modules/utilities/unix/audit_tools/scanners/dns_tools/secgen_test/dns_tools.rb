require_relative '../../../../../lib/post_provision_test'

class DnsToolsTest < PostProvisionTest
  def initialize
    self.module_name = 'dns_tools'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) { test_local_command('dig installed?', 'which dig', 'dig') }
    # dig -v prints "DiG <version>" to stderr; test_local_command checks both streams
    tier(2) { test_local_command('dig reports version', 'dig -v 2>&1', 'DiG') }
  end
end

DnsToolsTest.new.run
