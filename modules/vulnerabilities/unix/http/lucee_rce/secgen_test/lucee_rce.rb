require_relative '../../../../../lib/post_provision_test'

class LuceeRceTest < PostProvisionTest
  def initialize
    self.module_name = 'lucee_rce'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_local_command('java available', 'java -version 2>&1', 'version')
      # Lucee Express is extracted as a zip; check start script
      test_local_command('lucee start script', 'test -f /opt/lucee/bin/startup.sh && echo present', 'present')
    end
    tier(2) do
      test_service_up
      # Lucee Administrator login page
      test_http('/lucee/admin/server.cfm', match: 'Lucee')
    end
    # TODO tier 3 (exploit): deferred — wire up test_msf_exploit when attacker VM is available
    # msf_module: exploit/linux/http/lucee_admin_imgprocess_file_write (CVE-2021-21307)
  end
end

LuceeRceTest.new.run
