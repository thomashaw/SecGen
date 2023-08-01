require_relative '../../../../../lib/post_provision_test'

class DirtyCOWTest < PostProvisionTest
  def initialize
    self.module_name = 'policykit'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    test_local_command('')
  end

end

DirtyCOWTest.new.run
