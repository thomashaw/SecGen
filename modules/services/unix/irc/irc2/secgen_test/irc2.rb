require_relative '../../../../../lib/post_provision_test'

class IRC2Test < PostProvisionTest
  def initialize
    self.module_name = 'irc2'
    self.module_path = get_module_path(__FILE__)
    super
    self.port = 6667
  end

  def test_module
    super
    # Tier 1: package installed
    test_command_succeeds('ircd-irc2 package installed', 'dpkg -s ircd-irc2', tier: 1)
    # Tier 2: daemon listening on the IRC port
    test_service_up
  end
end

IRC2Test.new.run