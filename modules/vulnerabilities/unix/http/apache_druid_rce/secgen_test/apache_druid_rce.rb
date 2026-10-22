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
      # Druid is extracted from a tarball to /usr/local/apache-druid (manifests/install.pp)
      test_local_command('druid install exists', 'test -d /usr/local/apache-druid && echo present', 'present')
    end
    tier(2) do
      test_service_up
      # /status returns {"version":"..."} when the coordinator is up
      test_http('/status', match: 'version')
    end
    test_msf_exploit('exploit/linux/http/apache_druid_js_rce',
                     rport: self.port,
                     expect_privilege: 'user_rwx',
                     tier: 3)
  end
end

ApacheDruidRceTest.new.run
