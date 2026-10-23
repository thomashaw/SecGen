require_relative '../../../../../lib/post_provision_test'

class Drupal850Test < PostProvisionTest
  def initialize
    self.module_name = 'drupal_850'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_local_command('drupal docroot exists', 'test -d /var/www/drupal-8.5.0 && echo ok', 'ok')
      test_local_command('apache2 installed', 'dpkg -l apache2 | grep -E "^ii"', 'apache2')
      test_local_command('mod_php installed', 'dpkg -l libapache2-mod-php | grep -E "^ii"', 'libapache2-mod-php')
      test_local_command('mysql 5.7 built', 'test -x /usr/local/mysql/bin/mysqld && echo ok', 'ok')
    end
    tier(2) do
      test_service_up
      # identifies as Drupal, not just "a web server" (R4c).
      #
      # Plain curl via test_local_command, not test_http: on this pipeline's
      # Proxmox/QEMU-guest-agent transport, any literal '%' in the exec'd
      # command is silently dropped before it reaches the guest (confirmed
      # with 'echo FMT_%s_TEST' -> 'FMT_s_TEST' and
      # "curl -w '%{http_code}'" -> a literal '{http_code}' with no digits),
      # which is what test_http's status-code marker (curl -w '%{http_code}')
      # relies on - so test_http always reports "could not connect" here even
      # though the page serves fine. That is a transport-layer bug outside
      # this module (lib/objects/post_provision_test.rb's guest_http_get /
      # the Proxmox exec path), flagged to needs-human rather than fixed here.
      # A plain curl with no '%' in it sidesteps it while still proving the
      # same thing tier 2 needs: the vulnerable app serves real Drupal
      # content on the configured port.
      test_local_command('HTTP / serves Drupal',
                          "curl -sk --max-time 15 http://127.0.0.1:#{self.port}/ 2>/dev/null", 'Drupal')
      test_local_command('mysql service active', 'systemctl is-active mysql', 'active')

      # strings_to_pre_leak: written as a comment into the (statically served,
      # pre-authentication) robots.txt - a dirbuster-style clue reachable
      # before exploitation.
      if (pre_leak = self.json_inputs['strings_to_pre_leak']&.first)
        test_local_command('HTTP /robots.txt pre-leak',
                            "curl -sk --max-time 15 http://127.0.0.1:#{self.port}/robots.txt 2>/dev/null",
                            pre_leak)
      end

      # strings_to_leak is NOT checked here: it is written as a PHP comment
      # inside settings.php, which Apache executes rather than serves as
      # text, so it is unreachable without the RCE this module exists to
      # demonstrate - checking it is tier 3's job (reading the file via the
      # exploited shell), not tier 2's.
    end
    # drupal_drupalgeddon2 is a PHP-platform exploit: the default
    # cmd/unix/generic payload is rejected ("is not a compatible payload");
    # its PHP twin is php/unix/cmd/generic (see the secgen-test-pipeline
    # skill's PHP-platform gotcha). That payload in turn refuses to run
    # without AllowNoCleanup, since it can't remove the PHP file it drops
    # once our sentinel command (not a reverse shell) has run.
    #
    # sentinel_dir: '/dev/shm', not the default /tmp - Debian's apache2.service
    # ships PrivateTmp=yes (confirmed via systemctl show), so a command run by
    # mod_php writes into apache's own private /tmp mount namespace, which our
    # own guest-agent check (outside that namespace) never sees, even though
    # the exploit genuinely ran (confirmed directly: a www-data PHP system()
    # call wrote a file readable from the private-tmp-free docroot/shm, not
    # from /tmp). /dev/shm isn't namespaced by PrivateTmp, so it is visible to
    # both sides.
    test_msf_exploit('exploit/unix/webapp/drupal_drupalgeddon2',
                     rport: self.port,
                     payload: 'php/unix/cmd/generic',
                     options: { 'AllowNoCleanup' => true },
                     sentinel_dir: '/dev/shm',
                     expect_privilege: 'user_rwx',
                     tier: 3)
  end
end

Drupal850Test.new.run
