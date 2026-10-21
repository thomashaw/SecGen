require_relative '../../../../../lib/post_provision_test'

class CyberchefTest < PostProvisionTest
  def initialize
    self.module_name = 'cyberchef'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_local_command('cyberchef directory exists', 'test -d /opt/cyberchef', '/opt/cyberchef')
      test_local_command('CyberChef.html present', 'test -f /opt/cyberchef/CyberChef.html', '/opt/cyberchef/CyberChef.html')
    end
    tier(2) do
      # Files directory is world-readable (mode 0744)
      test_local_command('CyberChef.html readable', 'test -r /opt/cyberchef/CyberChef.html', '')
      test_local_command('assets directory populated', 'test -d /opt/cyberchef/assets', '/opt/cyberchef/assets')
    end
  end
end

CyberchefTest.new.run
