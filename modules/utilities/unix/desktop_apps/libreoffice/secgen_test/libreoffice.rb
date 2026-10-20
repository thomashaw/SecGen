require_relative '../../../../../lib/post_provision_test'

class LibreofficeTest < PostProvisionTest
  def initialize
    self.module_name = 'libreoffice'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_local_command('libreoffice-writer installed?', 'dpkg -l libreoffice-writer | grep -E "^ii"', 'libreoffice-writer')
      test_local_command('libreoffice-calc installed?', 'dpkg -l libreoffice-calc | grep -E "^ii"', 'libreoffice-calc')
    end
    tier(2) { test_command_succeeds('libreoffice binary exists', 'test -x /usr/bin/libreoffice') }
  end
end

LibreofficeTest.new.run
