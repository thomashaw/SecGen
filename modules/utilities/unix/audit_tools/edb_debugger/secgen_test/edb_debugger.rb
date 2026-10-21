require_relative '../../../../../lib/post_provision_test'

class EdbDebuggerTest < PostProvisionTest
  def initialize
    self.module_name = 'edb_debugger'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) { test_local_command('edb-debugger installed?', 'dpkg -l edb-debugger | grep "^ii"', 'edb-debugger') }
    # edb is a GUI app; confirm the binary is executable rather than launching it
    tier(2) { test_local_command('edb binary present', 'test -x /usr/bin/edb && echo "ok"', 'ok') }
  end
end

EdbDebuggerTest.new.run
