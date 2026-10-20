require_relative '../../../../../lib/post_provision_test'

class DeadImageTest < PostProvisionTest
  def initialize
    self.module_name = 'dead_image'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_local_command('evidence directory exists', 'test -d /root/evidence', '/root/evidence')
      test_local_command('disk image present', 'test -f /root/evidence/hda1.img', '/root/evidence/hda1.img')
      test_local_command('md5s file present', 'test -f /root/evidence/md5s', '/root/evidence/md5s')
    end
    tier(2) do
      # Image file must be non-empty (it is fetched from a remote URL at provision time)
      test_local_command('disk image non-empty', 'test -s /root/evidence/hda1.img', '')
    end
  end
end

DeadImageTest.new.run
