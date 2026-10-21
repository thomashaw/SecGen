require_relative '../../../../../lib/post_provision_test'

class MySQLWheezyTest < PostProvisionTest
  # The metadata declares no read_fact or default_input, so there are no inputs
  # to check; the port is the fixed MySQL port.
  def initialize
    self.module_name = 'mysql_wheezy_compatible'
    self.module_path = get_module_path(__FILE__)
    super
    self.port = 3306
  end

  def test_module
    super
    tier(1) do
      test_local_command('mysql server package installed?', 'dpkg -l | grep -E "^ii\s+mysql-server"', 'mysql-server')
      test_local_command('mysqld binary present', 'test -x /usr/sbin/mysqld && echo present || test -x /usr/bin/mysqld && echo present', 'present')
    end
    tier(2) do
      test_local_command('mysqld process running?', 'ps -ef | grep mysqld', '/usr/bin/mysqld')
      test_service_up
      # the server answers a query over the local socket
      test_local_command('server answers a query', 'mysql -N -e "select 1"', '1')
    end
  end
end

MySQLWheezyTest.new.run
