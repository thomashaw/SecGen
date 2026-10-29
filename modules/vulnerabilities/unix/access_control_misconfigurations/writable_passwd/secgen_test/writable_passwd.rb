require_relative '../../../../../lib/post_provision_test'

# Inputs (secgen_metadata.xml):
#   strings_to_leak, leaked_filenames: checked below (tier 1), each string must
#   be in a root-only file under /root.
class WritablePasswdTest < PostProvisionTest
  PASSWD = '/etc/passwd'.freeze

  def initialize
    self.module_name = 'writable_passwd'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super

    tier(1) do
      test_local_command('/etc/passwd is mode 777', "stat -c '%a' #{PASSWD}", '777')
      test_local_command('/etc/passwd is owned by root', "stat -c '%U' #{PASSWD}", 'root')
      check_leaked_strings
    end

    tier(2) { check_writable_by_low_privilege_user }
  end

  private

  # leak_files pairs strings with filenames in order; once the filenames run
  # out, the remaining strings are appended to the first file.
  def check_leaked_strings
    strings   = self.json_inputs['strings_to_leak'] || []
    filenames = self.json_inputs['leaked_filenames'] || []
    return skip_check('leaked strings', 'no strings_to_leak or leaked_filenames supplied') if strings.empty? || filenames.empty?

    strings.each_with_index do |string, i|
      path = "/root/#{filenames[i] || filenames.first}"
      out = run_vagrant_ssh("cat #{Shellwords.escape(path)}")
      name = "leaked string #{i + 1} present in #{path}"
      if out[:stdout].include?(string)
        pass_check(name, nil, tier: 1)
      else
        fail_check(name, "#{path} does not contain the expected string", tier: 1,
                   evidence: "$ cat #{path}\n--- stdout\n#{out[:stdout]}\n--- stderr\n#{out[:stderr]}")
      end
    end
    mode = filenames.uniq.map { |f| Shellwords.escape("/root/#{f}") }.join(' ')
    test_command_succeeds('leaked files are all 0600 root',
                          "[ \"$(stat -c '%a %U' #{mode} | sort -u)\" = '600 root' ]", tier: 1)
  end

  # An existing unprivileged account (nobody) can open /etc/passwd for writing.
  # No account is created for the test: useradd rewrites /etc/passwd, which
  # could change the mode we are trying to observe. The append is empty, so
  # nothing is changed and the system is not taken to root.
  def check_writable_by_low_privilege_user
    user = 'nobody'
    test_command_succeeds('unprivileged user can open /etc/passwd for writing',
                          ": >> #{PASSWD} || { ls -l #{PASSWD} >&2; id >&2; exit 1; }", user: user)
    test_command_succeeds('unprivileged user is not root', '[ "$(id -u)" -ne 0 ]', user: user)
    # The flag files stay protected: the user needs the vulnerability to reach them.
    (self.json_inputs['leaked_filenames'] || []).uniq.each do |f|
      test_command_succeeds("unprivileged user cannot read /root/#{f}",
                            "! cat #{Shellwords.escape("/root/#{f}")} >/dev/null 2>&1", user: user)
    end
  end
end

WritablePasswdTest.new.run
