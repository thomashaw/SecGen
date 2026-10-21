require_relative '../../../../../lib/post_provision_test'

# Verifies that after_login_message appended a message to /etc/bash.bashrc.
# The exact message is scenario-controlled; we check bashrc is non-empty and modified.
class AfterLoginMessageTest < PostProvisionTest
  def initialize
    self.module_name = 'after_login_message'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_command_succeeds('/etc/bash.bashrc exists', 'test -f /etc/bash.bashrc')
    end
    tier(2) do
      if (msg = self.json_inputs['strings_to_leak']&.first)
        test_local_command('bash.bashrc contains message', 'cat /etc/bash.bashrc', msg)
      else
        test_local_command('bash.bashrc has heredoc block', 'grep -c "EOF" /etc/bash.bashrc', '1')
      end
    end
  end
end

AfterLoginMessageTest.new.run
