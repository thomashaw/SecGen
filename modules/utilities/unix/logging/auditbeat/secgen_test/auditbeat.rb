require_relative '../../../../../lib/post_provision_test'

class AuditbeatTest < PostProvisionTest
  def initialize
    self.module_name = 'auditbeat'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) { test_local_command('auditbeat installed?', 'dpkg -l auditbeat | grep -E "^ii"', 'auditbeat') }
    tier(2) do
      test_command_succeeds('auditbeat service active', 'systemctl is-active auditbeat')
      test_local_command('auditbeat config present', 'test -f /etc/auditbeat/auditbeat.yml && echo ok', 'ok')
    end
  end
end

AuditbeatTest.new.run
