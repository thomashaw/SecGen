require_relative '../../../../../lib/post_provision_test'

class GhidraTest < PostProvisionTest
  def initialize
    self.module_name = 'ghidra'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) { test_local_command('ghidra installed?', 'ls /opt/ghidra/ghidraRun /usr/local/bin/ghidra', '/usr/local/bin/ghidra') }

    inputs = json_inputs || {}
    llm = inputs['llm_assistant']&.first == 'true'
    kali = run_command('grep -qi kali /etc/os-release && echo KALI')[:stdout].include?('KALI')
    if llm && kali
      test_llm_assistant(inputs)
    else
      tier(1) { test_command_succeeds('GhidrAssist not installed (llm_assistant off)', '! ls /opt/ghidra/Ghidra/Extensions/GhidrAssist') }
    end
  end

  def test_llm_assistant(inputs)
    tier(1) do
      test_command_succeeds('GhidrAssist extension installed', 'test -s /opt/ghidra/Ghidra/Extensions/GhidrAssist/lib/GhidrAssist.jar')
      # the settings dir is ghidra_<application.version>_<release.name>: check the version we wrote matches the install
      props = run_command('grep -E "^application\.(version|release\.name)=" /opt/ghidra/Ghidra/application.properties')[:stdout]
      version = props[/application\.version=(\S+)/, 1]
      release = props[/application\.release\.name=(\S+)/, 1]
      prefs = "~/.config/ghidra/ghidra_#{version}_#{release}/preferences"
      test_command_succeeds('preferences up to date for all users', '/usr/local/sbin/secgen-ghidra-llm-prefs --check')
      test_command_succeeds("/etc/skel has #{prefs}", "grep -q '^GhidrAssist.APIProviders=' /etc/skel/#{prefs.sub('~/', '')}")
      add_evidence('ghidra settings dirs', 'ls -la /home/*/.config/ghidra/ /root/.config/ghidra/ 2>&1')
    end

    # Does the configured API answer? Any reply from the gateway (401 with the placeholder key) means the route
    # (relay or direct) works; 000 = unreachable, 403/404 = relay refused, 502/504 = relay can't reach the gateway.
    relay_ip = inputs['llm_relay_ip']&.first.to_s
    url = relay_ip.empty? ? inputs['llm_api_url'].first : "http://#{relay_ip}:#{inputs['llm_relay_port'].first}/api/v1/"
    url += '/' unless url.end_with?('/')
    tier(2) do
      cmd = "curl -s -m 30 --noproxy '*' -o /dev/null -w '%{http_code}' -X POST -H 'Content-Type: application/json' " \
            "-d '{\"model\":\"#{inputs['llm_models'].first}\",\"messages\":[{\"role\":\"user\",\"content\":\"hi\"}],\"max_tokens\":1}' " \
            "#{url}chat/completions"
      code = run_command(cmd)[:stdout].strip
      if %w[200 401].include?(code)
        pass_check("LLM API answers at #{url}", "HTTP #{code}")
      else
        fail_check("LLM API answers at #{url}", "HTTP #{code.empty? ? '000' : code}")
      end
    end
  end
end

GhidraTest.new.run
