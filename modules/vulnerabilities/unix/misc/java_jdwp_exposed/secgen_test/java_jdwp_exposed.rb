require_relative '../../../../../lib/post_provision_test'

# Inputs (secgen_metadata.xml):
#   port            -> self.port; the JDWP agent listens here (tier 2).
#   unix_username   -> the service account the JVM runs as; its home holds the flag (tier 1).
#   strings_to_leak -> the flag content, leaked into leaked_filenames (tier 1).
#   leaked_filenames-> the file names under the service account's home (tier 1).
class JavaJdwpExposedTest < PostProvisionTest
  APP_DIR = '/opt/jdwp_service'.freeze

  def initialize
    self.module_name = 'java_jdwp_exposed'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super

    user      = self.json_inputs['unix_username']&.first
    filenames = self.json_inputs['leaked_filenames'] || []
    strings   = self.json_inputs['strings_to_leak'] || []

    tier(1) do
      # The bundled Java service compiled at provision time.
      test_local_command('JdwpService compiled', "test -f #{APP_DIR}/JdwpService.class && echo present", 'present')

      # The systemd unit is up.
      test_local_command('jdwp_service unit active', 'systemctl is-active jdwp_service', 'active')

      # The JVM is actually running as the unprivileged service account with the
      # JDWP agent (and the configured port) on its command line. ps -u limits to
      # the service account's processes, so a match proves both the owner and the
      # agent. This is the acceptance criterion for tier 1.
      if user && self.port
        test_local_command("jdwp agent on #{user}'s java command line",
                           "ps -u #{user} -o args= | grep -F agentlib:jdwp", "address=*:#{self.port}")
      end

      # The unprivileged service account exists.
      test_local_command("service account #{user} exists", "id #{user}", "(#{user})") if user

      # The flag was leaked into the account's home, owned by the service
      # account. leak_files zips each filename with the string at the same index.
      filenames.zip(strings).each do |filename, string|
        next if filename.nil? || string.nil?
        path = "/home/#{user}/#{filename}"
        test_local_command("flag #{filename} owned by #{user}", "stat -c '%U' #{path}", user) if user
        test_local_command("flag #{filename} leaked", "cat #{path}", string)
      end
    end

    tier(2) do
      # The JDWP agent is listening on the network port.
      test_service_up

      # JDWP answers the client handshake with the same 14-byte string. The
      # agent reads exactly "JDWP-Handshake" before replying, so the trailing
      # CRLF read_banner adds is harmless.
      test_banner('JDWP-Handshake', send: 'JDWP-Handshake')
    end

    # TODO tier 3 (exploit): deferred — wire up test_msf_exploit when attacker VM is available
    # msf_module: exploit/multi/misc/java_jdwp_debugger
  end
end

JavaJdwpExposedTest.new.run
