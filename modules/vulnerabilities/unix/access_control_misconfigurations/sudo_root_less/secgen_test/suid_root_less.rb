require_relative '../../../../../lib/post_provision_test'


class SudoLessTest < PostProvisionTest
  def initialize
    self.module_name = 'sudo_root_less'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    # TODO: testing
  end

end

SUIDLessTest.new.run
