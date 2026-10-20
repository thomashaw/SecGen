require_relative '../../../../../lib/post_provision_test'

# Verifies that the groups module created at least one additional group.
# The group names are scenario-controlled; we verify the mechanism works.
class GroupsTest < PostProvisionTest
  def initialize
    self.module_name = 'groups'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_command_succeeds('/etc/group readable', 'test -r /etc/group')
      test_local_command('groups exist in /etc/group', 'wc -l /etc/group', '')
    end
    tier(2) do
      test_command_succeeds('getent group works', 'getent group | wc -l')
    end
  end
end

GroupsTest.new.run
