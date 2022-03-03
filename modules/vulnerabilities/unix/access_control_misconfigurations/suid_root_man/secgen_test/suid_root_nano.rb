require_relative '../../../../../lib/post_provision_test'


class SUIDNanoTest < PostProvisionTest
  def initialize
    self.module_name = 'suid_root_man'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    test_local_command('man suid bit set?','sudo ls -la /usr/bin/man', '-rwsrwxrwx')
    test_local_command('man runs?','/usr/bin/man --version', 'GNU nano')
  end

end

SUIDNanoTest.new.run
