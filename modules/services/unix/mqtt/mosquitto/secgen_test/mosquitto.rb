require 'shellwords'
require_relative '../../../../../lib/post_provision_test'

class MosquittoTest < PostProvisionTest
  def initialize
    self.module_name = 'mosquitto'
    self.module_path = get_module_path(__FILE__)
    super
  end

  # Every read_fact is checked: port, listen_address, username and password.
  # The password is a throwaway generated one, passed to the clients on the
  # guest only.
  def test_module
    super
    port     = self.json_inputs['port']&.first
    listen   = self.json_inputs['listen_address']&.first
    user     = self.json_inputs['username']&.first
    password = self.json_inputs['password']&.first

    # A listener on 0.0.0.0 is reached over loopback; otherwise use its own address
    host = (listen.nil? || listen == '0.0.0.0') ? '127.0.0.1' : listen
    conf = '/etc/mosquitto/mosquitto.conf'

    tier(1) do
      test_local_command('mosquitto installed', 'dpkg -l mosquitto | grep -E "^ii"', 'mosquitto')
      test_local_command('mosquitto clients installed', 'which mosquitto_pub mosquitto_sub', 'mosquitto_sub')
      test_local_command('mosquitto running', 'systemctl is-active mosquitto', 'active')
      test_local_command('mosquitto enabled at boot', 'systemctl is-enabled mosquitto', 'enabled')
      test_local_command('conf.d is included', "grep -E '^include_dir /etc/mosquitto/conf.d\\s*$' #{conf}", 'include_dir')
      test_local_command('per_listener_settings is on', "grep -E '^per_listener_settings true' #{conf}", 'true')
      test_local_command('configured listener', "grep -E '^listener ' #{conf}", "listener #{port} #{listen}") if port && listen
      test_local_command('user is in the password file', 'cut -d: -f1 /etc/mosquitto/passwd', user.to_s) if user
    end

    tier(2) do
      test_service_up

      # listen_address: 127.0.0.1 stays on loopback, 0.0.0.0 binds every interface
      if listen && port
        if listen == '0.0.0.0'
          test_local_command('listens on all interfaces', "ss -ltn | grep -E '(0\\.0\\.0\\.0|\\*):#{port}\\s'", ":#{port}")
        else
          test_local_command('listens on the configured address', "ss -ltn | grep -E '#{Regexp.escape(listen)}:#{port}\\s'", "#{listen}:#{port}")
        end
      end

      if port && user && password
        auth = "-h #{host} -p #{port} -u #{Shellwords.escape(user)} -P #{Shellwords.escape(password)}"
        # Subscribe in the background, publish, then read what the subscriber saw
        token = "secgen-#{rand(100000..999999)}"
        pubsub = "out=$(mktemp); " \
                 "timeout 15 mosquitto_sub #{auth} -t secgen/test -C 1 > $out 2>&1 < /dev/null & " \
                 "sleep 3; mosquitto_pub #{auth} -t secgen/test -m #{token}; " \
                 "wait; cat $out; rm -f $out"
        test_local_command('configured user can publish and subscribe', pubsub, token)

        # Anonymous and wrong-password connects must be refused
        test_local_command('anonymous connect is refused',
                           "mosquitto_pub -h #{host} -p #{port} -t secgen/test -m x 2>&1", 'not authorised')
        test_local_command('wrong password is refused',
                           "mosquitto_pub -h #{host} -p #{port} -u #{Shellwords.escape(user)} -P wrong-#{Shellwords.escape(password)} -t secgen/test -m x 2>&1",
                           'not authorised')
      end
    end
  end
end

MosquittoTest.new.run
