require_relative '../../../../../lib/post_provision_test'
require 'shellwords'

class LdapServerTest < PostProvisionTest
  def initialize
    self.module_name = 'ldap_server'
    self.module_path = get_module_path(__FILE__)
    super
    # slapd's own protocol port; the module has no port input, so the base
    # class would otherwise leave this unset (-1) and skip the port check.
    self.port = 389
  end

  def test_module
    super
    tier(1) do
      test_local_command('slapd installed', 'dpkg -l slapd | grep -E "^ii"', 'slapd')
      test_local_command('ldap-utils installed', 'dpkg -l ldap-utils | grep -E "^ii"', 'ldap-utils')
      test_local_command('phpldapadmin installed', 'dpkg -l phpldapadmin | grep -E "^ii"', 'phpldapadmin')
    end

    # domain -> base DN, as the module derives it (example.com -> dc=example,dc=com)
    domain = self.json_inputs['domain']&.first
    base_dn = domain && domain.split('.').map { |c| "dc=#{c}" }.join(',')
    organization = self.json_inputs['organization']&.first
    admin_password = self.json_inputs['admin_password']&.first

    if base_dn
      # phpLDAPadmin is pointed at the configured base DN
      tier(1) do
        test_local_command("phpldapadmin config uses #{base_dn}",
                           'grep -F "server\',\'base\'" /etc/phpldapadmin/config.php', base_dn)
      end
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

      # organization is stored on the base entry's "o" attribute
      if base_dn && organization
        test_local_command("organization '#{organization}' on #{base_dn}",
                           "ldapsearch -x -LLL -H ldap://localhost -b #{Shellwords.escape(base_dn)} -s base o 2>&1",
                           "o: #{organization}")
      end

      # the generated admin password lets cn=admin bind (password read from stdin, not argv)
      if base_dn && admin_password
        admin_dn = "cn=admin,#{base_dn}"
        test_local_command("admin password binds as #{admin_dn}",
                           "printf %s #{Shellwords.escape(admin_password)} | ldapwhoami -x -H ldap://localhost -D #{Shellwords.escape(admin_dn)} -y /dev/stdin 2>&1",
                           "dn:#{admin_dn}")
      end

      # phpLDAPadmin web interface served by Apache
      test_http('/phpldapadmin/', port: 80, label: 'phpLDAPadmin web interface')
    end
  end
end

LdapServerTest.new.run
