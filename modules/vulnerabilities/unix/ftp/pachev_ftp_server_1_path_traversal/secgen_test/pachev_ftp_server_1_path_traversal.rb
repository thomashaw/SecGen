require_relative '../../../../../lib/post_provision_test'

# The server binds 127.0.0.1:2121; an iptables DNAT rule (applied when the
# network comes up) forwards the module's port to it. Locally generated
# traffic skips PREROUTING, so the guest-side checks talk to 2121 directly.
class PachevFtpServer1PathTraversalTest < PostProvisionTest
  def initialize
    self.module_name = 'pachev_ftp_server_1_path_traversal'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    release_dir = '/opt/pachev_ftp/pachev_ftp-master/ftp_server/target/release'
    tier(1) do
      # Pachev FTP server is built offline from the vendored crates with Cargo
      test_command_succeeds('ftp_server binary built', "test -x #{release_dir}/ftp_server")
      test_command_succeeds('pachevftp service file present', 'test -f /etc/systemd/system/pachevftp.service')
    end
    tier(2) do
      test_command_succeeds('pachevftp service active', 'systemctl is-active pachevftp')
      test_service_up(port: 2121)
      test_banner('Welcome', port: 2121)
      test_local_command("port #{port} redirected to 2121", 'iptables -t nat -S PREROUTING', '127.0.0.1:2121')
    end
    # TODO tier 3 (exploit): deferred — path traversal via FTP LIST command from attacker VM
  end
end

PachevFtpServer1PathTraversalTest.new.run
