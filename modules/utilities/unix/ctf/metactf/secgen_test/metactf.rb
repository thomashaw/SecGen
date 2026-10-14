require_relative '../../../../../lib/post_provision_test'
require 'json'
require 'shellwords'

class MetactfTest < PostProvisionTest
  def initialize
    self.module_name = 'metactf'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    account = JSON.parse(Array(json_inputs['account']).first || '{}')
    user = account['username']
    challenges = Array(json_inputs['challenge_list']).map { |c| JSON.parse(c) }
    skip!('no account or challenge_list input') if user.nil? || challenges.empty?

    challenges.each do |challenge|
      name = challenge['challenge_name']
      dir = "/home/#{user}/challenges/#{name}"
      bin = Shellwords.escape("#{dir}/#{name}")
      flag_file = Shellwords.escape("#{dir}/flag")

      # placed by secgen_functions::install_setgid_binary: setgid to the challenge's group, flag readable only by it
      tier(1) do
        test_local_command("#{name}: setgid binary", "stat -c '%a %G' #{bin}", "2775 #{challenge['group']}")
        test_local_command("#{name}: flag file", "stat -c '%a %U %G' #{flag_file}", "760 root #{challenge['group']}")
        test_local_command("#{name}: flag value", "cat #{flag_file}", challenge['flag'])
      end

      tier(2) do
        test_command_succeeds("#{name}: student cannot read the flag directly", "! cat #{flag_file}", user: user)
        # A challenge built with empty randomised values (e.g. build.zsh without bc) typically accepts an empty or
        # arbitrary password; a correctly built one releases the flag only for its password.
        %w[empty wrong].each do |kind|
          input = kind == 'empty' ? '\n' : 'secgen-wrong-password\n'
          out = run_as_user(user, "cd #{Shellwords.escape(dir)} && printf '#{input}' | timeout 10 ./#{Shellwords.escape(name)} 2>&1")
          if out[:stdout].include?(challenge['flag']) || out[:stderr].include?(challenge['flag'])
            fail_check("#{name}: #{kind} password rejected", 'the flag was released (challenge built with empty values?)')
          else
            pass_check("#{name}: #{kind} password rejected")
          end
        end
      end
    end
  end
end

MetactfTest.new.run
