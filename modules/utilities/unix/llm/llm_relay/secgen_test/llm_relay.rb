require_relative '../../../../../lib/post_provision_test'

class LlmRelayTest < PostProvisionTest
  def initialize
    self.module_name = 'llm_relay'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    port = (json_inputs['listen_port'] || ['8080']).first.to_i
    tier(1) { test_command_succeeds('nginx config valid', 'nginx -t') }
    tier(2) do
      test_service_up(port: port)
      test_http('/', status: 403, port: port)
      # Through the relay to the upstream API: any reply from the gateway (401 without a key) shows the relay VM
      # kept its internal NIC and can reach the gateway; 502/504 = it can't.
      code = run_command("curl -s -m 30 -o /dev/null -w '%{http_code}' -X POST -H 'Content-Type: application/json' " \
                         "-d '{}' http://127.0.0.1:#{port}/api/v1/chat/completions")[:stdout].strip
      if %w[200 400 401 422].include?(code)
        pass_check('relay reaches the upstream LLM API', "HTTP #{code}")
      else
        fail_check('relay reaches the upstream LLM API', "HTTP #{code.empty? ? '000' : code}")
      end
      add_evidence('relay interfaces and routes', 'ip -4 -o addr show; ip route')
      add_evidence('nginx error log', 'tail -n 50 /var/log/nginx/error.log')
    end
  end
end

LlmRelayTest.new.run
