require_relative '../../../../../lib/post_provision_test'
require 'json'
require 'shellwords'

class DockerGroupPrivescTest < PostProvisionTest
  def initialize
    self.module_name = 'docker_group_privesc'
    self.module_path = get_module_path(__FILE__)
    super
  end

  # The account added to the docker group, read from the `accounts` input (a
  # JSON object, as crackable_user_account produces it), falling back to a bare
  # `username` input.
  def docker_group_username
    %w[accounts account].each do |key|
      raw = self.json_inputs[key]&.first
      next if raw.to_s.empty?
      begin
        return JSON.parse(raw)['username']
      rescue StandardError
        next
      end
    end
    self.json_inputs['username']&.first
  end

  def test_module
    super

    # read_facts: accounts (the docker-group user, below), strings_to_leak +
    # leaked_filenames (the flag file in /root, below). No other inputs.
    username = docker_group_username
    return skip_check('docker group privesc', 'no account/username input to identify the created user') unless username

    strings   = self.json_inputs['strings_to_leak'] || []
    filenames = self.json_inputs['leaked_filenames'] || []

    tier(1) do
      # The account exists...
      test_local_command("user #{username} exists", "id -un #{Shellwords.escape(username)}", username)
      # ...and belongs to the docker group, which is the whole point of the module.
      test_local_command("#{username} is in the docker group", "id -nG #{Shellwords.escape(username)}", 'docker')

      # The flag file(s) are in /root, owned by root and mode 0600, so only root
      # can read them. secgen_functions::leak_files pairs each filename with the
      # leaked string of the same index.
      filenames.each_with_index do |filename, i|
        path = "/root/#{filename}"
        esc  = Shellwords.escape(path)
        test_local_command("#{path} is root:root 0600", "stat -c '%a %U %G' #{esc}", '600 root root')
        if (flag = strings[i])
          test_local_command("flag leaked to #{path}", "cat #{esc}", flag)
        end
      end
    end

    tier(2) do
      # As that unprivileged user, the Docker daemon is reachable without sudo:
      # docker-group membership grants full control of the daemon. `runuser`
      # initialises the user's supplementary groups, so membership is effective
      # in this check. `docker ps` always prints the "CONTAINER ID" header.
      test_local_command("#{username} can use docker without sudo", 'docker ps', 'CONTAINER ID', user: username)
    end

    # Tier 3: exploit/linux/local/docker_daemon_privilege_escalation is a LOCAL privilege escalation — Metasploit drives it against
    # an existing low-privilege SESSION, not a remote RHOSTS call. with_throwaway_ssh_user
    # creates a disposable unprivileged account on the target (removed afterwards, so the
    # image ships no extra login) for the attacker's ssh_login to land on; the local exploit
    # runs against that session and the sentinel then proves a root shell (uid=0), which
    # expect_privilege: :root asserts. SKIPs cleanly with no attacker VM. TODO t3-worker: add
    # any module-specific options the exploit needs, and raise sentinel_wait for a deferred
    # trigger (a cron tick) if it fires on a schedule — see exploit/unix/local/chkrootkit.
    tier(3) do
      with_throwaway_ssh_user do |user, pass|
        # The module plants a SUID ELF via generate_payload_exe (not a cmd payload),
        # so cmd/unix/generic is rejected. A native linux/x64/exec payload is
        # compatible and still honours the sentinel's CMD, which the root-owned SUID
        # binary runs as uid 0. sentinel_wait is raised to cover the docker image
        # build + container run plus the module's 60s WfsDelay.
        test_msf_local_exploit('exploit/linux/local/docker_daemon_privilege_escalation',
                               session_user: user, session_pass: pass,
                               payload: 'linux/x64/exec',
                               options: {},
                               expect_privilege: :root,
                               sentinel_wait: 240,
                               tier: 3)
      end
    end
  end
end

DockerGroupPrivescTest.new.run
