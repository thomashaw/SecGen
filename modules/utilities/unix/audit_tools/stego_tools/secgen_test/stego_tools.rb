require_relative '../../../../../lib/post_provision_test'

class StegoToolsTest < PostProvisionTest
  def initialize
    self.module_name = 'stego_tools'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) { test_local_command('steghide installed?', 'which steghide', 'steghide') }
    tier(2) do
      test_command_succeeds('steghide reports version', 'steghide --version')
      test_local_command('exiftool available', 'exiftool -ver', '.')
    end
  end
end

StegoToolsTest.new.run
