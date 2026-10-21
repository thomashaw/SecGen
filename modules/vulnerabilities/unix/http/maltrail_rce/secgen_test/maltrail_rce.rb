require_relative '../../../../../lib/post_provision_test'

class MaltrailRceTest < PostProvisionTest
  def initialize
    self.module_name = 'maltrail_rce'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    username = self.json_inputs['unix_username']&.first

    tier(1) do
      test_local_command('python3 installed?', 'which python3', '/python3')
      # MalTrail is installed under the dedicated user's home directory
      if username
        test_local_command("#{username} user exists", "id #{username}", username)
        test_local_command('maltrail script exists',
                           "test -f /home/#{username}/maltrail-master/maltrail.py && echo ok", 'ok')
      else
        test_local_command('maltrail script exists', 'find /home -name "maltrail.py" | head -1', 'maltrail.py')
      end
    end
    tier(2) do
      test_service_up
      test_http('/login', match: 'maltrail')
    end
    # TODO tier 3 (exploit): deferred — wire up test_msf_exploit when attacker VM is available
    # msf_module: exploit/unix/http/maltrail_rce (CVE-2025-34073)
  end
end

MaltrailRceTest.new.run
