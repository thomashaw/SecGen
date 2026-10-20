require_relative '../../../../../lib/post_provision_test'

class SqlmapTest < PostProvisionTest
  def initialize
    self.module_name = 'sqlmap'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_local_command('sqlmap cloned', 'test -f /usr/share/sqlmap/sqlmap.py', '/usr/share/sqlmap/sqlmap.py')
      test_local_command('sqlmap symlinked', 'test -L /usr/local/bin/sqlmap', '/usr/local/bin/sqlmap')
    end
    tier(2) { test_command_succeeds('sqlmap runs', 'sqlmap --version') }
  end
end

SqlmapTest.new.run
