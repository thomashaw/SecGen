require_relative '../../../../../lib/post_provision_test'

class VsftpdTest < PostProvisionTest
  def initialize
    self.module_name = 'vsftpd'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    test_service_up
    # Tier 2: banner set by vsftpd.pp (ftpd_banner); proves vsftpd (not another FTP daemon) answers
    test_banner('Hello, welcome to this vsftpd server!', label: 'FTP banner', tier: 2)
  end
end

VsftpdTest.new.run