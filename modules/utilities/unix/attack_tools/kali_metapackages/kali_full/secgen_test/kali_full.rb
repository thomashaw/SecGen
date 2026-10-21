require_relative '../../../../../lib/post_provision_test'

class KaliFullTest < PostProvisionTest
  def initialize
    self.module_name = 'kali_full'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    # No inputs. The module only installs the kali-linux-everything metapackage.
    tier(1) do
      test_local_command('kali-linux-everything installed', 'dpkg -l kali-linux-everything | grep -E "^ii"', 'kali-linux-everything')
      %w[nmap sqlmap msfconsole john hydra].each do |tool|
        test_local_command("#{tool} on PATH", "which #{tool}", tool)
      end
    end
    tier(2) do
      test_command_succeeds('nmap available', 'nmap --version')
      test_command_succeeds('sqlmap runs', 'sqlmap --version')
      test_command_succeeds('john runs', 'john --list=formats')
      test_local_command('hydra runs', 'hydra -h 2>&1', 'Hydra')
      test_command_succeeds('msfconsole runs', 'export HOME=/root TERM=dumb; msfconsole --version')
    end
  end
end

KaliFullTest.new.run
