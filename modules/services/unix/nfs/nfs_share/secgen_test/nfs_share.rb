require_relative '../../../../../lib/post_provision_test'

class NFSShareTest < PostProvisionTest
  def initialize
    self.module_name = 'nfs_share'
    self.module_path = get_module_path(__FILE__)
    super
    self.port = 2049
  end

  def test_module
    super
    tier(1) do
      test_command_succeeds('nfs-kernel-server installed', 'dpkg -s nfs-kernel-server')
      test_local_command('exports file', 'cat /etc/exports', 'no_subtree_check')
    end
    # Tier 2: NFS server listening and the export is active
    test_service_up
    test_local_command('active export', 'exportfs -v', 'no_subtree_check')
  end
end

NFSShareTest.new.run