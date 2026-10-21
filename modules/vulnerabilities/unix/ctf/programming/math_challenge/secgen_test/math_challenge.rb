require_relative '../../../../../lib/post_provision_test'

class MathChallengeTest < PostProvisionTest
  def initialize
    self.module_name = 'math_challenge'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_local_command('ruby installed', 'which ruby', 'ruby')
      # Challenge script is placed in a user home or challenge directory.
      test_local_command('challenge file exists', 'find /home /root -name "*.rb" -not -path "*/secgen_test/*" | head -1', '/')
    end
    tier(2) do
      # Ruby runs without crashing with a basic invocation.
      test_command_succeeds('ruby interpreter works', 'ruby -e "puts 1+1"')
    end
    # TODO tier 3 (exploit): deferred — challenge is a CTF programming task, no remote exploit
  end
end

MathChallengeTest.new.run
