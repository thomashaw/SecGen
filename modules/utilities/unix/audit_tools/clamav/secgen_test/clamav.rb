require_relative '../../../../../lib/post_provision_test'

class ClamavTest < PostProvisionTest
  def initialize
    self.module_name = 'clamav'
    self.module_path = get_module_path(__FILE__)
    super
  end

  # No inputs: the module only installs packages.
  def test_module
    super
    tier(1) do
      %w[clamav clamtk clamav-freshclam].each { |pkg| test_local_command("#{pkg} installed", "dpkg -l #{pkg} | grep -E \"^ii\"", pkg) }
      test_local_command('clamscan installed?', 'which clamscan', 'clamscan')
      test_local_command('freshclam installed', 'command -v freshclam', 'freshclam')
      test_local_command('clamtk installed', 'command -v clamtk', 'clamtk')
    end
    tier(2) do
      test_command_succeeds('clamscan reports version', 'clamscan --version')
      test_local_command('clamscan runs as an unprivileged user', 'clamscan --version', 'ClamAV', user: 'nobody')
      test_local_command('freshclam runs as an unprivileged user', 'freshclam --version', 'ClamAV', user: 'nobody')
      # clamtk is a GTK front end with no CLI: check that its launcher is executable
      test_command_succeeds('clamtk launcher is executable', 'test -x "$(command -v clamtk)"', user: 'nobody')
    end
  end
end

ClamavTest.new.run
