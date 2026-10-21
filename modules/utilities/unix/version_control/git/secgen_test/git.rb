require_relative '../../../../../lib/post_provision_test'

class GitTest < PostProvisionTest
  def initialize
    self.module_name = 'git'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_local_command('git installed', 'which git', '/usr/bin/git')
    end
    tier(2) do
      test_command_succeeds('git version runs', 'git --version')
    end
  end
end

GitTest.new.run
