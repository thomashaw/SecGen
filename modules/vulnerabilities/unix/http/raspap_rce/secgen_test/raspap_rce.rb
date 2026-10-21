require_relative '../../../../../lib/post_provision_test'

class RaspapRceTest < PostProvisionTest
  def initialize
    self.module_name = 'raspap_rce'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_local_command('lighttpd installed?', 'dpkg -l lighttpd | grep -E "^ii"', 'lighttpd')
      test_local_command('raspap installed?', 'test -d /var/www/raspap && echo present', 'present')
    end
    tier(2) do
      test_service_up
      test_http('/')
    end
    # TODO tier 3 (exploit): deferred — wire up test_msf_exploit when attacker VM is available
    # msf_module: exploit/unix/http/raspap_rce (CVE-2022-39986)
  end
end

RaspapRceTest.new.run
