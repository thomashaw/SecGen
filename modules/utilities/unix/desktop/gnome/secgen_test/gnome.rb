require_relative '../../../../../lib/post_provision_test'

class GnomeTest < PostProvisionTest
  def initialize
    self.module_name = 'gnome'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) { test_local_command('task-gnome-desktop installed?', 'dpkg -l task-gnome-desktop | grep -E "^ii"', 'task-gnome-desktop') }
    tier(2) { test_command_succeeds('gnome-shell binary exists', 'test -x /usr/bin/gnome-shell') }
  end
end

GnomeTest.new.run
