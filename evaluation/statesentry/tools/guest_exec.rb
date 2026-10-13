# Run a command inside a Proxmox VM through the QEMU Guest Agent, for the
# StateSentry evaluation (copying scripts to the victim, running the
# correctness test, collecting detections). Throwaway tooling for this
# evaluation, kept here for reproducibility; SecGen master has the
# maintained equivalent (scripts/agent-check, lib/helpers/proxmox_connection.rb).
#
#   BUNDLE_PATH=/home/deploy/Code/SecGen/vendor/bundle bundle exec ruby \
#     evaluation/statesentry/tools/guest_exec.rb [--node N] [--timeout S] [--stdin FILE] VMID -- CMD [ARG...]
#   ... guest_exec.rb [--node N] --script FILE VMID [ARG...]     # run a local script with bash
#   ... guest_exec.rb [--node N] --upload LOCAL VMID GUEST_PATH  # copy a file into the guest
#
# Arguments containing '%' lose it on the way to the guest, so use --script for
# anything with a format string. The guest agent waits for the command to
# exit (--timeout, default 30 s); start long jobs detached from a script.
#
# Credentials are handled as on SecGen master: the options file (SECGEN_CONF,
# default ~/.config/secgen/secgen.conf) is read in this process, as in
# scripts/lib/agent_check.rb, and the password comes from SECGEN_PROXMOX_PASS
# if set (as in scripts/secgen-test-run and PostProvisionTest), otherwise from
# the file. It goes straight to the API login; nothing from the file is
# printed or placed on a command line.
#
# Prints the guest's stdout and stderr, and exits with the guest's exit code.
require 'optparse'
# This branch's client uses JSON and RestClient without requiring them (master requires both).
require 'json'
require 'rest-client'
require_relative '../../../lib/helpers/proxmox_connection'

# exec_qemu_guest and post_json as on SecGen master (lib/helpers/proxmox_connection.rb);
# this branch's client predates them.
module Proxmox
  class Connection
    def exec_qemu_guest(vm_id, node, command, timeout: 10, input_data: nil)
      body = { command: Array(command) }
      body[:'input-data'] = input_data if input_data
      pid = post_json("/nodes/#{node}/qemu/#{vm_id}/agent/exec", body).dig(:data, :pid)

      deadline = Process.clock_gettime(Process::CLOCK_MONOTONIC) + timeout
      delay = 0.25
      loop do
        status = get("/nodes/#{node}/qemu/#{vm_id}/agent/exec-status?pid=#{pid}")[:data]
        # PVE versions differ on whether flags are 1 or true
        if truthy?(status[:exited])
          return { exitcode: status[:exitcode],
                   stdout: status[:'out-data'].to_s,
                   stderr: status[:'err-data'].to_s,
                   truncated: truthy?(status[:'out-truncated']) || truthy?(status[:'err-truncated']) }
        end
        if Process.clock_gettime(Process::CLOCK_MONOTONIC) >= deadline
          raise ProxmoxTaskTimeout, "guest command #{Array(command).inspect} on VM #{vm_id} did not exit within #{timeout}s"
        end
        sleep delay
        delay = [delay * 2, 1].min
      end
    end

    private

    # JSON-encoded POST: form encoding mangles array params such as agent/exec's command
    def post_json(path, body)
      response = RestClient::Resource.new("#{api_url}#{path}", verify_ssl: false,
                                          headers: headers.merge(content_type: :json)).post body.to_json
      JSON.parse response.to_s, symbolize_names: true
    rescue RestClient::Unauthorized
      raise ApiError::UnauthorizedError
    rescue RestClient::NotImplemented
      raise ApiError::NotImplemented
    rescue RestClient::InternalServerError => x
      raise ApiError::ServerError, "#{x.message} for POST #{api_url}#{path}"
    rescue => x
      raise ApiError::ConnectionError, x.message
    end

    def truthy?(value)
      value == true || value == 1 || value == '1'
    end
  end
end

usage = 'Usage: guest_exec.rb [--node N] [--timeout S] [--stdin FILE] VMID -- CMD [ARG...]'
opts = { timeout: 30 }
OptionParser.new do |o|
  o.banner = usage
  o.on('--node NODE') { |v| opts[:node] = v }
  o.on('--timeout SECONDS', Integer) { |v| opts[:timeout] = v }
  o.on('--stdin FILE', 'send FILE to the command as its standard input') { |v| opts[:stdin] = v }
  # The guest agent path drops '%' from command arguments (seen on PVE 9.2 with
  # this guest), so anything containing '%' goes in a script file instead.
  o.on('--script FILE', 'run FILE in the guest with bash; remaining args are its $1...') { |v| opts[:script] = v }
  o.on('--upload LOCAL', 'copy LOCAL to the guest path given as the only argument') { |v| opts[:upload] = v }
end.parse!(ARGV)
vm_id = ARGV.shift or abort usage
if opts[:script]
  ARGV.replace(['/bin/bash', '-s', '--', *ARGV])
  opts[:stdin] = opts[:script]
elsif opts[:upload]
  abort 'Usage: guest_exec.rb --upload LOCAL VMID GUEST_PATH' unless ARGV.size == 1
  ARGV.replace(['/bin/dd', "of=#{ARGV[0]}", 'status=none'])
  opts[:stdin] = opts[:upload]
end
abort usage if ARGV.empty?

conf_path = ENV.fetch('SECGEN_CONF', File.expand_path('~/.config/secgen/secgen.conf'))
conf = File.read(conf_path).split.each_slice(2).to_h
node = opts[:node] || conf['--proxmox-node'] or abort 'No --node given and no --proxmox-node in config'

# As on master, SECGEN_PROXMOX_PASS (set by scripts/lib/common.sh's conf_get
# for a single process) takes precedence over the password in the file.
password = ENV['SECGEN_PROXMOX_PASS'].to_s.empty? ? conf['--proxmoxpass'] : ENV['SECGEN_PROXMOX_PASS']
conn = Proxmox::Connection.new(conf['--proxmox-url'])
conn.login(username: conf['--proxmoxuser'], password: password)
conf = password = nil

input = opts[:stdin] && File.read(opts[:stdin])
r = conn.exec_qemu_guest(vm_id, node, ARGV, timeout: opts[:timeout], input_data: input)
$stdout.write r[:stdout]
$stderr.write r[:stderr]
warn '[guest_exec] output truncated by the guest agent' if r[:truncated]
exit r[:exitcode].to_i
