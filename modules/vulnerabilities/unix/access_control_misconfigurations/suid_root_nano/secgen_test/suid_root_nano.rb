require_relative '../../../../../lib/post_provision_test'


class SUIDNanoTest < PostProvisionTest
  NANO_PATHS = ['/bin/nano', '/usr/bin/nano'].freeze

  def initialize
    self.module_name = 'suid_root_nano'
    self.module_path = get_module_path(__FILE__)
    super
  end

  # The module declares no read_fact or default_input, so there are no
  # input values to check through json_inputs.
  def test_module
    super

    tier(1) do
      test_local_command('nano installed', 'dpkg -s nano', 'Status: install ok installed')
      NANO_PATHS.each do |path|
        # mode 4755 (setuid, rwxr-xr-x), owned by root
        test_local_command("#{path} setuid root", "stat -L -c '%a %U' #{path}", '4755 root')
      end
    end

    tier(2) do
      test_local_command('nano runs', '/bin/nano --version', 'GNU nano')
      test_nano_runs_with_euid_0
    end
  end

  # Start nano as an unprivileged user (under a pty, so it stays running) and
  # read its real and effective uids from /proc: the setuid bit must give
  # euid 0 to a process whose real uid is not root. Nothing is exploited.
  def test_nano_runs_with_euid_0
    user = "secgen_t2_#{rand(100_000)}"
    inner = '(sleep 12) | script -qec "/bin/nano /tmp/secgen_nano_t2.txt" /dev/null >/dev/null 2>&1 & ' \
            'sleep 4; pid=$(pgrep -U "$(id -u)" -x nano | head -1); ' \
            '[ -n "$pid" ] && awk \'/^Uid:/{print "ruid=" $2 " euid=" $3}\' /proc/$pid/status'
    cmd = "useradd -m -s /bin/bash #{user} && runuser -u #{user} -- bash -c #{Shellwords.escape(inner)}"
    name = "nano started by unprivileged user #{user} has euid 0 (setuid root)"
    begin
      out = run_vagrant_ssh(cmd, timeout: 90)
      stdout = out[:stdout].to_s
      if stdout =~ /ruid=(\d+) euid=0\b/ && $1 != '0'
        pass_check(name, "#{name}: #{stdout.strip}", tier: 2)
      else
        fail_check(name, "#{name}: got '#{stdout.strip}'", tier: 2,
                   evidence: "$ #{cmd}\n--- stdout\n#{stdout}\n--- stderr\n#{out[:stderr]}")
      end
    ensure
      begin
        run_vagrant_ssh("pkill -u #{user} 2>/dev/null; userdel -r #{user} 2>/dev/null; rm -f /tmp/secgen_nano_t2.txt", timeout: 60)
      rescue StandardError
        nil
      end
    end
  end

end

SUIDNanoTest.new.run
