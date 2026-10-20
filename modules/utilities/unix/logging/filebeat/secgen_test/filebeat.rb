require_relative '../../../../../lib/post_provision_test'

class FilebeatTest < PostProvisionTest
  def initialize
    self.module_name = 'filebeat'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) { test_local_command('filebeat installed?', 'dpkg -l filebeat | grep -E "^ii"', 'filebeat') }
    tier(2) do
      test_command_succeeds('filebeat service active', 'systemctl is-active filebeat')
      test_local_command('filebeat config present', 'test -f /etc/filebeat/filebeat.yml && echo ok', 'ok')
    end
  end
end

FilebeatTest.new.run
