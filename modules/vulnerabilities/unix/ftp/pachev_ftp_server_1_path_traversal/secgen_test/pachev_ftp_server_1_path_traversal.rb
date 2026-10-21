require_relative '../../../../../lib/post_provision_test'

class PachevFtpServer1PathTraversalTest < PostProvisionTest
  def initialize
    self.module_name = 'pachev_ftp_server_1_path_traversal'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      # Pachev FTP server is built from source with Cargo
      test_local_command('pachev binary built', 'test -f /opt/pachev_ftp/pachev_ftp-master/ftp_server/target/release/ftp_server', '/opt/pachev_ftp')
      test_local_command('pachevftp service file present', 'test -f /etc/systemd/system/pachevftp.service', '/etc/systemd')
    end
    tier(2) do
      # port input controls the external port; iptables NAT redirects it to 2121
      test_service_up
    end
    # TODO tier 3 (exploit): deferred — path traversal via FTP LIST command from attacker VM
    # Note: metadata marks this module as conflicting with all others (disabled — cargo build fails online)
  end
end

PachevFtpServer1PathTraversalTest.new.run
