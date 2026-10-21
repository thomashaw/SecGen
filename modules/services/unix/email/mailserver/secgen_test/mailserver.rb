require_relative '../../../../../lib/post_provision_test'

class MailserverTest < PostProvisionTest
  SMTP_PORT = 25
  IMAP_PORT = 143

  def initialize
    self.module_name = 'mailserver'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super

    tier(1) do
      test_local_command('postfix installed', 'dpkg -s postfix | grep ^Status', 'install ok installed')
      test_local_command('dovecot-imapd installed', 'dpkg -s dovecot-imapd | grep ^Status', 'install ok installed')
      test_local_command('dovecot plaintext auth enabled', 'cat /etc/dovecot/dovecot.conf', 'disable_plaintext_auth = no')
    end

    tier(2) do
      # The module has no port input, so pass the ports explicitly.
      test_service_up(port: SMTP_PORT)
      test_service_up(port: IMAP_PORT)
      test_banner('220 ', port: SMTP_PORT, label: 'postfix SMTP greeting')
      test_banner('* OK', port: IMAP_PORT, label: 'dovecot IMAP greeting')
    end
    # No tier 3: a service module has no intended vulnerability to exploit.
  end
end

MailserverTest.new.run
