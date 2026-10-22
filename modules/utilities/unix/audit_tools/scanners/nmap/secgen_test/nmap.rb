require_relative '../../../../../lib/post_provision_test'

class NmapTest < PostProvisionTest
  def initialize
    self.module_name = 'nmap'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) { test_local_command('nmap installed?', 'which nmap', '/usr/bin/nmap') }
    tier(2) do
      test_command_succeeds('nmap reports version', 'nmap --version')
      # a real scan of the local host (ping scan, no ports needed)
      test_local_command('nmap scans localhost', 'nmap -sn 127.0.0.1 2>&1', 'Nmap scan report')
    end
  end
end

NmapTest.new.run
