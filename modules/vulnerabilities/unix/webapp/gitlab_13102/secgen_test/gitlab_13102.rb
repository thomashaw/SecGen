require_relative '../../../../../lib/post_provision_test'

class Gitlab13102Test < PostProvisionTest
  def initialize
    self.module_name = 'gitlab_13102'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_local_command('gitlab-ce package installed', 'dpkg -l gitlab-ce | grep -E "^ii"', 'gitlab-ce')
    end
    tier(2) do
      test_service_up(port: 80)
      test_http('/users/sign_in', port: 80, match: 'GitLab')
    end
    # TODO tier 3 (exploit): deferred — wire up test_msf_exploit (exploit/multi/http/gitlab_exif_rce) when attacker VM is available
  end
end

Gitlab13102Test.new.run
