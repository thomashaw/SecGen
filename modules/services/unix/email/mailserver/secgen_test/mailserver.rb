require_relative '../../../../../lib/post_provision_test'

class MailserverTest < PostProvisionTest
  def initialize
    self.module_name = 'mailserver'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) { test_local_command('postfix installed?', 'dpkg -l postfix | grep -E "^ii"', 'postfix') }
    tier(1) { test_local_command('dovecot installed?', 'dpkg -l dovecot-core | grep -E "^ii"', 'dovecot') }
    tier(2) { test_service_up(port: 25) }
    tier(2) { test_banner('220', port: 25) }
    tier(2) { test_service_up(port: 143) }
  end
end

MailserverTest.new.run
