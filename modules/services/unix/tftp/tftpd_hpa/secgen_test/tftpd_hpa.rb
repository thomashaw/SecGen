require 'shellwords'
require_relative '../../../../../lib/post_provision_test'

class TftpdHpaTest < PostProvisionTest
  def initialize
    self.module_name = 'tftpd_hpa'
    self.module_path = get_module_path(__FILE__)
    super
  end

  # Inputs: address (checked in the defaults file, on the wire and from another
  # host), directory (defaults file, ownership, where the fetched file lives) and
  # allow_create (--create present or absent, and an upload accepted or refused).
  # The module has no port input, so the port comes from address.
  # Fetches use curl's tftp:// support, so the target needs no tftp client package.
  def test_module
    super
    address      = self.json_inputs['address']&.first
    directory    = self.json_inputs['directory']&.first
    allow_create = self.json_inputs['allow_create']&.first.to_s.downcase == 'true'

    host, port = address.to_s.split(':', 2)
    port = (port || '69').to_i
    exposed = host.to_s.empty? || host == '0.0.0.0'
    local_host = exposed ? '127.0.0.1' : host
    nonce = "secgen_tftp_#{Time.now.to_i}_#{rand(100000)}"
    body = "tftp-#{nonce}"
    test_file = File.join(directory.to_s, nonce)

    tier(1) do
      test_local_command('tftpd-hpa installed', 'dpkg -l tftpd-hpa | grep -E "^ii"', 'tftpd-hpa')
      test_local_command('tftpd-hpa enabled at boot', 'systemctl is-enabled tftpd-hpa', 'enabled')
      test_local_command('tftpd-hpa active', 'systemctl is-active tftpd-hpa', 'active')
      if directory
        test_local_command('tftp directory exists', "test -d #{Shellwords.escape(directory)} && echo isdir", 'isdir')
        test_local_command('tftp directory owned by tftp', "stat -c %U #{Shellwords.escape(directory)}", 'tftp')
        test_local_command('defaults file directory', 'grep -E "^TFTP_DIRECTORY=" /etc/default/tftpd-hpa', "\"#{directory}\"")
      end
      test_local_command('defaults file address', 'grep -E "^TFTP_ADDRESS=" /etc/default/tftpd-hpa', "\"#{address}\"") if address
      test_local_command('--secure always set', 'grep -E "^TFTP_OPTIONS=" /etc/default/tftpd-hpa', '--secure')
      create_cmd = 'grep -E "^TFTP_OPTIONS=" /etc/default/tftpd-hpa | grep -c -- "--create"; true'
      test_local_command("--create #{allow_create ? 'present' : 'absent'}", create_cmd, allow_create ? '1' : '0')
    end

    tier(2) do
      # udp listener on the configured address and port
      listen_pat = exposed ? "(0\\.0\\.0\\.0|\\*):#{port}\\s" : "#{Regexp.escape(host)}:#{port}\\s"
      test_local_command('tftpd listening on the configured address', "ss -lun | grep -E '#{listen_pat}'", ":#{port}")

      if directory
        run_command("printf %s #{Shellwords.escape(body)} > #{Shellwords.escape(test_file)} && chown tftp:tftp #{Shellwords.escape(test_file)} && chmod 644 #{Shellwords.escape(test_file)}")
        test_local_command('file in the directory is served over TFTP',
                           "curl -sS --max-time 15 tftp://#{local_host}:#{port}/#{nonce}", body, tier: 2)

        # --secure: the server is confined to the directory, so /etc/hostname is not served (curl exits non-zero)
        out = run_command("curl -sS --max-time 15 tftp://#{local_host}:#{port}/etc/hostname 2>&1; echo \"rc=$?\"")[:stdout].to_s
        if out =~ /rc=0\s*\z/
          fail_check('--secure refuses a path outside the directory', "served /etc/hostname: #{out[0, 200]}", tier: 2)
        else
          pass_check('--secure refuses a path outside the directory', "/etc/hostname not served: #{out[0, 200]}", tier: 2)
        end

        # allow_create: an upload of a new file is accepted only with --create
        up = "#{nonce}_up"
        run_command("printf up > /tmp/#{up}; curl -sS --max-time 15 -T /tmp/#{up} tftp://#{local_host}:#{port}/#{up} >/dev/null 2>&1; rm -f /tmp/#{up}")
        landed = run_command("test -f #{Shellwords.escape(File.join(directory, up))} && echo present || echo absent")[:stdout].to_s
        run_command("rm -f #{Shellwords.escape(File.join(directory, up))}")
        if allow_create == landed.include?('present')
          pass_check("upload of a new file #{allow_create ? 'accepted' : 'refused'}", "landed: #{landed.strip}", tier: 2)
        else
          fail_check("upload of a new file #{allow_create ? 'accepted' : 'refused'}", "allow_create=#{allow_create} but landed: #{landed.strip}", tier: 2)
        end
      end

      # Remote reachability from the attacker VM, when the scenario has one:
      # a loopback-only address must refuse, an exposed one must serve.
      label = exposed ? 'attacker fetches the file over TFTP' : 'TFTP is not reachable from another host'
      if directory && (resolved = resolve_attacker(label, nil, nil, 2))
        attacker, _attacker_ip, rhost = resolved
        res = run_on_system(attacker, "curl -sS --max-time 15 tftp://#{rhost}:#{port}/#{nonce} 2>&1; echo \"rc=$?\"")
        out = res[:stdout].to_s
        fetched = out.include?(body)
        if exposed == fetched
          pass_check(label, "from #{attacker}: #{out[0, 200].strip}", tier: 2)
        else
          fail_check(label, "from #{attacker} (address #{address}): #{out[0, 300].strip}", tier: 2)
        end
      end

      run_command("rm -f #{Shellwords.escape(test_file)}") if directory
    end
  end
end

TftpdHpaTest.new.run
