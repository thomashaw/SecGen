require_relative '../../../../../lib/post_provision_test'

class GdbserverExposedTest < PostProvisionTest
  def initialize
    self.module_name = 'gdbserver_exposed'
    self.module_path = get_module_path(__FILE__)
    super
  end

  # read_facts: port (tier 2, the listening port), username (the service account
  # the stub runs as — tier 1 unit/user, tier 2 process owner), leaked_filenames
  # (tier 1, the flag file in the account's home). strings_to_leak is the flag
  # content, only reachable once code execution is gained, so it is left to tier 3.
  def test_module
    super

    username = self.json_inputs['username']&.first
    leaked_filenames = self.json_inputs['leaked_filenames'] || []

    tier(1) do
      test_local_command('gdbserver installed', 'test -x /usr/bin/gdbserver && echo ok', 'ok')
      test_local_command('systemd unit present', 'test -f /etc/systemd/system/gdbserver-exposed.service && echo ok', 'ok')

      if username
        test_local_command('service account exists', "id -u #{username} >/dev/null 2>&1 && echo ok", 'ok')
        # the unit runs the stub as the service account, not root (user_rwx)
        test_local_command('unit runs as service account', 'cat /etc/systemd/system/gdbserver-exposed.service', "User=#{username}")
        # flag file leaked into the account's home, owned by that account
        leaked_filenames.each do |fname|
          test_local_command("flag file #{fname} owned by #{username}",
                             "stat -c %U /home/#{username}/#{fname}", username)
        end
      end
    end

    tier(2) do
      # the debug stub is listening on the configured port
      test_service_up(port: self.port)
      if username
        # gdbserver is actually running as the unprivileged service account.
        # Compare UIDs rather than names: `ps -o user=` truncates names longer
        # than 8 chars (e.g. "clever_e+"), but usernames can be up to 20 chars.
        test_local_command('gdbserver running as service account',
                           "test \"$(ps -o uid= -C gdbserver | head -1 | tr -d ' ')\" = \"$(id -u #{username})\" && echo ok",
                           'ok')
      end
      # listening on all interfaces, not just loopback
      test_local_command('listening on all interfaces',
                         "ss -ltn | grep ':#{self.port} '", '0.0.0.0')
    end

    # gdb_server_exec writes a native payload straight into the debuggee's memory
    # and detects the architecture from gdb, so it rejects the helper's default
    # cmd/unix/generic. The amd64 base debugs an x86_64 process, so a native
    # linux/x64/exec payload matches; its CMD option still runs the sentinel
    # command, keeping the proof-of-RCE model intact.
    #
    # The module's DefaultTarget is 0 ('x86', ARCH_X86 only), which narrows the
    # compatible payloads to 32-bit and makes MSF reject linux/x64/exec with
    # "not a compatible payload". Select target 1 ('x86_64') so the x64 payload
    # is accepted and matches the architecture gdb reports for the debuggee.
    #
    # The module resumes the debuggee with continue(read: false): it writes the
    # payload and returns without waiting, so the forked sentinel command runs
    # asynchronously after the exploit reports success. Poll for the sentinel
    # rather than reading it once, so the test does not race the write.
    test_msf_exploit('exploit/multi/gdb/gdb_server_exec',
                     rport: self.port,
                     payload: 'linux/x64/exec',
                     options: { 'TARGET' => 1 },
                     expect_privilege: 'user_rwx',
                     sentinel_wait: 60,
                     tier: 3)
  end
end

GdbserverExposedTest.new.run
