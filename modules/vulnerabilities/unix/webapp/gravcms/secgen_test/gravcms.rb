require_relative '../../../../../lib/post_provision_test'

# Post-provision test for the GravCMS Admin Plugin unauthenticated RCE
# (CVE-2021-21425, exploit/linux/http/gravcms_exec). The module ships Grav core
# 1.7.8 with Admin Plugin 1.10.7 (vulnerable), served by Apache at /grav-admin/.
#
# Tiers:
#   1  Apache, PHP and the extracted Grav app are present on the target.
#   2  Apache serves the Grav site over HTTP (port 80, /grav-admin/).
#   3  the Metasploit exploit, run from the attacker VM, achieves RCE; the
#      flag leaked into the webroot is captured alongside the sentinel as
#      evidence. With no attacker VM in the project, tier 3 SKIPs and the
#      lower tiers still run.
class GravcmsTest < PostProvisionTest
  def initialize
    self.module_name = 'gravcms'
    self.module_path = get_module_path(__FILE__)
    super
    self.port = 80
  end

  def test_module
    super

    # Tier 1 — provisioned on the target.
    tier(1) do
      test_command_succeeds('apache2 installed', 'test -x /usr/sbin/apache2')
      test_command_succeeds('php installed', 'command -v php')
      test_command_succeeds('grav app extracted', 'test -d /var/www/html/grav-admin')
    end

    # Tier 2 — Apache serves the Grav site.
    tier(2) do
      test_service_up                               # port 80 listening
      test_http('/grav-admin/', match: 'Grav')      # Grav renders its home page
    end

    # Tier 3 — unauthenticated RCE from the attacker VM.
    test_msf_exploit('exploit/linux/http/gravcms_exec',
                     rport: 80,
                     options: { 'TARGETURI' => '/grav-admin/' },
                     collect: 'cat /var/www/html/grav-admin/flag* 2>/dev/null',
                     tier: 3)
  end
end

GravcmsTest.new.run
