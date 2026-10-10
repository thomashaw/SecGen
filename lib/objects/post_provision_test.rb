# Post Provision Testing
#
# This file will be copied into each project folder at creation time.
# It will be required from each of the modules/secgen_tests/module_name.rb test scripts
#
# Test classes must: require_relative '../../../../../lib/post_provision_test'
#
# Results (see agentic_pipeline/ROADMAP.md, "Results contract"):
# - every check is PASS / FAIL / SKIP with a tier (1 provisioned, 2 service/tool
#   works, 3 exploitable);
# - run writes projects/<id>/test_results/<system>/<module>.json, collects
#   evidence into evidence/<module>/ on FAIL, prints the PASSED:/FAILED:/SKIPPED:
#   lines and exits 0 (PASS), 1 (FAIL) or 2 (SKIP: could not test).
# Legacy tests that push "PASSED: ..." / "FAILED: ..." onto outputs and set
# all_tests_passed keep working: those lines are recorded as checks.

require 'json'
require 'base64'
require 'socket'
require 'timeout'
require 'net/http'
require 'open3'
require 'shellwords'
require 'fileutils'
require 'time'
require 'rexml/document'

# Proxmox transport (QEMU Guest Agent). Optional: only used when the project was
# built against Proxmox (a proxmox_test_context.json sits in the project root and
# .vagrant/machines/<system>/proxmox/id exists). Copied into the project lib
# alongside this file at build time.
begin
  require_relative './proxmox_connection'
rescue LoadError
  # Vagrant-only projects don't ship the proxmox client; that's fine.
end

