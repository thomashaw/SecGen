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

    # java_jdwp_debugger drops and runs a native ELF payload (no CMD option,
    # Arch x86/x64), so the helper's default cmd/unix/generic is incompatible
    # and the sentinel command never runs. linux/x64/exec is a no-session
    # native payload whose CMD (set by the helper to the sentinel) executes on
    # the x86_64 target.
    #
    # The module drops the ELF, chmod +x's it, then launches it through
    # Java Runtime.exec() and returns without waiting, so the payload process
    # writes the sentinel a moment after the exploit reports success. Poll for
    # it rather than read once immediately.
    test_msf_exploit('exploit/multi/misc/java_jdwp_debugger',
                     rport: self.port,
                     payload: 'linux/x64/exec',
                     expect_privilege: 'user_rwx',
                     sentinel_wait: 60,
                     tier: 3)
  end
end

JavaJdwpExposedTest.new.run
