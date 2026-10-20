require_relative '../../../../../lib/post_provision_test'

class IceweaselTest < PostProvisionTest
  def initialize
    self.module_name = 'iceweasel'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      # On Debian ≥9 and Kali, the package is firefox-esr; on older Debian it was iceweasel
      test_local_command('firefox-esr or iceweasel installed',
        'which firefox-esr || which iceweasel || which firefox', '')
    end
    tier(2) do
      test_command_succeeds('firefox binary runs (headless version check)',
        'firefox-esr --version 2>/dev/null || iceweasel --version 2>/dev/null || firefox --version 2>/dev/null')
    end
  end
end

IceweaselTest.new.run
