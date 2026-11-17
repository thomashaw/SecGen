require 'shellwords'
require_relative '../../../../../lib/post_provision_test'

class EtcdTest < PostProvisionTest
  def initialize
    self.module_name = 'etcd'
    self.module_path = get_module_path(__FILE__)
    super
  end

  # Both read_facts are checked: port and listen_address.
  def test_module
    super
    port   = self.json_inputs['port']&.first
    listen = self.json_inputs['listen_address']&.first
    ctl    = port ? "ETCDCTL_API=3 etcdctl --endpoints=http://127.0.0.1:#{port}" : nil

    tier(1) do
      test_local_command('etcd server installed', 'dpkg -l etcd-server | grep -E "^ii"', 'etcd-server')
      test_local_command('etcdctl installed', 'which etcdctl', 'etcdctl')
      test_local_command('etcd enabled at boot', 'systemctl is-enabled etcd', 'enabled')
      test_local_command('configured client URL',
                         'grep -E "^ETCD_LISTEN_CLIENT_URLS" /etc/default/etcd',
                         "#{listen}:#{port}") if port && listen
    end

    tier(2) do
      test_service_up
      if ctl
        test_local_command('etcdctl put', "#{ctl} put secgen_probe secgen_value", 'OK')
        test_local_command('etcdctl get', "#{ctl} get secgen_probe --print-value-only", 'secgen_value')
      end

      if listen && port
        if listen == '0.0.0.0'
          test_local_command('listens on all interfaces', "ss -ltn | grep -E '(0\\.0\\.0\\.0|\\*):#{port}\\s'", ":#{port}")
        else
          test_local_command('listens on the configured address only',
                             "ss -ltn | grep -E ':#{port}\\s' | grep -vE '(0\\.0\\.0\\.0|\\*|\\[::\\]):#{port}\\s'",
                             "#{listen}:#{port}")
        end
      end

      # The attacker's view: refused on loopback-only, answered when exposed.
      # SKIPs when the scenario has no attacker VM or the target IP is unknown.
      if listen && port && proxmox? && attack_system && system_present?(attack_system)
        target_ip = system_ip
        if target_ip
          probe = run_on_system(attack_system, "curl -s -m 5 -o /dev/null -w '%{http_code}' http://#{target_ip}:#{port}/health; echo \" rc=$?\"")
          out = probe[:stdout].to_s
          if listen == '0.0.0.0'
            out.include?('200') ? pass_check('attacker can reach etcd when exposed') : fail_check('attacker can reach etcd when exposed', out)
          else
            out.include?('200') ? fail_check('attacker cannot reach etcd by default', out) : pass_check('attacker cannot reach etcd by default')
          end
        end
      end
    end
  end
end

EtcdTest.new.run
