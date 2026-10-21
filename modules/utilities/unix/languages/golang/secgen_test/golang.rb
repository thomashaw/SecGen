require_relative '../../../../../lib/post_provision_test'

class GolangTest < PostProvisionTest
  def initialize
    self.module_name = 'golang'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    # golang is installed to /usr/local/go (not via apt)
    tier(1) { test_local_command('go installed?', 'test -d /usr/local/go && echo ok', 'ok') }
    tier(2) { test_command_succeeds('go version', '/usr/local/go/bin/go version') }
  end
end

GolangTest.new.run
