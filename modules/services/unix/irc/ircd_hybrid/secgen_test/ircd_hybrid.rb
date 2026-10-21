require_relative '../../../../../lib/post_provision_test'

class IrcdHybridTest < PostProvisionTest
  def initialize
    self.module_name = 'ircd_hybrid'
    self.module_path = get_module_path(__FILE__)
    super
    self.port = 6667 # module has no port input; ircd-hybrid's default
  end

  def test_module
    super
    # Tier 1: package installed and service running
    test_local_command('ircd-hybrid package', 'dpkg -s ircd-hybrid', 'Status: install ok installed', tier: 1)
    test_local_command('ircd-hybrid service', 'systemctl is-active ircd-hybrid', 'active', tier: 1)
    # Tier 2: IRC port open and the daemon greets with its AUTH notices
    test_service_up
    test_banner('NOTICE', label: 'ircd-hybrid greets with an IRC NOTICE')
  end
end

IrcdHybridTest.new.run
