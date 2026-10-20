require_relative '../../../../../lib/post_provision_test'

class HashToolsTest < PostProvisionTest
  def initialize
    self.module_name = 'hash_tools'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    # md5deep on Debian, hashdeep on Kali (md5deep transitional package dropped from kali-rolling)
    tier(1) { test_local_command('md5deep or hashdeep installed?', 'dpkg -l md5deep hashdeep 2>/dev/null | grep "^ii"', 'deep') }
    tier(2) { test_local_command('deep-hash tool produces output', '(md5deep /etc/hostname || hashdeep /etc/hostname) 2>&1', '/etc/hostname') }
  end
end

HashToolsTest.new.run
