require_relative '../../../../../lib/post_provision_test'

class Gitlist040Test < PostProvisionTest
  FLAG_DIR = '/home/git'
  SECRET_REPO_DIR = '/home/git/repositories/secret_files'

  def initialize
    self.module_name = 'gitlist_040'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    inputs = self.json_inputs

    tier(1) do
      test_local_command('gitlist deployed', 'test -s /var/www/gitlist/index.php && echo ok', 'ok')
      test_local_command('gitlist config present', 'test -s /var/www/gitlist/config.ini && echo ok', 'ok')
      test_local_command('apache installed', 'dpkg -l apache2 | grep -E "^ii"', 'apache2')
      test_local_command('php7.4 is the apache handler', 'dpkg -l libapache2-mod-php7.4 | grep -E "^ii"', 'libapache2-mod-php7.4')
    end

    tier(2) do
      test_service_up
      test_html_returned_content('/', '<title>GitList</title>')
    end

    # strings_to_leak + leaked_filenames: dropped as a flag directly under
    # /home/git (not inside a git repository), so only readable by exploiting
    # the vulnerability or getting a shell - checked for presence, not web
    # visibility.
    (inputs['strings_to_leak'] || []).zip(inputs['leaked_filenames'] || []).each_with_index do |(string, filename), i|
      next unless string && filename
      tier(1) { test_local_command("flag #{i} (#{filename}) leaked", "test -s #{FLAG_DIR}/#{filename} && echo ok", 'ok') }
    end

    # strings_to_pre_leak + pre_leaked_filenames: committed into the
    # secret_files git repository, which GitList serves - so these are
    # discoverable through the webapp once the repository is reachable.
    (inputs['strings_to_pre_leak'] || []).zip(inputs['pre_leaked_filenames'] || []).each_with_index do |(string, filename), i|
      next unless string && filename
      tier(1) { test_local_command("pre-leaked file #{i} (#{filename})", "test -s #{SECRET_REPO_DIR}/#{filename} && echo ok", 'ok') }
    end
    if (inputs['pre_leaked_filenames'] || []).any?
      tier(2) { test_html_returned_content('/', 'secret_files') }
    end

    # images_to_leak: written into the same secret_files repository as
    # image<N>.png.
    (inputs['images_to_leak'] || []).each_index do |i|
      tier(1) { test_local_command("leaked image #{i + 1}", "test -s #{SECRET_REPO_DIR}/image#{i + 1}.png && echo ok", 'ok') }
    end

    # port: already exercised via test_service_up/test_html_returned_content
    # above (self.port). No other read_facts to skip.

    # gitlist_arg_injection is a PHP-platform module (defaults to
    # php/meterpreter/reverse_tcp and rejects cmd/unix/*). php/exec runs the
    # sentinel command via PHP with no session, which suits the helper; the
    # command runs as the web user (user_rwx).
    test_msf_exploit('exploit/multi/http/gitlist_arg_injection',
                     rport: self.port,
                     payload: 'php/exec',
                     expect_privilege: 'user_rwx',
                     tier: 3)
  end
end

Gitlist040Test.new.run
