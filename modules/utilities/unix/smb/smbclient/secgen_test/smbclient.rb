require_relative '../../../../../lib/post_provision_test'

class SmbclientTest < PostProvisionTest
  def initialize
    self.module_name = 'smbclient'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_local_command('smbclient installed', 'which smbclient', 'smbclient')
    end
    tier(2) do
      test_local_command('smbclient version', 'smbclient --version', 'Version')
    end
  end
end

SmbclientTest.new.run
