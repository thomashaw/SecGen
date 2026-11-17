require_relative '../../../../../lib/post_provision_test'

# read_facts: port (self.port, tier 2 + JVM cmdline), unix_username (service
# owner, tier 1), strings_to_leak + leaked_filenames (leaked flag, tier 1).
class JavaJmxUnauthTest < PostProvisionTest
  def initialize
    self.module_name = 'java_jmx_unauth'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    username = self.json_inputs['unix_username']&.first

    tier(1) do
      if username
        test_local_command("#{username} user exists", "id #{username}", username)
      end
      test_local_command('javac available', 'javac -version 2>&1', 'javac')
      test_local_command('JmxApp compiled',
                         'test -f /opt/java_jmx_unauth/classes/com/secgen/JmxApp.class && echo present', 'present')

      # The JVM runs as the service user with the insecure JMX options on its
      # command line (proves the unit is up as that user, not just installed).
      if username
        test_local_command('JMX JVM runs as service user', 'ps -eo user=,args= -C java', username)
      end
      test_local_command('JMX remote agent enabled without auth',
                         'ps -eo args= -C java', 'com.sun.management.jmxremote.authenticate=false')
      test_local_command('JMX remote agent has TLS disabled',
                         'ps -eo args= -C java', 'com.sun.management.jmxremote.ssl=false')
      test_local_command('JMX agent on the configured port',
                         'ps -eo args= -C java', "com.sun.management.jmxremote.port=#{self.port}")
    end

    # leaked flag: leak_files writes strings_to_leak into leaked_filenames in
    # the service user's home.
    tier(1) do
      home = username ? "/home/#{username}" : nil
      if home
        self.json_inputs['leaked_filenames']&.each_with_index do |filename, i|
          leaked_string = self.json_inputs['strings_to_leak']&.at(i)
          leak_path = "#{home}/#{filename}"
          test_local_command("leaked file #{filename} present", "test -f #{leak_path} && echo ok", 'ok')
          if leaked_string
            test_local_command("leaked file #{filename} contains expected string", "cat #{leak_path}", leaked_string)
          end
        end
      end
    end

    tier(2) do
      # JMX RMI registry listening, and reachable off-box (not loopback-only) so
      # a client on the lab VLAN can reach it. The JDK binds the registry on the
      # wildcard, which ss renders as *:PORT (dual-stack), 0.0.0.0:PORT or
      # [::]:PORT depending on the stack — never the literal 0.0.0.0 as a plain
      # substring in every case — so assert "a non-loopback address listens on
      # the port" rather than one exact wildcard spelling.
      test_service_up
      test_local_command('JMX port reachable off-box (not loopback-only)',
                         "ss -ltnH 2>/dev/null | awk '{print $4}' | grep -E ':#{self.port}$' | " \
                         "grep -vE '^(127\\.|\\[::1\\])' | grep -q . && echo REACHABLE",
                         'REACHABLE')
    end

    # TODO tier 3 (exploit): deferred — wire up test_msf_exploit when attacker VM is available
    # msf_module: exploit/multi/misc/java_jmx_server
  end
end

JavaJmxUnauthTest.new.run
