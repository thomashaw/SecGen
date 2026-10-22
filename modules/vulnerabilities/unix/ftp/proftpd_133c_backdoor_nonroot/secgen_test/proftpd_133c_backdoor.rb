require_relative '../../../../../lib/post_provision_test'

class Proftpd133cBackdoorTest < PostProvisionTest
  def initialize
    self.module_name = 'proftpd_133c_backdoor_nonroot'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) { test_command_succeeds('proftpd installed', 'test -x /usr/local/sbin/proftpd') }

    tier(2) do
      test_service_up
      if (server_name = self.json_inputs['server_name']&.first)
        test_banner(server_name)
      end
    end

    test_msf_exploit('exploit/unix/ftp/proftpd_133c_backdoor',
                     rport: self.port,
                     tier: 3)
  end
end

Proftpd133cBackdoorTest.new.run
