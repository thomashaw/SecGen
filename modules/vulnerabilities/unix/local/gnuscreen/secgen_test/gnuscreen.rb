require_relative '../../../../../lib/post_provision_test'

class gnuscreen < PostProvisionTest
  def initialize
    self.module_name = 'gnuscreen'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    test_local_command('')
  end

end

gnuscreen.new.run
