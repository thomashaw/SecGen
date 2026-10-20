require_relative '../../../../../lib/post_provision_test'

MALTRAIL_PORT = 8338

class MaltrailRceTest < PostProvisionTest
  def initialize
    self.module_name = 'maltrail_rce'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_local_command('python3 installed?', 'which python3', '/python3')
      # MalTrail is installed as a tarball under the dedicated user's home
      test_local_command('maltrail script exists', 'find /home -name "maltrail.py" | head -1', 'maltrail.py')
    end
    tier(2) do
      # MalTrail uses fixed port 8338
      test_service_up(port: MALTRAIL_PORT)
      # The /login endpoint serves the MalTrail login form
      test_http('/login', port: MALTRAIL_PORT, match: 'maltrail')
    end
    # TODO tier 3 (exploit): deferred — wire up test_msf_exploit when attacker VM is available
    # msf_module: exploit/unix/http/maltrail_rce (CVE-2025-34073)
  end
end

MaltrailRceTest.new.run
