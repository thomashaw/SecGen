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
      test_local_command('git package installed', 'dpkg -l git | grep -E "^ii"', 'git')
      test_local_command('git installed', 'which git', '/usr/bin/git')
    end
    tier(2) do
      test_command_succeeds('git version runs', 'git --version')
      # a tiny real invocation: create a repository, commit and read the log
      test_local_command('git init, commit and log',
                         'd=$(mktemp -d) && cd "$d" && git init -q && ' \
                         'git -c user.name=t -c user.email=t@example.com commit -q --allow-empty -m secgen-test && ' \
                         'git log --format=%s; rm -rf "$d"',
                         'secgen-test')
    end
  end
end

GitTest.new.run
