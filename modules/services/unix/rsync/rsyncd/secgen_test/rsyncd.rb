require_relative '../../../../../lib/post_provision_test'

class RsyncdTest < PostProvisionTest
  def initialize
    self.module_name = 'rsyncd'
    self.module_path = get_module_path(__FILE__)
    super
  end

  # The only read_fact is address; it is checked in tiers 1 and 2. The daemon port is fixed
  # at 873 by the module (not an input).
  def test_module
    super
    address = self.json_inputs['address']&.first
    port = 873

    tier(1) do
      test_local_command('rsync installed', 'dpkg -l rsync | grep -E "^ii"', 'rsync')
      test_local_command('rsync enabled at boot', 'systemctl is-enabled rsync', 'enabled')
      test_local_command('rsync service active', 'systemctl is-active rsync', 'active')
      test_local_command('rsyncd.conf includes rsyncd.d', 'grep -E "^&include /etc/rsyncd.d\s*$" /etc/rsyncd.conf', '&include /etc/rsyncd.d')
      test_local_command('rsyncd.d directory exists', 'test -d /etc/rsyncd.d && echo isdir', 'isdir')
      test_local_command('rsyncd.conf holds no module definition', 'grep -qE "^\s*\[" /etc/rsyncd.conf || echo no-modules', 'no-modules')
      test_local_command('configured address', 'grep -E "^address\s*=" /etc/rsyncd.conf', address.to_s) if address
    end

    tier(2) do
      test_service_up(port: port)

      # Listing the daemon works and, with no module files in rsyncd.d, shows no modules.
      # Connect to the bound address (loopback when it is 0.0.0.0).
      target = (address.nil? || address == '0.0.0.0') ? '127.0.0.1' : address
      test_local_command('daemon answers a module list with no modules',
                         "rsync rsync://#{target}/ 2>&1; echo \"rc=$?\"", 'rc=0')
      test_local_command('module list is empty',
                         "test \"$(rsync rsync://#{target}/ 2>/dev/null | wc -l)\" = 0 && echo no-modules", 'no-modules')

      # address: the default 127.0.0.1 must not be reachable from other hosts
      if address
        if address == '0.0.0.0'
          test_local_command('listens on all interfaces', "ss -ltn | grep -E '(0\\.0\\.0\\.0|\\*):#{port}\\s'", ":#{port}")
        else
          test_local_command('listens on the configured address only',
                             "ss -ltn | grep -E ':#{port}\\s'",
                             "#{address}:#{port}")
          test_local_command('does not listen on a wildcard address',
                             "ss -ltn | grep -qE '(0\\.0\\.0\\.0|\\*|\\[::\\]):#{port}\\s' || echo no-wildcard",
                             'no-wildcard')
        end
      end
    end
  end
end

RsyncdTest.new.run
