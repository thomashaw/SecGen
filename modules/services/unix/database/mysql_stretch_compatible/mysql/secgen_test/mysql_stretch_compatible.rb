require_relative '../../../../../lib/post_provision_test'

class MySQLStretchTest < PostProvisionTest
  # The metadata declares no read_fact or default_input, so there are no inputs
  # to check; the port is the fixed MySQL port.
  def initialize
    self.module_name = 'mysql_stretch_compatible'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      # mysql::server and mysql::client are both installed
      test_local_command('mysql client installed?', 'which mysql', 'mysql')
      test_local_command('mysql server package installed?', 'dpkg -l | grep -E "^ii\s+(mysql|mariadb)-server"', 'server')
      # override_options disable SSL
      test_local_command('ssl disabled in config', 'grep -rE "ssl-disable" /etc/mysql/', 'ssl-disable')
    end
    tier(2) do
      test_local_command('mysqld process running?', 'ps -ef | grep mysqld', '/usr/sbin/mysqld')
      test_service_up(port: 3306)
      # the server answers a query over the local socket
      test_local_command('server answers a query', 'mysql -N -e "select 1"', '1')
    end
  end
end

MySQLStretchTest.new.run
