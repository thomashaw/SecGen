require_relative '../../../../../lib/post_provision_test'

class ApacheDruidRceTest < PostProvisionTest
  def initialize
    self.module_name = 'apache_druid_rce'
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
      # install.pp moves the release to /usr/local/apache-druid; druid.service runs this script
      test_local_command('druid start script installed',
                         'test -x /usr/local/apache-druid/bin/start-nano-quickstart && echo present', 'present')
    end
    tier(2) do
      test_service_up
      # /status returns {"version":"..."} when the coordinator is up
      test_http('/status', match: 'version')
    end
    # TODO tier 3 (exploit): deferred — wire up test_msf_exploit when attacker VM is available
    # msf_module: exploit/linux/http/apache_druid_js_rce (CVE-2021-25646)
  end
end

ApacheDruidRceTest.new.run
