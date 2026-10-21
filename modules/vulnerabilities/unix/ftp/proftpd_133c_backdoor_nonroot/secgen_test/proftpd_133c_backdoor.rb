require_relative '../../../../../lib/post_provision_test'

class Proftpd133cBackdoorTest < PostProvisionTest
  def initialize
    self.module_name = 'proftpd_133c_backdoor_nonroot'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    test_service_up
    # TODO tier 3 (exploit): deferred — wire up test_msf_exploit when attacker VM is available
    # msf_module: exploit/unix/ftp/proftpd_133c_backdoor
    # Note: set self.port = 21 in initialize; same exploit as proftpd_133c_backdoor (non-root variant)
  end
end

Proftpd133cBackdoorTest.new.run
