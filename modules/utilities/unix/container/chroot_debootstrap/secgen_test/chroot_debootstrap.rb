require_relative '../../../../../lib/post_provision_test'

class ChrootDebootstrapTest < PostProvisionTest
  CHROOT_DIR = '/opt/chroot'

  def initialize
    self.module_name = 'chroot_debootstrap'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) { test_local_command('debootstrap installed?', 'which debootstrap', 'debootstrap') }
    tier(2) do
      # chroot directory and a basic Debian tree should be present after provisioning
      test_local_command('chroot dir exists', "test -d #{CHROOT_DIR}/etc && echo ok", 'ok')
      test_local_command('ncat present in chroot', "test -x #{CHROOT_DIR}/usr/bin/ncat && echo ok", 'ok')
    end
  end
end

ChrootDebootstrapTest.new.run
