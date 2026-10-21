require_relative '../../../../../lib/post_provision_test'

class ChkrootkitVulnTest < PostProvisionTest
  def initialize
    self.module_name = 'chkrootkit'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    test_local_command('Chkrootkit binary exists?', 'sudo ls -la /usr/sbin/chkrootkit', 'chkrootkit-0.49')
    test_local_command('Chkrootkit runs?', 'sudo /usr/sbin/chkrootkit -V', 'chkrootkit version 0.49')
    # Tier 3 note: exploit/unix/local/chkrootkit is a LOCAL privilege-escalation module —
    # it requires an existing low-privilege session on the target, not a remote RHOSTS call.
    # test_msf_exploit (which runs from the attacker VM) is not directly applicable.
    # A tier-3 test here needs a two-stage approach: gain initial access, then run the
    # local exploit. Deferred — needs manual authoring or a custom helper.
  end

end
ChkrootkitVulnTest.new.run