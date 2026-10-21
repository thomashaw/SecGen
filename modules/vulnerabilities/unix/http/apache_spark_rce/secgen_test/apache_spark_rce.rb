require_relative '../../../../../lib/post_provision_test'

class ApacheSparkRceTest < PostProvisionTest
  def initialize
    self.module_name = 'apache_spark_rce'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    username = self.json_inputs['unix_username']&.first

    tier(1) do
      if username
        test_local_command("#{username} user exists", "id #{username}", username)
      end
      test_local_command('spark install exists', 'test -d /opt/spark && echo present', 'present')
      test_local_command('java available', 'java -version 2>&1', 'version')
    end
    tier(2) do
      test_service_up
      # Spark UI returns an HTML page with "Spark" in the title
      test_http('/', match: 'Spark')
    end
    # TODO tier 3 (exploit): deferred — wire up test_msf_exploit when attacker VM is available
    # msf_module: exploit/linux/http/apache_spark_rce_cve_2022_33891 (CVE-2022-33891)
  end
end

ApacheSparkRceTest.new.run
