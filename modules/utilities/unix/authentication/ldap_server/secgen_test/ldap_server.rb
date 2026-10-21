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
    base_dn = if (domain = self.json_inputs['domain']&.first)
      domain.split('.').map { |c| "dc=#{c}" }.join(',')
    end

    tier(2) do
      test_service_up
      if base_dn
        test_local_command("base DN #{base_dn} in naming contexts",
                           "ldapsearch -x -H ldap://localhost -b '' -s base namingContexts 2>&1",
                           base_dn)
      else
        test_local_command('slapd answers ldap queries', 'ldapsearch -x -H ldap://localhost -b "" -s base namingContexts 2>&1', 'result:')
      end
    end
  end
end

LdapServerTest.new.run
