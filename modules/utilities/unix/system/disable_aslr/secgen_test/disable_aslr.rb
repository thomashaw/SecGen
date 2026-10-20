require_relative '../../../../../lib/post_provision_test'

class DisableAslrTest < PostProvisionTest
  def initialize
    self.module_name = 'disable_aslr'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_command_succeeds('ASLR sysctl config exists',
                            'test -f /etc/sysctl.d/01-disable-aslr.conf')
      test_local_command('ASLR config has correct value',
                         'cat /etc/sysctl.d/01-disable-aslr.conf',
                         'randomize_va_space = 0')
    end
    tier(2) do
      # Verify the kernel parameter is actually 0 after boot/sysctl apply
      test_local_command('ASLR disabled at runtime',
                         'cat /proc/sys/kernel/randomize_va_space',
                         '0')
    end
  end
end

DisableAslrTest.new.run
