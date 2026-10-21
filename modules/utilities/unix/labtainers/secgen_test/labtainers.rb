require_relative '../../../../../lib/post_provision_test'

class LabtainersTest < PostProvisionTest
  def initialize
    self.module_name = 'labtainers'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_local_command('labtainer directory exists', 'test -d /opt/labtainer', '/opt/labtainer')
      test_local_command('labtainer-student bin exists', 'test -d /opt/labtainer/scripts/labtainer-student/bin', '/opt/labtainer/scripts/labtainer-student/bin')
      test_local_command('labtainer binary present', 'test -f /opt/labtainer/scripts/labtainer-student/bin/labtainer', '/opt/labtainer/scripts/labtainer-student/bin/labtainer')
      test_local_command('virtualenv present', 'test -d /opt/labtainer/venv', '/opt/labtainer/venv')
    end
    tier(2) do
      # labtainer script is executable and Python virtualenv is functional
      test_command_succeeds('labtainer bin executable', 'test -x /opt/labtainer/scripts/labtainer-student/bin/labtainer')
      test_local_command('docker available', 'which docker', '/usr/bin/docker')
    end
  end
end

LabtainersTest.new.run
