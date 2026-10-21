require_relative '../../../../../lib/post_provision_test'

class NTPTest < PostProvisionTest
  def initialize
    self.module_name = 'ntp'
    self.module_path = get_module_path(__FILE__)
    super
    self.port = 123
  end

  def test_module
    super
    tier(1) do
      test_local_command('ntp package installed', 'dpkg -s ntp', 'Status: install ok installed')
      test_local_command('ntpd binary present', 'which ntpd', 'ntpd')
    end
    tier(2) do
      test_command_succeeds('ntp service active', 'systemctl is-active ntp')
      # NTP is UDP, so test_service_up (TCP) does not apply
      test_local_command("ntpd listening on UDP #{port}", 'ss -lun', ":#{port} ")
      # ntpq sends a real NTP control query to the daemon over UDP 123 on the guest
      test_command_succeeds('ntpd answers ntpq query', 'ntpq -pn')
    end
  end
end

NTPTest.new.run
