require_relative '../../../../../lib/post_provision_test'

class IRC2Test < PostProvisionTest
  def initialize
    self.module_name = 'irc2'
    self.module_path = get_module_path(__FILE__)
    super
    self.port = 6667 # module has no port input; fixed by the P line in files/ircd.conf
  end

  def test_module
    super
    # No read_facts or default_inputs: nothing parameterised to check.
    tier(1) do
      test_local_command('ircd-irc2 package', 'dpkg -s ircd-irc2', 'Status: install ok installed')
      test_local_command('ircd.conf deployed', 'cat /etc/ircd.conf', 'irc.localhost')
      test_local_command('ircd.motd deployed', 'cat /etc/ircd.motd', 'Welcome')
      test_local_command('service enabled', 'systemctl is-enabled ircd-irc2', 'enabled')
      test_local_command('service active', 'systemctl is-active ircd-irc2', 'active')
    end
    tier(2) do
      test_service_up
      # a client can register and gets the welcome numeric from the configured server
      test_local_command('IRC registration welcomed',
                         %q(timeout 10 bash -c 'exec 3<>/dev/tcp/127.0.0.1/6667; printf "NICK secgen\r\nUSER secgen 0 * :secgen\r\n" >&3; timeout 3 cat <&3' 2>/dev/null | head -c 2000),
                         ' 001 ')
    end
  end
end

IRC2Test.new.run
