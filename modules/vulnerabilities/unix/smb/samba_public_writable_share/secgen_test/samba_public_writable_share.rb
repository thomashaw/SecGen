require_relative '../../../../../lib/post_provision_test'

class SambaPublicWritableShareTest < PostProvisionTest
  def initialize
    self.module_name = 'samba_public_writable_share'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    storage_directory = self.json_inputs['storage_directory']&.first
    share_comment = self.json_inputs['share_comment']&.first
    guests_allowed = self.json_inputs['guests_allowed']&.first != 'false'
    read_only = self.json_inputs['read_only_share']&.first == 'true'
    organisation = self.json_inputs['organisation']&.first.to_s
    writable_expected = guests_allowed && !read_only

    # With an organisation the module renames the share (<domain>_public_share)
    # and its comment, so read the real name from the last section of smb.conf.
    share_name = self.json_inputs['share_name']&.first
    share_comment = nil unless organisation.empty?
    if !organisation.empty?
      share_name = run_command("grep -E '^\\[' /etc/samba/smb.conf | tail -1 | tr -d '[]'")[:stdout].to_s.strip
    end

    tier(1) do
      test_local_command('samba installed', 'dpkg -l samba | grep -E "^ii"', 'samba')
      if share_name && !share_name.empty?
        test_local_command("share [#{share_name}] in smb.conf", "grep -i '\\[#{share_name}\\]' /etc/samba/smb.conf", share_name)
      else
        test_local_command('samba share configured in smb.conf', "grep -E '^\\[' /etc/samba/smb.conf", '[')
      end
      if storage_directory
        test_local_command('storage directory exists', "test -d #{Shellwords.escape(storage_directory)} && echo present", 'present')
      end
    end

    tier(2) do
      test_service_up(port: 445)
      # Effective configuration as samba parses it (testparm ships with samba).
      test_local_command('share is guest accessible', "testparm -s 2>/dev/null | sed -n '/^\\[#{share_name}\\]/,/^\\[/p'",
                         "guest ok = #{guests_allowed ? 'Yes' : 'No'}") if share_name && !share_name.empty?
      test_local_command("share is #{writable_expected ? 'writable' : 'read only'}", "testparm -s 2>/dev/null | sed -n '/^\\[#{share_name}\\]/,/^\\[/p'",
                         "read only = #{read_only ? 'Yes' : 'No'}") if share_name && !share_name.empty?

      # The target has no smbclient (and the module must not install one), so
      # act as a student would: list and write from the attacker VM.
      attacker = proxmox? ? attack_system : nil
      if attacker && system_present?(attacker) && share_name && !share_name.empty?
        rhost = system_ip
        listing = run_on_system(attacker, "smbclient -L //#{Shellwords.escape(rhost)} -N 2>&1", timeout: 60)[:stdout].to_s
        add_evidence('smbclient_listing', "echo #{Shellwords.escape(listing[0..2000])}")
        if listing.include?('Sharename') && listing.include?(share_name)
          pass_check("share #{share_name} listed anonymously", "smbclient -L from #{attacker} lists #{share_name}", tier: 2)
        else
          fail_check("share #{share_name} listed anonymously", "smbclient -L from #{attacker}: #{listing[0..500]}", tier: 2)
        end
        if share_comment && listing.include?('Sharename') && !listing.include?(share_comment)
          fail_check('share comment listed', "comment #{share_comment.inspect} not in listing: #{listing[0..500]}", tier: 2)
        end

        nonce = "secgen_t2_#{Time.now.to_i}_#{rand(100000)}"
        put = run_on_system(attacker,
                            "echo #{nonce} > /tmp/#{nonce} && smbclient //#{Shellwords.escape(rhost)}/#{Shellwords.escape(share_name)} " \
                            "-N -c 'put /tmp/#{nonce} #{nonce}' 2>&1; rm -f /tmp/#{nonce}", timeout: 60)
        put_out = put[:stdout].to_s
        add_evidence('smbclient_put', "echo #{Shellwords.escape(put_out[0..2000])}")
        if storage_directory
          present = run_command("test -f #{Shellwords.escape(storage_directory + '/' + nonce)} && echo present")[:stdout].to_s.include?('present')
          run_command("rm -f #{Shellwords.escape(storage_directory + '/' + nonce)}")
          if writable_expected == present
            pass_check('anonymous write to share',
                       writable_expected ? "file written by anonymous smbclient appeared in #{storage_directory}" :
                                           'anonymous write was refused, as configured', tier: 2)
          else
            fail_check('anonymous write to share',
                       "expected #{writable_expected ? 'write to succeed' : 'write to be refused'}; smbclient said: #{put_out[0..500]}", tier: 2)
          end
        end
      else
        skip_check('anonymous list/write from attacker',
                   'No attacker (base type=attack) system in this project, or no share name; only testparm checks ran', tier: 2)
      end
    end
    # No tier 3 for this issue: the vulnerability is the anonymous write itself,
    # which tier 2 exercises from the attacker VM.
    #
    # Inputs not checked directly:
    #   strings_to_leak, leaked_filenames, images_to_leak — content planted in the share,
    #     not part of share access (mode 0666 is set by leak_files)
  end
end

SambaPublicWritableShareTest.new.run
