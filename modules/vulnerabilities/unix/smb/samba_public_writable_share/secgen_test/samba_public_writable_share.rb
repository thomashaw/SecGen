require_relative '../../../../../lib/post_provision_test'

class SambaPublicWritableShareTest < PostProvisionTest
  def initialize
    self.module_name = 'samba_public_writable_share'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_local_command('samba installed', 'dpkg -l samba | grep -E "^ii"', 'samba')
      test_local_command('samba share configured in smb.conf', 'grep -i "\[" /etc/samba/smb.conf', '[')
    end
    tier(2) do
      test_service_up(port: 445)
      # Anonymous listing should work
      test_local_command('share listed anonymously', 'smbclient -L localhost -N', 'Sharename')
    end
    # TODO tier 3 (exploit): deferred — connect and write/read files from attacker VM
  end
end

SambaPublicWritableShareTest.new.run
