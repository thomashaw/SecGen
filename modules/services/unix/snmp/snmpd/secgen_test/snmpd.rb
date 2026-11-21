require 'shellwords'
require_relative '../../../../../lib/post_provision_test'

class SnmpdTest < PostProvisionTest
  def initialize
    self.module_name = 'snmpd'
    self.module_path = get_module_path(__FILE__)
    super
  end

  # Every read_fact is checked: agent_address, ro_community, sys_location and
  # sys_contact. The community is a throwaway generated one; it is only ever on
  # the guest's own command lines.
  def test_module
    super
    agent     = self.json_inputs['agent_address']&.first
    community = self.json_inputs['ro_community']&.first
    location  = self.json_inputs['sys_location']&.first
    contact   = self.json_inputs['sys_contact']&.first

    # First agentAddress entry -> [bind ip or nil, port]; "udp:161" binds every interface
    bind_ip = nil
    port = 161
    if agent
      entry = agent.split(',').first.to_s.strip.sub(/\A(udp6?|tcp6?):/i, '')
      if entry =~ /\A(?:(\d+\.\d+\.\d+\.\d+):)?(\d+)\z/
        bind_ip = Regexp.last_match(1)
        port = Regexp.last_match(2).to_i
      end
    end
    query_host = bind_ip && bind_ip != '0.0.0.0' ? bind_ip : '127.0.0.1'
    snmp = lambda do |oid, comm, host = query_host|
      "snmpget -v2c -t 2 -r 0 -On -Oqv -c #{Shellwords.escape(comm)} #{host}:#{port} #{oid}"
    end
    sys_descr = '.1.3.6.1.2.1.1.1.0'

    tier(1) do
      test_local_command('snmpd installed', 'dpkg -l snmpd | grep -E "^ii"', 'snmpd')
      test_local_command('snmp client installed', 'which snmpget', '/usr/bin/snmpget')
      test_local_command('snmpd enabled at boot', 'systemctl is-enabled snmpd', 'enabled')
      test_local_command('include directory exists', 'test -d /etc/snmp/snmpd.conf.d && echo present', 'present')
      test_local_command('snmpd.conf includes the directory',
                         'grep -E "^includeDir /etc/snmp/snmpd.conf.d\\s*$" /etc/snmp/snmpd.conf', 'includeDir')
      test_local_command('agentAddress configured',
                         "grep -E '^agentAddress ' /etc/snmp/snmpd.conf", agent.to_s) if agent
      test_local_command('sysLocation configured',
                         "grep -F #{Shellwords.escape("sysLocation #{location}")} /etc/snmp/snmpd.conf", location.to_s) if location
      test_local_command('sysContact configured',
                         "grep -F #{Shellwords.escape("sysContact #{contact}")} /etc/snmp/snmpd.conf", contact.to_s) if contact
      test_local_command('no leftover public community',
                         'grep -Eic "^ro(community6?|user) +public( |$)" /etc/snmp/snmpd.conf; true', '0')
    end

    tier(2) do
      test_local_command('snmpd service active', 'systemctl is-active snmpd', 'active')
      listen_re = bind_ip ? Regexp.escape(bind_ip) : '(0\.0\.0\.0|\*)'
      test_local_command("snmpd listens on UDP/#{port}",
                         "ss -lun | grep -E '#{listen_re}:#{port}\\s'", ":#{port}")

      if community
        test_local_command('configured community answers sysDescr',
                           snmp.call(sys_descr, community), 'Linux')
        test_local_command('wrong community is refused',
                           "#{snmp.call(sys_descr, "wrong-#{community}")} 2>&1; echo \"rc=$?\"", 'rc=1')
        # system subtree only: hrSystem is in the view, ifTable is not
        test_local_command('view is limited to the system subtree',
                           "#{snmp.call('.1.3.6.1.2.1.2.2.1.1.1', community)} 2>&1", 'No Such')
        # the community is restricted to localhost, so our own external address is refused
        ext = system_ip
        test_local_command('same community refused from a non-local source',
                           "#{snmp.call(sys_descr, community, ext)} 2>&1; echo \"rc=$?\"", 'rc=1') if ext
      end
      test_local_command('sysLocation served',
                         snmp.call('.1.3.6.1.2.1.1.6.0', community), location.to_s) if community && location
      test_local_command('sysContact served',
                         snmp.call('.1.3.6.1.2.1.1.4.0', community), contact.to_s) if community && contact
    end
  end
end

SnmpdTest.new.run
