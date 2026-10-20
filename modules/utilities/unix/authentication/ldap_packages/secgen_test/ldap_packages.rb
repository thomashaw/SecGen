require_relative '../../../../../lib/post_provision_test'

class LdapPackagesTest < PostProvisionTest
  def initialize
    self.module_name = 'ldap_packages'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_local_command('ldap-utils installed', 'dpkg -l ldap-utils | grep -E "^ii"', 'ldap-utils')
      test_local_command('nslcd installed', 'dpkg -l nslcd | grep -E "^ii"', 'nslcd')
      test_local_command('sssd installed', 'dpkg -l sssd | grep -E "^ii"', 'sssd')
    end
    tier(2) do
      test_command_succeeds('ldapsearch available', 'ldapsearch --help 2>&1 || true')
      test_local_command('ldapsearch on PATH', 'which ldapsearch', 'ldapsearch')
    end
  end
end

LdapPackagesTest.new.run
