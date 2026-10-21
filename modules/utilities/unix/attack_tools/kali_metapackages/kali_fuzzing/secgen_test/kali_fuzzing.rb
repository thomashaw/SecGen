require_relative '../../../../../lib/post_provision_test'

class KaliFuzzingTest < PostProvisionTest
  def initialize
    self.module_name = 'kali_fuzzing'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_local_command('kali-tools-fuzzing installed', 'dpkg -l kali-tools-fuzzing | grep -E "^ii"', 'kali-tools-fuzzing')
      test_local_command('ncat installed', 'dpkg -l ncat | grep -E "^ii"', 'ncat')
    end
    tier(2) { test_command_succeeds('ncat available', 'ncat --version') }
  end
end

KaliFuzzingTest.new.run
