require_relative '../../../../../lib/post_provision_test'

class LabtainersMarkerTest < PostProvisionTest
  def initialize
    self.module_name = 'labtainers_marker'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_local_command('labflags directory exists', 'test -d /opt/labflags', '/opt/labflags')
      test_local_command('labflags binary present', 'test -f /opt/labflags/labflags', '/opt/labflags/labflags')
      test_local_command('labflags.rb present', 'test -f /opt/labflags/labflags.rb', '/opt/labflags/labflags.rb')
    end
    tier(2) do
      # labflags binary must be SUID root (mode 4755) so students can query flags
      test_local_command('labflags is SUID', "stat -c '%a' /opt/labflags/labflags | grep -q '^4'", '')
    end
  end
end

LabtainersMarkerTest.new.run
