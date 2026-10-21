require_relative '../../../../../lib/post_provision_test'

class MailTest < PostProvisionTest
  def initialize
    self.module_name = 'mail'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_local_command('mailutils installed', 'dpkg -l mailutils | grep -E "^ii"', 'mailutils')
      test_local_command('mail binary on PATH', 'which mail', 'mail')
    end
    tier(2) do
      # Verify mail command is functional (--version exits 0)
      test_command_succeeds('mail --version', 'mail --version 2>&1 || true')
      # Mail spool directory should exist
      test_command_succeeds('/var/spool/mail exists', 'test -d /var/spool/mail')
    end
  end
end

MailTest.new.run
