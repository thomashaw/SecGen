require_relative '../../../../../lib/post_provision_test'

class AugeasTest < PostProvisionTest
  # No inputs: the module only installs augeas-tools.
  def initialize
    self.module_name = 'augeas'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) { test_local_command('augeas-tools installed?', 'dpkg -l augeas-tools | grep -E "^ii"', 'augeas-tools') }
    tier(1) do
      test_local_command('augeas-tools package status', "dpkg-query -W -f='${Status}' augeas-tools", 'install ok installed')
      test_local_command('augtool on PATH', 'command -v augtool', 'augtool')
      test_local_command('augparse on PATH', 'command -v augparse', 'augparse')
      test_local_command('stock lenses installed', 'ls /usr/share/augeas/lenses/dist/passwd.aug', 'passwd.aug')
    end
    tier(2) do
      test_command_succeeds('augtool runs', 'echo "quit" | augtool')
      test_command_succeeds('augparse runs', 'augparse --version')
      # a real lookup through the passwd lens
      test_local_command('augtool parses /etc/passwd', 'augtool match /files/etc/passwd/root', '/files/etc/passwd/root')
      test_local_command('augtool reads a value', 'augtool get /files/etc/passwd/root/uid', '= 0')
    end
  end
end

AugeasTest.new.run