class PostProvisionTest
  STATUSES = %w[PASS FAIL SKIP].freeze
  EXIT_CODES = { 'PASS' => 0, 'FAIL' => 1, 'SKIP' => 2 }.freeze
  LINE_PREFIXES = { 'PASS' => 'PASSED', 'FAIL' => 'FAILED', 'SKIP' => 'SKIPPED' }.freeze
  TIERS = { 1 => 'provisioned', 2 => 'service/tool works', 3 => 'exploitable' }.freeze

  # Raised to stop the test when it cannot go on (no IP, no guest agent, no
  # credentials). Recorded as a SKIP check: a harness problem, never a pass.
  class SkipTest < StandardError; end

  # outputs is still an array of lines, but legacy "PASSED: ..." / "FAILED: ..."
  # lines pushed with << are also recorded as checks.
  class Outputs < Array
    def initialize(test)
      super()
      @test = test
    end

    def <<(line)
      super
      @test.send(:record_legacy_line, line)
      self
    end
  end

  attr_accessor :project_path
  attr_writer :system_ip
  attr_accessor :module_name
  attr_accessor :module_path
  attr_accessor :json_inputs
  attr_accessor :port
  attr_accessor :outputs
  attr_accessor :all_tests_passed
  attr_reader :results

  def initialize
    self.outputs = Outputs.new(self)
    @results = []
    @current_tier = nil # set by #tier; nil = each helper's own default
    @extra_evidence = {}
    self.json_inputs = get_json_inputs
    self.port = get_port
    self.all_tests_passed = true
  end

  def run
    @started_at = Time.now
    begin
      wait_for_guest_agent if proxmox?
      test_module
    rescue SkipTest => e
      record('test could not continue', 'SKIP', e.message)
    rescue StandardError, ScriptError => e
      record("test raised #{e.class}", 'SKIP', "#{e.message} (#{(e.backtrace || []).first(3).join(' | ')})")
    end

    # A legacy test may clear all_tests_passed without a FAILED: line.
    if !all_tests_passed && @results.none? { |r| r[:status] == 'FAIL' }
      record('test reported failure', 'FAIL', 'all_tests_passed was set to false')
    end
    record('no checks were run', 'SKIP') if @results.empty?

    status = overall_status
    collect_evidence if status == 'FAIL'
    write_results(status)

    puts self.outputs
    exit(EXIT_CODES[status])
  end

  def test_module
    # Call super first in overriden methods
    self.outputs.push "Running tests for #{self.module_name}"
  end

  #####################
  # Results           #
  #####################

  # Record one check. status is PASS / FAIL / SKIP; tier defaults to the current
  # tier (see #tier). evidence (text) is saved under evidence/<module>/.
  def record(name, status, detail = nil, tier: nil, evidence: nil)
    status = status.to_s.upcase
    raise ArgumentError, "unknown status #{status}" unless STATUSES.include?(status)
    tier ||= @current_tier || 1
    check = { tier: tier, name: name.to_s, status: status, detail: detail }
    check[:evidence] = [save_evidence("check_#{@results.size + 1}", evidence)].compact if evidence
    @results << check
    self.all_tests_passed = false if status == 'FAIL'
    self.outputs.push "#{LINE_PREFIXES[status]}: #{detail.nil? || detail.to_s.empty? ? name : detail}"
    check
  end

  def pass_check(name, detail = nil, **opts)
    record(name, 'PASS', detail, **opts)
  end

  def fail_check(name, detail = nil, **opts)
    record(name, 'FAIL', detail, **opts)
  end

  def skip_check(name, detail = nil, **opts)
    record(name, 'SKIP', detail, **opts)
  end

  # Stop the test now and report SKIP (e.g. a prerequisite isn't available).
  def skip!(reason)
    raise SkipTest, reason
  end

  # Set the tier for the checks that follow, or (with a block) for the checks
  # inside it: 1 provisioned, 2 service/tool works, 3 exploitable.
  #   tier(3) { test_banner('vsFTPd 2.3.4') }
  def tier(level)
    raise ArgumentError, "tier must be one of #{TIERS.keys}" unless TIERS.key?(level)
    return @current_tier = level unless block_given?
    previous = @current_tier
    @current_tier = level
    begin
      yield
    ensure
      @current_tier = previous
    end
  end

  # tier: given explicitly, else the enclosing #tier, else the helper's default.
  def default_tier(tier, fallback)
    tier || @current_tier || fallback
  end

  # PASS if every check passed, FAIL if any failed, otherwise SKIP.
  def overall_status
    return 'FAIL' if @results.any? { |r| r[:status] == 'FAIL' }
    return 'SKIP' if @results.empty? || @results.any? { |r| r[:status] == 'SKIP' }
    'PASS'
  end

  # Highest tier whose checks (and every lower tier's) all passed; tiers with no
  # checks don't block a higher one. 0 if none.
  def tier_reached
    reached = 0
    TIERS.keys.each do |level|
      checks = @results.select { |r| r[:tier] == level }
      next if checks.empty?
      break unless checks.all? { |r| r[:status] == 'PASS' }
      reached = level
    end
    reached
  end

  # Add a guest command whose output is saved as evidence if the test FAILs.
  def add_evidence(name, command)
    @extra_evidence[name.to_s] = command
  end

  #####################
  # Testing Functions #
  #####################

  # Test service is up (tcp)
  def test_service_up(port: self.port, tier: nil)
    tier = default_tier(tier, 2)
    if port.nil? || port.to_i <= 0
      return skip_check("service up", "No port to test for #{self.module_name} (no 'port' input)", tier: tier)
    end
    port_open = proxmox? ? guest_port_listening?(port) : is_port_open?(system_ip, port)
    if port_open
      pass_check("port #{port} open", "Port #{port} is open at #{ip_label} (#{get_system_name})!", tier: tier)
    else
      fail_check("port #{port} open", "Port #{port} is closed at #{ip_label} (#{get_system_name})!", tier: tier)
    end
  end

  # example usage for page: /index.html
  # On Proxmox the page is fetched from inside the guest (the host can't reach
  # the scenario VLAN).
  def test_html_returned_content(page, match_string, hide_content = false, port: self.port, tier: nil)
    tier = default_tier(tier, 2)
    response = http_get(page, port: port)
    shown = hide_content ? '<redacted>' : match_string
    name = "#{page} contains #{shown}"
    if response[:error]
      fail_check(name, "Content #{shown} is not contained within #{page} at #{ip_label}:#{port} (#{get_system_name})! (#{response[:error]})", tier: tier)
    elsif response[:body].include?(match_string)
      pass_check(name, "Content #{shown} is contained within #{page} at #{ip_label}:#{port} (#{get_system_name})!", tier: tier)
    else
      fail_check(name, "Content #{shown} is not contained within #{page} at #{ip_label}:#{port} (#{get_system_name})! (HTTP #{response[:status]})", tier: tier)
    end
  end

  # HTTP check: passes if the response status matches (default 2xx/3xx) and,
  # if given, the body contains match.
  def test_http(path, match: nil, status: nil, port: self.port, scheme: 'http', label: nil, tier: nil)
    tier = default_tier(tier, 2)
    response = http_get(path, port: port, scheme: scheme)
    name = label || "HTTP #{scheme}://:#{port}#{path}"
    if response[:error]
      return fail_check(name, "#{name}: #{response[:error]}", tier: tier)
    end
    status_ok = status ? response[:status].to_i == status.to_i : (200..399).cover?(response[:status].to_i)
    body_ok = match.nil? || response[:body].include?(match)
    detail = "#{name}: HTTP #{response[:status]}#{match ? ", body #{body_ok ? 'contains' : 'does not contain'} #{match.inspect}" : ''}"
    status_ok && body_ok ? pass_check(name, detail, tier: tier) : fail_check(name, detail, tier: tier)
  end

  # Banner check: connects to the port (from inside the guest on Proxmox),
  # optionally sends a line, and passes if what comes back contains match.
  def test_banner(match, port: self.port, send: nil, label: nil, tier: nil)
    tier = default_tier(tier, 2)
    name = label || "banner on port #{port} contains #{match.inspect}"
    banner = read_banner(port, send: send)
    if banner.include?(match)
      pass_check(name, "Port #{port} banner contains #{match.inspect} (#{get_system_name})", tier: tier)
    else
      fail_check(name, "Port #{port} banner does not contain #{match.inspect} (#{get_system_name}); got #{banner[0, 200].inspect}", tier: tier)
    end
  end

  # Passes if match_string is in the command's stdout or stderr. user: runs it as
  # that account (see #run_as_user) instead of root.
  def test_local_command(test_output, local_command, match_string, user: nil, tier: nil)
    tier = default_tier(tier, 1)
    output = user ? run_as_user(user, local_command) : run_vagrant_ssh(local_command)
    as = user ? " as #{user}" : ''
    name = "#{test_output} local command (#{local_command})#{as} matches with output (#{match_string})"
    if output[:stdout].include?(match_string) || output[:stderr].include?(match_string)
      pass_check(name, "#{name} on #{get_system_name}!", tier: tier)
    else
      fail_check(name, "#{name} on #{get_system_name}!", tier: tier,
           evidence: "$ #{local_command}#{as}\n--- stdout\n#{output[:stdout]}\n--- stderr\n#{output[:stderr]}")
      self.outputs.push output[:stderr]
    end
  end

  # Passes if the command exits 0 (e.g. a tool runs from a login shell).
  def test_command_succeeds(label, command, user: nil, tier: nil)
    tier = default_tier(tier, 2)
    output = user ? run_as_user(user, command) : run_vagrant_ssh(command)
    as = user ? " as #{user}" : ''
    name = "#{label} (#{command})#{as}"
    if output[:exit_status].to_i == 0
      pass_check(name, "#{name} exited 0 on #{get_system_name}", tier: tier)
    else
      fail_check(name, "#{name} exited #{output[:exit_status]} on #{get_system_name}", tier: tier,
           evidence: "$ #{command}#{as}\n--- stdout\n#{output[:stdout]}\n--- stderr\n#{output[:stderr]}")
    end
  end

  ##################
  # Misc Functions #
  ##################

  # Run a shell command on the system (as root on Proxmox, as the vagrant user
  # over vagrant ssh). Returns {stdout:, stderr:, exit_status:}.
  def run_vagrant_ssh(args, timeout: 30)
    # On Proxmox, run the command in the guest via the QEMU Guest Agent instead
    # of vagrant ssh (no guest network path is needed, and it survives net0
    # teardown). This is just run_on_system targeting ourselves.
    return run_on_system(get_system_name, args, timeout: timeout) if proxmox?
    # argv form: no local shell, so quotes in args reach the guest intact.
    vagrant = File.executable?('/usr/bin/vagrant') ? '/usr/bin/vagrant' : 'vagrant'
    stdout, stderr, status = Open3.capture3(vagrant, 'ssh', get_system_name, '-c', args, chdir: get_project_path)
    {:stdout => stdout, :stderr => stderr, :exit_status => status.exitstatus}
  end
  alias run_command run_vagrant_ssh

  # --- Cross-system helpers (tier 3 / Phase 1C) ---------------------------
  # Network-side and exploit tests run from *another* system in the project
  # (e.g. a Kali attacker VM) and target the system under test by its IP.
  # On Proxmox each sibling system is reached over its own QEMU Guest Agent,
  # resolved from .vagrant/machines/<name>/proxmox/id, so no guest network
  # path to the host is needed. Only supported on Proxmox.

  # Does a sibling system by this name exist in the project?
  def system_present?(name)
    return File.exist?(other_proxmox_id_path(name)) if proxmox?
    File.directory?("#{get_project_path}/.vagrant/machines/#{name}")
  end

  def other_proxmox_id_path(name)
    "#{get_project_path}/.vagrant/machines/#{name}/proxmox/id"
  end

  # [node, vmid] for a sibling system from its Vagrant-written id file.
  def other_proxmox_node_vmid(name)
    File.read(other_proxmox_id_path(name)).strip.split('/')
  end

  # Run a shell command on another system in the project. Returns
  # {stdout:, stderr:, exit_status:}. Proxmox only (guest agent on that VM).
  def run_on_system(name, args, timeout: 60)
    unless proxmox?
      return {:stdout => '', :stderr => "run_on_system is Proxmox-only (#{name})", :exit_status => 127}
    end
    node, vmid = other_proxmox_node_vmid(name)
    result = proxmox_connection.exec_qemu_guest(vmid, node, ['bash', '-lc', args], timeout: timeout)
    {:stdout => result[:stdout], :stderr => result[:stderr], :exit_status => result[:exitcode]}
  end

  # The IP of a sibling system (first non-loopback IPv4 from its guest agent).
  def other_system_ip(name)
    return nil unless proxmox?
    node, vmid = other_proxmox_node_vmid(name)
    proxmox_connection.qemu_agent_get_ip(vmid, node)
  end

  # The SecGen checkout root (the project lives at <root>/projects/<id>).
  # The SecGen checkout root (the project lives at <root>/projects/<id>, see
  # PROJECTS_DIR in lib/helpers/constants.rb). We derive it rather than use the
  # ROOT_DIR constant on purpose: a secgen_test runs as a standalone `ruby`
  # subprocess that never requires constants.rb, so ROOT_DIR isn't defined
  # here. This matches how the rest of this class self-derives its paths
  # (get_project_path etc.).
  def secgen_root
    File.expand_path('../../', get_project_path)
  end

  # [[system_name, base_module_path], ...] for every system in the project,
  # read from the resolved projects/<id>/scenario.xml (no secrets in it).
  # Scanned with a regex rather than REXML/Nokogiri: the file has a default
  # xmlns (which REXML's XPath handles badly) and the test process keeps its
  # requires minimal; the generated markup is simple and stable.
  def project_systems
    @project_systems ||= begin
      xml = File.read("#{get_project_path}/scenario.xml")
      xml.scan(/<system>.*?<\/system>/m).map do |block|
        name = block[/<system_name>\s*(.*?)\s*<\/system_name>/m, 1]
        base = block[/<base[^>]*\bmodule_path="([^"]+)"/m, 1]
        [name, base]
      end.reject { |n, _| n.nil? }
    end
  rescue StandardError
    []
  end

  # Does the base module at this path declare <type>wanted</type>?
  def base_has_type?(base_module_path, wanted)
    return false if base_module_path.nil?
    meta = "#{secgen_root}/#{base_module_path}/secgen_metadata.xml"
    return false unless File.exist?(meta)
    File.read(meta).scan(/<type>\s*(.*?)\s*<\/type>/m).flatten.include?(wanted)
  rescue StandardError
    false
  end

  # Name of the sibling system acting as the attacker: the first *other* system
  # whose base is type 'attack' (e.g. a Kali base). nil if there isn't one, so
  # callers SKIP cleanly on a single-VM run. Override by passing an explicit
  # name to the exploit helpers.
  def attack_system
    return @attack_system if defined?(@attack_system)
    @attack_system = project_systems
                     .reject { |name, _| name == get_system_name }
                     .find { |_, base| base_has_type?(base, 'attack') }
                     &.first
  end

  # --- Metasploit exploit helper (tier 3) ---------------------------------
  # Drive a Metasploit exploit module from the attacker VM against this system
  # (the target under test) and record a tier-3 PASS/FAIL/SKIP.
  #
  # The default payload is the no-session cmd/unix/generic: the exploit runs a
  # command *on the target*, so there is no reverse/bind shell to race. Proof of
  # RCE is a sentinel file the command leaves on the target, which we read back
  # over the target's own guest agent and check for `uid=`. `collect:` appends
  # extra shell (e.g. 'cat /home/distccd/*') whose output is captured into the
  # sentinel as evidence. `options:` adds/overrides msf datastore settings
  # (RHOSTS/RPORT/PAYLOAD/CMD are set for you; pass LHOST etc. here if needed).
  #
  #   test_msf_exploit('exploit/unix/misc/distcc_exec',
  #                    rport: 3632, collect: 'cat /home/distccd/*')
  def test_msf_exploit(msf_module, rhost: nil, rport: self.port, attacker: nil,
                       payload: 'cmd/unix/generic', collect: nil,
                       options: {}, tier: 3, label: nil)
    name = label || "msf exploit (#{msf_module})"

    unless proxmox?
      skip_check(name, 'msf exploit tests run only on the Proxmox pipeline', tier: tier)
      return
    end
    attacker ||= attack_system
    unless attacker && system_present?(attacker)
      skip_check(name, "No attacker (base type='attack') system in this project; exploit skipped", tier: tier)
      return
    end
    rhost ||= system_ip                 # may SKIP if the target IP can't be resolved
    attacker_ip = other_system_ip(attacker)
    if rhost.nil? || attacker_ip.nil?
      skip_check(name, "Could not resolve target (#{rhost.inspect}) or attacker (#{attacker_ip.inspect}) IP", tier: tier)
      return
    end

    nonce = "#{Time.now.to_i}_#{rand(100000)}"
    sentinel = "/tmp/secgen_pwned_#{nonce}"
    cmd = "id > #{sentinel}; uname -n >> #{sentinel}; "
    cmd += "#{collect} >> #{sentinel} 2>/dev/null; " if collect
    cmd += "chmod 644 #{sentinel}"

    settings = { 'RHOSTS' => rhost, 'RPORT' => rport, 'PAYLOAD' => payload, 'CMD' => cmd }
    settings.merge!(options.map { |k, v| [k.to_s, v] }.to_h)
    set_lines = settings.map { |k, v| "set #{k} #{msf_set_value(v)};" }.join(' ')

    # HOME must be exported: the guest agent runs commands with no HOME, and
    # msfconsole's rb-readline aborts on startup without it.
    msf = "export HOME=/root TERM=dumb; msfconsole -q -x \"use #{msf_module}; " \
          "#{set_lines} run; sleep 8; exit -y\""
    result = run_on_system(attacker, msf, timeout: (ENV['SECGEN_MSF_TIMEOUT'] || 600).to_i)
    msf_out = "#{result[:stdout]}\n#{result[:stderr]}"
    add_evidence('msf_output', "echo #{Shellwords.escape(msf_out[-4000..-1] || msf_out)}")

    body = run_vagrant_ssh("cat #{sentinel} 2>/dev/null", timeout: 30)[:stdout].to_s
    add_evidence('sentinel_on_target', "echo #{Shellwords.escape(body)}") unless body.empty?

    if body =~ /uid=\d+/
      pass_check(name,
                 "RCE confirmed: #{msf_module} from #{attacker} (#{attacker_ip}) ran a command on the target (#{rhost}); sentinel #{sentinel} contains #{body[/uid=\S+/]}.",
                 tier: tier)
    else
      fail_check(name,
                 "Exploit did not confirm RCE: sentinel #{sentinel} absent or empty on the target. See msf_output evidence.",
                 tier: tier)
    end
  end

  # Render an msf datastore value for a `set` line inside the -x string: quote
  # in single quotes if it contains whitespace or shell metacharacters.
  def msf_set_value(value)
    s = value.to_s
    s =~ /[\s;'"|&<>$`]/ ? "'#{s.gsub("'", %q('\\''))}'" : s
  end

  # Run a shell command as the given account (login shell, that user's
  # environment) rather than root.
  def run_as_user(user, command, timeout: 30)
    run_vagrant_ssh(as_user_command(user, command, sudo: !proxmox?), timeout: timeout)
  end

  def as_user_command(user, command, sudo: false)
    u = Shellwords.escape(user)
    c = Shellwords.escape(command)
    s = sudo ? 'sudo ' : ''
    "if command -v runuser >/dev/null 2>&1; then #{s}runuser -u #{u} -- bash -lc #{c}; " \
      "else #{s}su -s /bin/bash -l #{u} -c #{c}; fi"
  end

  # GET a page. On Proxmox, from inside the guest (curl, wget or bash /dev/tcp,
  # whichever exists), against 127.0.0.1 then the guest's own IP; otherwise from
  # this host. Returns {status:, body:, error:}.
  def http_get(path, port: self.port, scheme: 'http')
    path = "/#{path}" unless path.start_with?('/')
    return host_http_get(path, port, scheme) unless proxmox?

    response = guest_http_get('127.0.0.1', path, port, scheme)
    if response[:status].to_i == 0
      ip = resolved_ip_or_nil
      response = guest_http_get(ip, path, port, scheme) if ip && ip != '127.0.0.1'
    end
    response
  end

  ############################
  # Proxmox (Guest Agent)    #
  ############################

  # Memoised {url, user} from the project, or nil if this isn't a Proxmox build.
  def proxmox_context
    return @proxmox_context if defined?(@proxmox_context)
    ctx_path = "#{get_project_path}/proxmox_test_context.json"
    @proxmox_context = File.exist?(ctx_path) ? JSON.parse(File.read(ctx_path)) : nil
  end

  def proxmox?
    !proxmox_context.nil? && File.exist?(proxmox_id_path) && !!defined?(Proxmox::Connection)
  end

  # This system's id-file path / [node, vmid] — the sibling helpers applied to
  # ourselves (see the cross-system helpers section).
  def proxmox_id_path
    other_proxmox_id_path(get_system_name)
  end

  def proxmox_node_vmid
    other_proxmox_node_vmid(get_system_name)
  end

  def proxmox_connection
    return @proxmox_connection if @proxmox_connection
    password = ENV['SECGEN_PROXMOX_PASS']
    if password.nil? || password.empty?
      skip!('SECGEN_PROXMOX_PASS is not set; cannot reach the Proxmox API for testing.')
    end
    @proxmox_connection = Proxmox::Connection.new(proxmox_context['url'])
    @proxmox_connection.login(username: proxmox_context['user'], password: password)
    @proxmox_connection
  end

  # Wait (up to SECGEN_AGENT_WAIT seconds, default 60) for the guest agent to
  # answer; SKIP if it never does.
  def wait_for_guest_agent(timeout = (ENV['SECGEN_AGENT_WAIT'] || 60).to_i)
    node, vmid = proxmox_node_vmid
    deadline = Time.now + timeout
    loop do
      return true if proxmox_connection.qemu_agent_running?(vmid, node)
      skip!("QEMU Guest Agent on VM #{vmid} did not respond within #{timeout}s") if Time.now >= deadline
      sleep 5
    end
  end

  # Is the given TCP port listening inside the guest? (checked locally on the guest)
  def guest_port_listening?(port)
    # ss is present on modern Debian/Kali; fall back to a bash /dev/tcp probe.
    cmd = "ss -ltn 2>/dev/null | grep -q ':#{port.to_i} ' && echo LISTENING || " \
          "(exec 3<>/dev/tcp/127.0.0.1/#{port.to_i} && echo LISTENING) 2>/dev/null"
    run_vagrant_ssh(cmd, timeout: 15)[:stdout].include?('LISTENING')
  end

  # The system's IP, resolved on first use. SKIPs the test if it can't be found.
  def system_ip
    @system_ip ||= get_system_ip
  end

  def get_system_ip
    if proxmox?
      # first non-loopback IPv4 from agent/network-get-interfaces (our own guest)
      ip = other_system_ip(get_system_name)
      skip!("Could not determine #{get_system_name}'s IP via the QEMU Guest Agent") if ip.nil?
      return ip
    end
    vagrant_file_path = "#{get_project_path}/Vagrantfile"
    vagrantfile = File.read(vagrant_file_path)
    ip_line = vagrantfile.split("\n").delete_if {|line| !line.include? "# ip_address_for_#{get_system_name}"}[0]
    skip!("No IP for #{get_system_name} in the Vagrantfile") if ip_line.nil?
    ip_address = ip_line.split('=')[-1].strip
    skip!("Cannot test #{get_system_name} over the network: it uses DHCP") if ip_address == "DHCP"
    ip_address
  end

  def get_json_inputs
    json_inputs_path = "#{File.expand_path('../', self.module_path)}/secgen_functions/files/json_inputs/*"
    json_inputs_files = Dir.glob(json_inputs_path)
    json_inputs_files.delete_if do |path|
      end_path = path.split('/')[-1]
      !end_path.include?(self.module_name)
    end
    if json_inputs_files.size > 0
      return JSON.parse(Base64.strict_decode64(File.read(json_inputs_files.first)))
    end
    {}
  end

  def get_port
    if get_json_inputs != {} and get_json_inputs['port'] != nil
      get_json_inputs['port'][0].to_i
    else
      -1
    end
  end

  # Pass __FILE__ in from subclasses
  def get_module_path(file_path)
    "#{File.expand_path('..', File.dirname(file_path))}"
  end

  # Note: returns proftpd_testing
  def get_system_name
    get_system_path.match(/.*?([^\/]*)$/i).captures[0]
  end

  # Note: returns /home/thomashaw/git/SecGen/projects/SecGen20190202_010552/puppet/proftpd_testing
  def get_system_path
    "#{File.expand_path('../../', self.module_path)}"
  end

  # Note: returns /home/thomashaw/git/SecGen/projects/SecGen20190202_010552/
  def get_project_path
    "#{File.expand_path('../../../../', self.module_path)}"
  end

  # The module's directory name in the project (unique per system), e.g. proftpd.
  def module_dir_name
    File.basename(self.module_path)
  end

  # projects/<id>/test_results/<system>/
  def results_dir
    "#{get_project_path}/test_results/#{get_system_name}"
  end

  def is_port_open?(ip, port)
    retries = 5
    while retries > 0
      begin
        Timeout::timeout(2) do
          begin
            s = TCPSocket.new(ip, port)
            s.close
            return true
          rescue Errno::ECONNREFUSED, Errno::EHOSTUNREACH
            # do nothing
          end
        end
      rescue Timeout::Error
        # ignored
      end
      retries -= 1
    end
    false
  end

  private

  # Legacy tests push "PASSED: ..." / "FAILED: ..." lines; record them as checks.
  def record_legacy_line(line)
    return unless line.is_a?(String)
    status = { 'PASSED:' => 'PASS', 'FAILED:' => 'FAIL', 'SKIPPED:' => 'SKIP' }.find { |p, _| line.start_with?(p) }&.last
    return unless status
    text = line.sub(/\A\w+:\s*/, '')
    @results << { tier: @current_tier || 1, name: text[0, 200], status: status, detail: text }
    self.all_tests_passed = false if status == 'FAIL'
  end

  # For messages only: the IP if already known/resolvable, never SKIPs.
  def ip_label
    resolved_ip_or_nil || 'unknown IP'
  end

  def resolved_ip_or_nil
    return nil if @ip_unresolvable
    system_ip
  rescue SkipTest, StandardError
    @ip_unresolvable = true
    nil
  end

  def host_http_get(path, port, scheme)
    http = Net::HTTP.new(system_ip, port)
    http.use_ssl = scheme == 'https'
    http.verify_mode = OpenSSL::SSL::VERIFY_NONE if http.use_ssl?
    http.open_timeout = 10
    http.read_timeout = 15
    response = http.get(path)
    { status: response.code.to_i, body: response.body.to_s, error: nil }
  rescue SkipTest
    raise
  rescue StandardError => e
    { status: 0, body: '', error: "#{e.class}: #{e.message}" }
  end

  HTTP_STATUS_MARKER = '__SECGEN_HTTP_STATUS__'.freeze

  def guest_http_get(host, path, port, scheme)
    url = Shellwords.escape("#{scheme}://#{host}:#{port.to_i}#{path}")
    request = Shellwords.escape("GET #{path} HTTP/1.0\\r\\nHost: #{host}\\r\\nConnection: close\\r\\n\\r\\n")
    m = HTTP_STATUS_MARKER
    cmd = "if command -v curl >/dev/null 2>&1; then " \
            "curl -sk --max-time 15 -o - -w '\\n#{m}%{http_code}' #{url}; " \
          "elif command -v wget >/dev/null 2>&1; then " \
            "wget -q -O - --no-check-certificate -T 15 -S #{url} 2>/tmp/.secgen_wget_hdr; rc=$?; " \
            "code=$(awk '/^  HTTP\\//{c=$2} END{print c}' /tmp/.secgen_wget_hdr); rm -f /tmp/.secgen_wget_hdr; " \
            "printf '\\n#{m}%s' \"${code:-000}\"; " \
          "else " \
            "out=$(timeout 15 bash -c 'exec 3<>/dev/tcp/#{host}/#{port.to_i} && printf %b \"$1\" >&3 && cat <&3' _ #{request} 2>/dev/null); " \
            "code=$(printf '%s' \"$out\" | head -1 | awk '{print $2}'); " \
            "printf '%s' \"$out\" | sed '1,/^\\r\\{0,1\\}$/d'; printf '\\n#{m}%s' \"${code:-000}\"; " \
          "fi"
    result = run_vagrant_ssh(cmd, timeout: 30)
    out = result[:stdout]
    idx = out.rindex(m)
    return { status: 0, body: '', error: "no HTTP response (#{result[:stderr].to_s.strip[0, 200]})" } if idx.nil?
    status = out[(idx + m.size)..-1].to_i
    body = out[0...idx].sub(/\n\z/, '')
    { status: status, body: body, error: status == 0 ? "could not connect to #{host}:#{port}" : nil }
  end

  def read_banner(port, send: nil)
    if proxmox?
      cmd = "timeout 10 bash -c 'exec 3<>/dev/tcp/127.0.0.1/#{port.to_i} || exit 1; " \
            "[ -n \"$1\" ] && printf \"%s\\r\\n\" \"$1\" >&3; timeout 3 cat <&3' _ " \
            "#{Shellwords.escape(send.to_s)} 2>/dev/null | head -c 4096"
      return run_vagrant_ssh(cmd, timeout: 20)[:stdout].to_s
    end
    data = ''.dup
    Timeout.timeout(10) do
      sock = TCPSocket.new(system_ip, port)
      sock.write("#{send}\r\n") if send
      while IO.select([sock], nil, nil, 3)
        chunk = sock.read_nonblock(4096, exception: false)
        break if chunk.nil? || chunk == :wait_readable
        data << chunk
        break if data.size >= 4096
      end
      sock.close
    end
    data
  rescue SkipTest
    raise
  rescue StandardError
    data || ''
  end

  # Standard evidence for a failed module (service status, recent journal,
  # listening ports) plus anything added with add_evidence, in one guest call.
  def evidence_commands
    unit = Shellwords.escape("*#{module_name}*")
    {
      'failed_units' => 'systemctl --no-pager --failed 2>&1',
      'service_status' => "systemctl --no-pager --all status #{unit} 2>&1 | head -150",
      'journal' => 'journalctl --no-pager -b -n 200 2>&1',
      'listening_ports' => 'ss -ltnup 2>&1 || netstat -ltnup 2>&1',
      'processes' => 'ps auxf 2>&1 | head -200'
    }.merge(@extra_evidence)
  end

  EVIDENCE_MARKER = '===SECGEN_EVIDENCE '.freeze

  def collect_evidence
    commands = evidence_commands
    script = commands.map { |name, cmd| "echo '#{EVIDENCE_MARKER}#{name}==='; ( #{cmd} )" }.join("\n")
    output = run_vagrant_ssh(script, timeout: 60)
    files = []
    output[:stdout].split(/^#{Regexp.escape(EVIDENCE_MARKER)}/).each do |section|
      name, body = section.split("===\n", 2)
      next if body.nil? || !commands.key?(name)
      files << save_evidence(name, "$ #{commands[name]}\n#{body}")
    end
    files.compact!
    @results.each { |r| r[:evidence] = ((r[:evidence] || []) + files).uniq if r[:status] == 'FAIL' }
    files
  rescue SkipTest, StandardError => e
    warn "WARNING: could not collect evidence: #{e.class}: #{e.message}"
    []
  end

  # Writes evidence/<module>/<name>.txt; returns its path relative to results_dir.
  def save_evidence(name, text)
    return nil if text.nil?
    rel = "evidence/#{module_dir_name}/#{name.to_s.gsub(/[^\w.-]/, '_')}.txt"
    FileUtils.mkdir_p(File.dirname("#{results_dir}/#{rel}"))
    File.write("#{results_dir}/#{rel}", text)
    rel
  rescue StandardError => e
    warn "WARNING: could not save evidence #{name}: #{e.message}"
    nil
  end

  # Repo path of the module (e.g. modules/services/unix/ftp/proftpd), from the
  # project's resolved scenario.xml; nil if it can't be found.
  def repo_module_path
    doc = REXML::Document.new(File.read("#{get_project_path}/scenario.xml"))
    doc.root.each_element('system') do |system|
      next unless system.elements['system_name']&.text.to_s.strip == get_system_name
      system.each_element('*[@module_path]') do |mod|
        path = mod.attributes['module_path']
        return path if File.basename(path) == module_dir_name
      end
    end
    nil
  rescue StandardError
    nil
  end

  def write_results(status)
    finished = Time.now
    data = {
      module: module_dir_name,
      module_path: repo_module_path,
      test: self.class.name,
      system: get_system_name,
      backend: proxmox? ? 'proxmox-guest-agent' : 'vagrant',
      started_at: (@started_at || finished).iso8601,
      duration_s: (finished - (@started_at || finished)).round(1),
      status: status,
      tier_reached: tier_reached,
      results: @results
    }
    FileUtils.mkdir_p(results_dir)
    File.write("#{results_dir}/#{module_dir_name}.json", JSON.pretty_generate(data) + "\n")
  rescue StandardError => e
    warn "WARNING: could not write test results JSON: #{e.class}: #{e.message}"
  end
end
