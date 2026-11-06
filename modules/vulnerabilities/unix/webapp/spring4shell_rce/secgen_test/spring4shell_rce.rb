require_relative '../../../../../lib/post_provision_test'

# Spring4Shell (CVE-2022-22965): a Spring MVC 5.3.17 app on Apache Tomcat 8.5.77.
# tier 1 proves Tomcat and the vulnerable WAR are installed and a JDK is present.
# tier 2 proves the app serves and that both halves of the vulnerable combination
#   are in place: Spring Framework 5.3.17 (in the vulnerable 5.3.0-5.3.17 range)
#   and Tomcat 8.5.77 (before the 8.5.78 AccessLogValve hardening). The HTTP check
#   drives the data-binding sink itself (the page echoes request parameters bound
#   onto a POJO), which is the exact mechanism the exploit abuses.
# tier 3 (exploit) is deferred to the attacker-VM test (see the stub below).
#
# Parameterisation: the module declares no <read_fact>, so there are no
# build-time inputs to thread through json_inputs. The port (8080), install path
# (/opt/tomcat) and web root (ROOT -> /) are fixed by the module, so they are
# asserted as literals here.
class Spring4shellRceTest < PostProvisionTest
  CATALINA = '/opt/tomcat'.freeze

  def initialize
    self.module_name = 'spring4shell_rce'
    self.module_path = get_module_path(__FILE__)
    super
    self.port = 8080 # no 'port' input; the module serves on a fixed 8080
  end

  def test_module
    super

    tier(1) do
      test_local_command('Tomcat installed',
                         "test -x #{CATALINA}/bin/catalina.sh && echo ok", 'ok')
      test_local_command('vulnerable WAR deployed',
                         "test -f #{CATALINA}/webapps/ROOT.war && echo ok", 'ok')
      test_local_command('JDK present (exploit needs JDK 9+)',
                         'test -x /usr/bin/java && echo ok', 'ok')
    end

    tier(2) do
      test_service_up

      # The app renders the request parameters bound onto a Person POJO; a hit on
      # our probe proves both that Tomcat has deployed the app and that the
      # data-binding sink is live. Retry while Tomcat starts and unpacks the WAR.
      test_local_command('Spring MVC app serves and binds request params',
                         "for i in $(seq 1 30); do " \
                         "curl -s 'http://127.0.0.1:8080/?name=SecGenProbe&age=42' " \
                         "| grep -q 'Hello, my name is SecGenProbe' && { echo APP_OK; break; }; " \
                         "sleep 3; done", 'APP_OK')

      # Confirm the vulnerable Spring Framework version is the one deployed
      # (unpacked into webapps/ROOT once Tomcat has started).
      test_local_command('vulnerable Spring Framework 5.3.17 deployed',
                         "ls #{CATALINA}/webapps/ROOT/WEB-INF/lib/ 2>/dev/null", 'spring-core-5.3.17.jar')

      # Confirm Tomcat predates the 8.5.78 AccessLogValve hardening (static file,
      # so the check needs no Java/CATALINA_HOME environment).
      test_local_command('Tomcat 8.5.77 (pre-8.5.78 valve hardening)',
                         "grep -i 'Apache Tomcat Version' #{CATALINA}/RELEASE-NOTES", '8.5.77')
    end

    # The module's default target is Java (ARCH_JAVA), for which the helper's
    # default cmd/unix/generic payload is "not a compatible payload". Select the
    # Linux target (index 1: ARCH_X86/X64, Platform linux) and use linux/x64/exec,
    # a no-session command payload whose CMD runs our sentinel directly on the
    # target — the model test_msf_exploit relies on to prove RCE.
    #
    # The Linux target drops a native ELF and register_files_for_cleanup's it, so
    # MSF's driver refuses to run a no-session payload (it has no session to delete
    # the file with): "linux/x64/exec cannot cleanup files created during exploit".
    # AllowNoCleanup bypasses that pre-flight check so the exec payload runs our
    # sentinel; the stray ELF in /tmp is fine on a throwaway teaching VM.
    #
    # TARGETURI defaults to /app/example/HelloWorld.action, but our ROOT.war binds
    # request params at the web root (/), as tier 2 proves. With the default path
    # the check's DefaultAssertionStatus probe hits a 404 instead of the 400 a
    # vulnerable binding returns, so AutoCheck reports not-vulnerable and the
    # exploit never runs. Point TARGETURI at / so both the check and the class
    # loader manipulation drive the live data-binding sink.
    test_msf_exploit('exploit/multi/http/spring_framework_rce_spring4shell',
                     rport: 8080,
                     payload: 'linux/x64/exec',
                     options: { 'TARGET' => 1, 'AllowNoCleanup' => true, 'TARGETURI' => '/' },
                     expect_privilege: 'user_rwx',
                     tier: 3)
  end
end

Spring4shellRceTest.new.run
