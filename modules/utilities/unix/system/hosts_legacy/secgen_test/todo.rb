require_relative '../../../../../lib/post_provision_test'

class HostsTest < PostProvisionTest
  def initialize
    self.module_name = 'hosts'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    test_hosts_exist
  end

  def test_hosts_exist
    # TODO: test_local_command("#{username} account exists?", 'cat /etc/passwd', username)

  end
end

HostsTest.new.run
