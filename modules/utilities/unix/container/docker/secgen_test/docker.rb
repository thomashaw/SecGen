require_relative '../../../../../lib/post_provision_test'

class DockerTest < PostProvisionTest
  def initialize
    self.module_name = 'docker'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) { test_local_command('docker binary present', 'which docker', 'docker') }
    tier(2) do
      test_command_succeeds('docker version runs', 'docker --version')
      test_command_succeeds('docker daemon socket exists', 'test -S /var/run/docker.sock')
    end
  end
end

DockerTest.new.run
