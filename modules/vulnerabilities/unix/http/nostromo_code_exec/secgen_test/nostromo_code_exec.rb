require_relative '../../../../../lib/post_provision_test'

class NostromoCodeExecTest < PostProvisionTest
  def initialize
    self.module_name = 'nostromo_code_exec'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_local_command('nostromousr exists', 'id nostromousr', 'nostromousr')
      # nhttpd binary is built from source and installed under the user's home
      test_local_command('nhttpd binary exists', 'find /home/nostromousr -name "nhttpd" | head -1', 'nhttpd')
    end
    tier(2) do
      test_service_up
      # Nostromo serves the pre-leaked HTML index page
      test_http('/', match: nil)
    end
    # TODO tier 3 (exploit): deferred — wire up test_msf_exploit when attacker VM is available
    # msf_module: exploit/multi/http/nostromo_code_exec (CVE-2019-16278)
  end
end

NostromoCodeExecTest.new.run
