require_relative '../../../../../lib/post_provision_test'

class Popa3dTest < PostProvisionTest
  def initialize
    self.module_name = 'popa3d'
    self.module_path = get_module_path(__FILE__)
    super
    self.port = 110
  end

  def test_module
    super
    # Tier 1: package installed (Debian ships the daemon at /usr/sbin/popa3d)
    test_command_succeeds('popa3d binary installed', 'test -x /usr/sbin/popa3d', tier: 1)
    # Tier 2: daemon listening (standalone mode is the debconf default) and greets with POP3 +OK
    test_service_up
    test_banner('+OK', label: 'POP3 greeting from popa3d')
  end
end

Popa3dTest.new.run