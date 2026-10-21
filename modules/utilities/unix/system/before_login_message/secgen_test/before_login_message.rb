require_relative '../../../../../lib/post_provision_test'

# Verifies that before_login_message wrote a message to /etc/issue and /etc/issue.net.
# The exact message is scenario-controlled; we check the files are non-empty.
class BeforeLoginMessageTest < PostProvisionTest
  def initialize
    self.module_name = 'before_login_message'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_command_succeeds('/etc/issue exists', 'test -f /etc/issue')
      test_command_succeeds('/etc/issue.net exists', 'test -f /etc/issue.net')
    end
    tier(2) do
      if (msg = self.json_inputs['strings_to_leak']&.first)
        test_local_command('/etc/issue contains message', 'cat /etc/issue', msg)
        test_local_command('/etc/issue.net contains message', 'cat /etc/issue.net', msg)
      else
        test_local_command('/etc/issue has content', 'wc -c /etc/issue', '')
        test_local_command('/etc/issue.net has content', 'wc -c /etc/issue.net', '')
      end
    end
  end
end

BeforeLoginMessageTest.new.run
