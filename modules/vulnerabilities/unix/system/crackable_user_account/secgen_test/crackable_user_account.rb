require_relative '../../../../../lib/post_provision_test'

class CrackableUserAccountTest < PostProvisionTest
  def initialize
    self.module_name = 'crackable_user_account'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      # A non-system user account (UID >= 1000) must exist
      test_local_command('non-system user created', 'awk -F: "$3 >= 1000 && $3 < 65534 {print $1}" /etc/passwd', '.+')
    end
    tier(2) do
      # That user must have a hashed (not locked) password in /etc/shadow
      test_local_command('crackable hash in shadow', 'awk -F: "$3 >= 1000 && $3 < 65534 {print $1}" /etc/passwd | xargs -I{} grep "^{}" /etc/shadow | grep -v "^[^:]*:!"', '$')
    end
    # TODO tier 3 (exploit): deferred — wire up test_msf_exploit when attacker VM is available
  end
end

CrackableUserAccountTest.new.run
