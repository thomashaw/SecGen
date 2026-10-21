require_relative '../../../../../lib/post_provision_test'

# puppet_legacy is a wrapper that makes the legacy accounts/firewall Puppet
# modules available to scenarios. It does not install any system packages itself.
# We verify that Puppet ran (system is reachable) and the module path is present.
class PuppetLegacyTest < PostProvisionTest
  def initialize
    self.module_name = 'puppet_legacy'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      # The module only provides Puppet resources; verify puppet binary is present
      test_command_succeeds('puppet binary available', 'which puppet || which puppet7 || which puppet6 || dpkg -l puppet | grep -E "^ii"')
    end
    tier(2) do
      test_command_succeeds('puppet runs', 'puppet --version 2>&1 || puppet6 --version 2>&1 || puppet7 --version 2>&1')
    end
  end
end

PuppetLegacyTest.new.run
