require_relative '../../../../../lib/post_provision_test'

class SambaSymlinkTraversalTest < PostProvisionTest
  def initialize
    self.module_name = 'samba_symlink_traversal'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_local_command('samba installed', 'dpkg -l samba | grep -E "^ii"', 'samba')
      # The 'allow insecure wide links' setting that enables symlink traversal
      test_local_command('wide links enabled', 'grep "allow insecure wide links" /etc/samba/smb.conf', 'yes')
    end
    tier(2) do
      test_service_up(port: 445)
      test_local_command('share listed anonymously', 'smbclient -L localhost -N', 'Sharename')
    end
    # TODO tier 3 (exploit): deferred — use auxiliary/admin/smb/samba_symlink_traversal from attacker VM
  end
end

SambaSymlinkTraversalTest.new.run
