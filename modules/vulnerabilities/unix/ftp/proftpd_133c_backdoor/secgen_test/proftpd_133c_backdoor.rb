require_relative '../../../../../lib/post_provision_test'

class Proftpd133cBackdoorTest < PostProvisionTest
  def initialize
    self.module_name = 'proftpd_133c_backdoor'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    test_service_up
    # TODO tier 3 (exploit): deferred — wire up test_msf_exploit when attacker VM is available
    # msf_module: exploit/unix/ftp/proftpd_133c_backdoor
    # Note: set self.port = 21 in initialize; the backdoor is triggered over FTP port 21
  end
end

Proftpd133cBackdoorTest.new.run