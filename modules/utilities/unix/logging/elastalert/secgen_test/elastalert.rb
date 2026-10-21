require_relative '../../../../../lib/post_provision_test'

class ElastalertTest < PostProvisionTest
  def initialize
    self.module_name = 'elastalert'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    conf = '/opt/elastalert/config.yaml'
    tier(1) do
      test_local_command('elastalert installed?', 'which elastalert || python3 -m elastalert --help 2>&1 | head -1', 'elastalert')
      test_local_command('elastalert python package installed', 'pip3 show elastalert', 'Name: elastalert')
      test_local_command('supervisor installed', 'dpkg -l supervisor | grep -E "^ii"', 'supervisor')
      test_command_succeeds('elastalert checkout present', 'test -d /opt/elastalert/elastalert')
      test_command_succeeds('elastalert config present', "test -f #{conf}")
      test_command_succeeds('example rule installed', 'test -f /opt/elastalert/rules/example-rule.yaml')
      test_command_succeeds('custom exec alerter installed', 'test -f /opt/elastalert/elastalert/modules/alerter/exec.py')
      test_command_succeeds('elastalert unit installed', 'test -f /etc/systemd/system/elastalert.service')
      test_command_succeeds('index creation script installed', 'test -f /usr/local/bin/elastalert-index.rb')
      test_command_succeeds('index creation unit installed', 'test -f /etc/systemd/system/elastalert-index.service')
      test_local_command('elastalert enabled at boot', 'systemctl is-enabled elastalert', 'enabled')
      test_local_command('elastalert-index enabled at boot', 'systemctl is-enabled elastalert-index', 'enabled')
      # elasticsearch_ip / elasticsearch_port: the config points at the configured elasticsearch
      if (es_ip = self.json_inputs['elasticsearch_ip']&.first)
        test_local_command('es_host configured', "grep '^es_host:' #{conf}", es_ip)
      end
      if (es_port = self.json_inputs['elasticsearch_port']&.first)
        test_local_command('es_port configured', "grep '^es_port:' #{conf}", es_port)
      end
    end
    tier(2) do
      test_command_succeeds('elastalert service running', 'systemctl is-active elastalert')
      test_command_succeeds('elastalert runs', 'python3 -m elastalert.elastalert --help')
      test_command_succeeds('elastalert-test-rule runs', 'elastalert-test-rule --help')
      test_command_succeeds('elastalert loads the custom alerter', 'cd /opt/elastalert && python3 -c "import elastalert.modules.alerter.exec"')
    end
  end
end

ElastalertTest.new.run
