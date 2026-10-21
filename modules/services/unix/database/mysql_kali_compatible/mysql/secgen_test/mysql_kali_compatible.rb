require_relative '../../../../../lib/post_provision_test'

class MysqlKaliCompatibleTest < PostProvisionTest
  def initialize
    self.module_name = 'mysql_kali_compatible'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) { test_local_command('mysqld installed?', 'which mysqld || dpkg -l mysql-server | grep -E "^ii"', 'mysql') }
    tier(2) { test_local_command('mysqld running?', 'ps -ef | grep mysqld', '/usr/sbin/mysqld') }
    tier(2) { test_service_up(port: 3306) }
  end
end

MysqlKaliCompatibleTest.new.run
