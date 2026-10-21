require_relative '../../../../../lib/post_provision_test'

class NfsOvershareTest < PostProvisionTest
  def initialize
    self.module_name = 'nfs_overshare'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_local_command('nfs-kernel-server installed', 'dpkg -l nfs-kernel-server | grep -E "^ii"', 'nfs-kernel-server')
      test_local_command('/etc/exports configured', 'test -f /etc/exports && cat /etc/exports', '/')
    end
    tier(2) do
      # NFS service exposes a share
      test_local_command('NFS export visible', 'showmount -e localhost', 'Export list')
    end
    # TODO tier 3 (exploit): deferred — mount share from attacker VM and read leaked files
  end
end

NfsOvershareTest.new.run
