require_relative '../../../../../lib/post_provision_test'

class Php7Test < PostProvisionTest
  def initialize
    self.module_name = 'php7'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_local_command('php7.4 installed?', 'dpkg -l php7.4-cli | grep -E "^ii"', 'php7.4-cli')
      test_local_command('sury repo configured?', 'test -f /etc/apt/sources.list.d/php7-sury.list && echo ok', 'ok')
    end
    tier(2) { test_command_succeeds('php7.4 version', 'php7.4 --version') }
  end
end

Php7Test.new.run
