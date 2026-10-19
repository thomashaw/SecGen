require_relative '../../../../../lib/post_provision_test'

class GhidraTest < PostProvisionTest
  def initialize
    self.module_name = 'ghidra'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) { test_local_command('ghidra installed?', 'ls /opt/ghidra/ghidraRun /usr/local/bin/ghidra', '/usr/local/bin/ghidra') }
    # the headless analyser starts under the installed JDK and prints its usage when given no arguments
    tier(2) { test_local_command('analyzeHeadless runs', 'timeout 120 /opt/ghidra/support/analyzeHeadless 2>&1', 'Headless Analyzer Usage') }
  end
end

GhidraTest.new.run
