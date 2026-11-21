require 'shellwords'
require_relative '../../../../../lib/post_provision_test'

class MemcachedTest < PostProvisionTest
  def initialize
    self.module_name = 'memcached'
    self.module_path = get_module_path(__FILE__)
    super
  end

  LOOPBACK = %w[127.0.0.1 localhost ::1].freeze

  # Every read_fact is checked: listen, port and memory_mb (config file, listening
  # socket and the running process). An attacker VM, when the scenario has one,
  # checks reachability from off the box: refused for a loopback listen, `stats`
  # answered for any other address.
  def test_module
    super
    listen = self.json_inputs['listen']&.first
    port   = self.json_inputs['port']&.first
    mem    = self.json_inputs['memory_mb']&.first
    conf   = '/etc/memcached.conf'

    tier(1) do
      test_local_command('memcached installed', 'dpkg -l memcached | grep -E "^ii"', 'memcached')
      test_local_command('memcached enabled at boot', 'systemctl is-enabled memcached', 'enabled')
      test_local_command('configured listen address', "grep -E '^-l ' #{conf}", "-l #{listen}") if listen
      test_local_command('configured port', "grep -E '^-p ' #{conf}", "-p #{port}") if port
      test_local_command('configured memory', "grep -E '^-m ' #{conf}", "-m #{mem}") if mem
      test_local_command('UDP disabled in config', "grep -E '^-U ' #{conf}", '-U 0')
    end

    tier(2) do
      test_service_up
      test_command_succeeds('memcached service is active', 'systemctl is-active --quiet memcached')
      if port
        test_local_command('stats answers on loopback',
                           "printf 'stats\\r\\nquit\\r\\n' | timeout 5 bash -c 'exec 3<>/dev/tcp/127.0.0.1/#{port}; cat >&3; cat <&3'",
                           'STAT pid') if listen && (LOOPBACK.include?(listen) || listen == '0.0.0.0')
        # the limit (-m) shows up as limit_maxbytes in `stats`
        if mem
          test_local_command('memory limit applied',
                             "printf 'stats\\r\\nquit\\r\\n' | timeout 5 bash -c 'exec 3<>/dev/tcp/127.0.0.1/#{port}; cat >&3; cat <&3'",
                             "STAT limit_maxbytes #{mem.to_i * 1024 * 1024}")
        end
        # UDP stays off: no UDP socket on the port
        test_local_command('no UDP listener', "ss -lun | grep -c ':#{port}\\s' ; true", '0')
      end

      if listen && port
        if LOOPBACK.include?(listen)
          test_local_command('bound to loopback only',
                             "ss -ltn | grep -E ':#{port}\\s' | grep -vE '(127\\.0\\.0\\.1|\\[::1\\]):#{port}\\s' | wc -l",
                             '0')
        elsif listen == '0.0.0.0'
          test_local_command('bound to all interfaces', "ss -ltn | grep -E '(0\\.0\\.0\\.0|\\*):#{port}\\s'", ":#{port}")
        end
        attacker_checks(listen, port)
      end
    end
  end

  # From the attacker VM: loopback-only memcached is unreachable, otherwise stats works.
  def attacker_checks(listen, port)
    name = LOOPBACK.include?(listen) ? 'attacker cannot reach memcached' : 'attacker gets stats'
    resolved = resolve_attacker(name, nil, nil, 2)
    return unless resolved
    attacker, _attacker_ip, rhost = resolved
    if LOOPBACK.include?(listen)
      # control: the target must answer ping, or a refused connection proves nothing
      ping = run_on_system(attacker, "ping -c 2 -W 3 #{rhost} >/dev/null 2>&1; echo \"rc=$?\"", timeout: 30)[:stdout].to_s
      unless ping.include?('rc=0')
        return skip_check(name, "#{attacker} cannot ping #{rhost}, so a refused memcached connection would prove nothing", tier: 2)
      end
    end
    probe = "printf 'stats\\r\\nquit\\r\\n' | timeout 8 bash -c 'exec 3<>/dev/tcp/#{rhost}/#{port}; cat >&3; cat <&3' 2>&1; echo \"rc=$?\""
    out = run_on_system(attacker, probe, timeout: 30)[:stdout].to_s
    if LOOPBACK.include?(listen)
      if out.include?('STAT pid')
        fail_check(name, "#{name}: stats answered from #{attacker} although listen is #{listen}", tier: 2, evidence: out)
      else
        pass_check(name, "#{name}: no stats from #{attacker} to #{rhost}:#{port} (listen #{listen})", tier: 2)
      end
    elsif out.include?('STAT pid')
      pass_check(name, "#{name}: #{attacker} got stats from #{rhost}:#{port}", tier: 2)
    else
      fail_check(name, "#{name}: no stats from #{attacker} to #{rhost}:#{port} (listen #{listen}): #{out.strip[0, 200]}", tier: 2, evidence: out)
    end
  end
end

MemcachedTest.new.run
