require_relative '../../../../../lib/post_provision_test'


class SUIDviewTest < PostProvisionTest
  def initialize
    self.module_name = 'suid_root_view'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    test_local_command('view suid bit set?','sudo ls -la $(readlink -f `whereis view`)', 'rwsrwxrwx')
  end

end

SUIDviTest.new.run
