require_relative '../../../../../lib/post_provision_test'

class WordpressTest < PostProvisionTest
  INSTALL_DIR = '/opt/wordpress'

  def initialize
    self.module_name = 'wordpress'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_local_command('wordpress directory exists', "test -d #{INSTALL_DIR} && echo ok", 'ok')
      test_local_command('wp-config.php present', "test -f #{INSTALL_DIR}/wp-config.php && echo ok", 'ok')
    end
    tier(2) { test_local_command('wordpress index.php present', "test -f #{INSTALL_DIR}/index.php && echo ok", 'ok') }
  end
end

WordpressTest.new.run
