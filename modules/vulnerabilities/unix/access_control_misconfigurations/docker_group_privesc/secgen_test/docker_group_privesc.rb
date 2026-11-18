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
        # The vulnerability *is* docker-group membership: the leaked account is in
        # the docker group (tiers 1-2), and the daemon socket is root:docker 0660,
        # so only a docker-group member can reach it. with_throwaway_ssh_user
        # creates a bare foothold with no supplementary groups, so the exploit's
        # `docker ps` AutoCheck would fail and nothing would write the sentinel.
        # Grant the disposable foothold the same docker-group access the real
        # account has before the session opens (login reads /etc/group, so a
        # fresh ssh_login picks it up); userdel -r removes it again on teardown.
        run_vagrant_ssh("usermod -aG docker #{Shellwords.escape(user)}")
        # This module includes Msf::Exploit::EXE and drops a native ELF
        # (generate_payload_exe), which the host docker container chowns to
        # root:root and makes setuid before the foothold runs it. The helper's
        # default cmd/unix/generic payload is ARCH_CMD and incompatible with the
        # module's native arch, so MSF rejects it and nothing is planted — hence
        # the absent sentinel. Pin a native linux payload (linux/x64/exec runs
        # the sentinel CMD the helper sets). The setuid-root ELF gives euid=0 but
        # keeps the foothold's real uid, so `id` would still read uid!=0 and fail
        # expect_privilege: :root (→ uid0); PrependSetuid prepends setuid(0),
        # which succeeds because euid is 0, promoting the shell to a true root
        # uid. sentinel_wait stays generous for the module's WfsDelay (60s) plus
        # docker build/run latency before the setuid payload fires.
        test_msf_local_exploit('exploit/linux/local/docker_daemon_privilege_escalation',
                               session_user: user, session_pass: pass,
                               payload: 'linux/x64/exec',
                               options: { 'PrependSetuid' => true },
                               expect_privilege: :root,
                               sentinel_wait: 180,
                               tier: 3)
      end
    end
  end
end

DockerGroupPrivescTest.new.run
