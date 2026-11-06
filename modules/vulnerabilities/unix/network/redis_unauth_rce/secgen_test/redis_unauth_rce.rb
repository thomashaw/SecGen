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

    # This module is a CmdStager exploit with Platform => 'linux', Arch =>
    # [ARCH_X86, ARCH_X64], so the cmd/unix/generic default is "not a compatible
    # payload". A native Linux exec payload still honours the CMD option the
    # sentinel relies on; the x64 variant matches the 64-bit base.
    #
    # CUSTOM => false: by default (CUSTOM true) the module compiles its loadable
    # redis module from source on the attacker and ships that .so to the target
    # to MODULE LOAD. Compiled on Kali-rolling (glibc 2.38+) and loaded on the
    # Debian 12 target (glibc 2.36), it pulls in versioned symbols the target
    # cannot resolve, so the load fails, the trigger runs nothing and no sentinel
    # appears. Forcing CUSTOM false uses the precompiled exp.so that ships with
    # Metasploit (needs only GLIBC_2.2.5, so it loads on Debian 12); its
    # shell.exec then stages the syscall-only exec payload via a plain popen.
    #
    # The command runs as the redis daemon, whose Debian systemd unit sets
    # PrivateTmp=yes and a read-only root (ProtectSystem / ReadOnlyDirectories),
    # so a sentinel in /tmp is written into redis's private namespace (invisible
    # to the root guest-agent read) and /dev/shm is read-only. redis's own data
    # directory /var/lib/redis is the writable, shared location both the daemon
    # and the verifying read see, so the sentinel lands there.
    test_msf_exploit('exploit/linux/redis/redis_replication_cmd_exec',
                     rport: self.port,
                     payload: 'linux/x64/exec',
                     expect_privilege: 'user_rwx',
                     sentinel_dir: '/var/lib/redis',
                     options: { 'CUSTOM' => false },
                     tier: 3)
  end
end

RedisUnauthRceTest.new.run
