require_relative '../../../../../lib/post_provision_test'

class AliasRootkitTest < PostProvisionTest
  def initialize
    self.module_name = 'alias_rootkit'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      # The rootkit installs bash aliases that hide strings from common commands
      test_local_command('aliases in /root/.bashrc', "grep -c 'alias ps=' /root/.bashrc", '')
    end
    tier(2) do
      # Aliases survive a bash invocation (non-interactive -c; use bash directly so they expand)
      test_command_succeeds('aliases expand in bash', "bash -i -c 'type ps' 2>&1 | grep -q alias")
    end
  end
end

AliasRootkitTest.new.run
