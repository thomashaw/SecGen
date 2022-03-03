require_relative '../../../../../lib/post_provision_test'


class SetUIDNmapTest < PostProvisionTest
  def initialize
    self.module_name = 'setuid_nmap'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    test_local_command('nmap has setuid flag?', 'sudo ls -la /usr/bin/nmap', '-rwsr-xr-x')
    test_local_command('nmap runs?', 'sudo /usr/bin/nmap --version', 'Nmap version')
  end

end

SetUIDNmapTest.new.run