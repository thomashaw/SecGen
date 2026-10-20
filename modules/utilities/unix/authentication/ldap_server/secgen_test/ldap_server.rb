require_relative '../../../../../lib/post_provision_test'

class LdapServerTest < PostProvisionTest
  def initialize
    self.module_name = 'ldap_server'
    self.module_path = get_module_path(__FILE__)
    self.port = 389
    super
  end

  def test_module
    super
    tier(1) do
      test_local_command('slapd installed', 'dpkg -l slapd | grep -E "^ii"', 'slapd')
      test_local_command('ldap-utils installed', 'dpkg -l ldap-utils | grep -E "^ii"', 'ldap-utils')
      test_local_command('phpldapadmin installed', 'dpkg -l phpldapadmin | grep -E "^ii"', 'phpldapadmin')
    end
    tier(2) do
      test_service_up
      # anonymous bind should return the base object
      test_local_command('slapd answers ldap queries', 'ldapsearch -x -H ldap://localhost -b "" -s base namingContexts 2>&1', 'result:')
    end
  end
end

LdapServerTest.new.run
