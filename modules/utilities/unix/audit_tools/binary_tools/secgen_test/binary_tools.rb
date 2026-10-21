require_relative '../../../../../lib/post_provision_test'

class BinaryToolsTest < PostProvisionTest
  def initialize
    self.module_name = 'binary_tools'
    self.module_path = get_module_path(__FILE__)
    super
  end

  # No inputs: the module only installs binutils.
  def test_module
    super
    tier(1) do
      test_local_command('binutils installed?', 'dpkg -l binutils | grep "^ii"', 'binutils')
      %w[strings objdump readelf nm ld as].each { |bin| test_local_command("#{bin} on PATH", "command -v #{bin}", "/#{bin}") }
    end
    tier(2) do
      test_command_succeeds('strings available', 'strings --version')
      # run the tools against a real binary as an unprivileged user
      test_command_succeeds('strings reads /bin/ls as an unprivileged user', 'strings /bin/ls | head -n 1 | grep -q .', user: 'nobody')
      test_local_command('objdump identifies /bin/ls', 'objdump -f /bin/ls', 'file format elf', user: 'nobody')
      test_local_command('readelf reads the ELF header', 'readelf -h /bin/ls', 'ELF Header', user: 'nobody')
      test_command_succeeds('nm runs as an unprivileged user', 'nm --version', user: 'nobody')
    end
  end
end

BinaryToolsTest.new.run
