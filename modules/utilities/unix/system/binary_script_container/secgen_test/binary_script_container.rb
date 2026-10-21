require_relative '../../../../../lib/post_provision_test'

# No SecGen inputs: the wrapper is built and installed at fixed paths.
class BinaryScriptContainerTest < PostProvisionTest
  SUID = '/usr/local/bin/suid'
  PROBE = '/tmp/.secgen_suid_probe.sh'

  def initialize
    self.module_name = 'binary_script_container'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_command_succeeds('suid binary installed', "test -x #{SUID}")
      test_local_command('suid binary is owned by root', "stat -c %U #{SUID}", 'root')
      test_local_command('wrapper source copied to the box', 'test -f /root/suid/suid.c && echo ok', 'ok')
    end
    tier(2) do
      # The binary is installed setuid root; verify the SUID bit is set
      test_local_command('SUID bit set on binary', "ls -la #{SUID}", 'rws')
      # an unprivileged user can run it; with no script it prints its usage
      test_local_command('suid wrapper runs as an unprivileged user', SUID, 'Usage:', user: 'nobody')
      # a root-owned setuid script run through the wrapper executes with root as its effective user
      run_command("printf '#!#{SUID} /bin/bash -o privileged --\\nset -eu\\necho uid=$(id -run) euid=$(id -un)\\n' > #{PROBE}; " \
                  "chown root:root #{PROBE}; chmod 4755 #{PROBE}")
      test_local_command('setuid script via wrapper runs with root euid', PROBE, 'euid=root', user: 'nobody')
      run_command("rm -f #{PROBE}")
    end
  end
end

BinaryScriptContainerTest.new.run
