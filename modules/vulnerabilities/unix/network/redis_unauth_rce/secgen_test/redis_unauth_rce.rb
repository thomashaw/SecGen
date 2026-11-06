require_relative '../../../../../lib/post_provision_test'

# Post-provision test for the Redis unauthenticated replication module-load RCE
# (exploit/linux/redis/redis_replication_cmd_exec).
#
# Tiers:
#   1  redis-server is installed and the config has been weakened as the module
#      intends (listening on all interfaces, protected mode off, MODULE command
#      enabled).
#   2  the daemon listens on the configured port and answers commands with no
#      authentication (a bare PING returns +PONG rather than a NOAUTH error).
#   3  (deferred) a Metasploit exploit from the attacker VM achieves RCE.
#
# Inputs (secgen_metadata.xml): 'port' is the only read_fact. It is checked in
# the config file and used for the on-the-wire checks via self.port.
class RedisUnauthRceTest < PostProvisionTest
  def initialize
    self.module_name = 'redis_unauth_rce'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super

    port = self.json_inputs['port']&.first

    tier(1) do
      test_local_command('redis-server installed',
                         'dpkg -l redis-server 2>/dev/null | grep -E "^ii"', 'redis-server')
      test_local_command('redis-server binary present', 'which redis-server', 'redis-server')
      # the module rewrites redis.conf to expose the instance
      test_local_command('listens on all interfaces',
                         'grep -E "^bind " /etc/redis/redis.conf', '0.0.0.0')
      test_local_command('protected mode off',
                         'grep -E "^protected-mode " /etc/redis/redis.conf', 'no')
      test_local_command('MODULE command enabled',
                         'grep -E "^enable-module-command " /etc/redis/redis.conf', 'yes')
      test_local_command('configured port in redis.conf',
                         'grep -E "^port " /etc/redis/redis.conf', port) if port
    end

    tier(2) do
      test_service_up
      # a PING with no AUTH first returns +PONG; an authenticated instance would
      # answer -NOAUTH, so this proves the daemon accepts unauthenticated commands
      test_banner('+PONG', send: 'PING', label: 'unauthenticated PING accepted')
    end

    # TODO tier 3 (exploit): deferred — wire up test_msf_exploit when attacker VM is available
    # msf_module: exploit/linux/redis/redis_replication_cmd_exec
  end
end

RedisUnauthRceTest.new.run
