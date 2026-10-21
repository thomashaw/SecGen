require_relative '../../../../../lib/post_provision_test'

class PasswordlessUserAccountTest < PostProvisionTest
  def initialize
    self.module_name = 'passwordless_user_account'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      # A non-system user account created
      test_local_command('non-system user created', 'awk -F: "$3 >= 1000 && $3 < 65534 {print $1}" /etc/passwd', '.+')
    end
    tier(2) do
      # That user's shadow entry has an empty password field (field 2 is empty or just '::')
      test_local_command('passwordless entry in shadow', 'awk -F: "$3 >= 1000 && $3 < 65534 {print $1}" /etc/passwd | xargs -I{} grep "^{}::" /etc/shadow', '::')
    end
    # TODO tier 3 (exploit): deferred — wire up test_msf_exploit when attacker VM is available
  end
end

PasswordlessUserAccountTest.new.run
