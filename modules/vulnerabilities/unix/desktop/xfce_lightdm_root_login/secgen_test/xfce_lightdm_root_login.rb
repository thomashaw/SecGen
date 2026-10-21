require_relative '../../../../../lib/post_provision_test'

class XfceLightdmRootLoginTest < PostProvisionTest
  def initialize
    self.module_name = 'xfce_lightdm_root_login'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_local_command('lightdm installed', 'dpkg -l lightdm | grep -E "^ii"', 'lightdm')
    end
    tier(2) do
      # LightDM config must allow root login (either autologin-user=root or pam rule allowing root)
      test_local_command('lightdm root login configured', 'grep -rE "autologin-user=root|pam_succeed_if.so user != anything" /etc/lightdm/', 'root')
    end
    # TODO tier 3 (exploit): deferred — wire up test_msf_exploit when attacker VM is available
  end
end

XfceLightdmRootLoginTest.new.run
