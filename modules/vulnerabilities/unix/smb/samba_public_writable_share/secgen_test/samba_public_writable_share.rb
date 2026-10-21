require_relative '../../../../../lib/post_provision_test'

class SambaPublicWritableShareTest < PostProvisionTest
  def initialize
    self.module_name = 'samba_public_writable_share'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    share_name = self.json_inputs['share_name']&.first

    tier(1) do
      test_local_command('samba installed', 'dpkg -l samba | grep -E "^ii"', 'samba')
      if share_name
        test_local_command("share [#{share_name}] in smb.conf", "grep -i '\\[#{share_name}\\]' /etc/samba/smb.conf", share_name)
      else
        test_local_command('samba share configured in smb.conf', 'grep -i "\[" /etc/samba/smb.conf', '[')
      end
    end
    tier(2) do
      test_service_up(port: 445)
      test_local_command('share listed anonymously', 'smbclient -L localhost -N', 'Sharename')
      if share_name
        test_local_command("#{share_name} share visible", "smbclient -L localhost -N", share_name)
      end
    end
    # TODO tier 3 (exploit): deferred — connect and write/read files from attacker VM
  end
end

SambaPublicWritableShareTest.new.run
