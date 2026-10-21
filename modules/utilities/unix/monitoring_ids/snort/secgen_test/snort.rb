require_relative '../../../../../lib/post_provision_test'

class SnortTest < PostProvisionTest
  def initialize
    self.module_name = 'snort'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_local_command('snort-common installed?', 'dpkg -l snort-common | grep -E "^ii"', 'snort-common')
      test_local_command('snort binary present', 'which snort', 'snort')
    end
    tier(2) { test_command_succeeds('snort version runs', 'snort --version 2>&1 | head -3') }
  end
end

SnortTest.new.run
