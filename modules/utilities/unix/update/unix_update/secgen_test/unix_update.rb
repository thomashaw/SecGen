require_relative '../../../../../lib/post_provision_test'

class UnixUpdateTest < PostProvisionTest
  def initialize
    self.module_name = 'unix_update'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      # The module runs apt-get update; check that apt lists have been refreshed
      test_local_command('apt-get available', 'which apt-get', '/usr/bin/apt-get')
      test_local_command('apt lists populated', 'test -d /var/lib/apt/lists', '/var/lib/apt/lists')
    end
    tier(2) do
      # At least one package list file must exist (non-empty lists dir = update ran successfully)
      test_local_command('apt lists non-empty', 'ls /var/lib/apt/lists | grep -qv "^$"', '')
    end
  end
end

UnixUpdateTest.new.run
