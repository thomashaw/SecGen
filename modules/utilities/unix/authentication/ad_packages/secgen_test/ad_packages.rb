require_relative '../../../../../lib/post_provision_test'

class AdPackagesTest < PostProvisionTest
  # Every package install.pp asks for (module has no inputs)
  PACKAGES = %w[realmd sssd sssd-tools libnss-sss libpam-sss adcli samba-common-bin oddjob oddjob-mkhomedir].freeze

  def initialize
    self.module_name = 'ad_packages'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_local_command('realmd installed', 'dpkg -l realmd | grep -E "^ii"', 'realmd')
      test_local_command('sssd installed', 'dpkg -l sssd | grep -E "^ii"', 'sssd')
      test_local_command('adcli installed', 'dpkg -l adcli | grep -E "^ii"', 'adcli')
      PACKAGES.each do |pkg|
        test_local_command("#{pkg} package installed", "dpkg-query -W -f='${Status}' #{pkg}", 'install ok installed')
      end
      # binaries and the NSS/PAM libraries the packages provide
      %w[realm adcli sssd sssctl net oddjobd].each do |bin|
        test_local_command("#{bin} on PATH", "command -v #{bin} || ls /usr/sbin/#{bin}", bin)
      end
      test_local_command('libnss_sss present', 'ls /lib/*/libnss_sss.so.2 /usr/lib/*/libnss_sss.so.2 2>/dev/null', 'libnss_sss.so.2')
      test_local_command('pam_sss present', 'ls /lib/*/security/pam_sss.so /usr/lib/*/security/pam_sss.so 2>/dev/null', 'pam_sss.so')
      test_local_command('oddjob mkhomedir helper present', 'ls /usr/lib/oddjob/mkhomedir /usr/libexec/oddjob/mkhomedir 2>/dev/null', 'mkhomedir')
    end
    tier(2) do
      # these are admin tools, so they run as root
      test_command_succeeds('realm command available', 'realm --version')
      test_command_succeeds('samba-common tools available', 'net --version')
      test_command_succeeds('adcli runs', 'adcli --version')
      test_command_succeeds('sssd runs', 'sssd --version')
      test_command_succeeds('sssctl runs', 'sssctl --help')
      # shared libraries resolve for the NSS and PAM modules
      test_command_succeeds('libnss_sss links cleanly',
                            'f=$(ls /lib/*/libnss_sss.so.2 /usr/lib/*/libnss_sss.so.2 2>/dev/null | head -1); test -n "$f" && ! ldd "$f" | grep -q "not found"')
      test_command_succeeds('pam_sss links cleanly',
                            'f=$(ls /lib/*/security/pam_sss.so /usr/lib/*/security/pam_sss.so 2>/dev/null | head -1); test -n "$f" && ! ldd "$f" | grep -q "not found"')
      # oddjobd has no version flag; it must at least print usage and not fail on missing libraries
      test_command_succeeds('oddjobd links cleanly', '! ldd "$(command -v oddjobd || echo /usr/sbin/oddjobd)" | grep -q "not found"')
    end
  end
end

AdPackagesTest.new.run
