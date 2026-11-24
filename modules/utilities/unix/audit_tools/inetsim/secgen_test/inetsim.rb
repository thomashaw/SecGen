require_relative '../../../../../lib/post_provision_test'

class InetsimTest < PostProvisionTest
  # Sends one A query for a made-up name to the local resolver and prints the address in the answer.
  DNS_PROBE = <<~'PY'.freeze
    import socket
    name = b''.join(bytes([len(p)]) + p.encode() for p in 'secgen-probe.example'.split('.'))
    query = b'\x12\x34\x01\x00\x00\x01\x00\x00\x00\x00\x00\x00' + name + b'\x00\x00\x01\x00\x01'
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    s.settimeout(5)
    s.sendto(query, ('127.0.0.1', 53))
    print('answer=' + socket.inet_ntoa(s.recv(512)[-4:]))
  PY

  # Fetches INetSim's default page from the local fake HTTP server.
  HTTP_PROBE = <<~'PY'.freeze
    import urllib.request
    body = urllib.request.urlopen('http://127.0.0.1/', timeout=5).read().decode(errors='replace')
    print('http=' + ('INetSim' if 'INetSim' in body else 'other'))
  PY

  def initialize
    self.module_name = 'inetsim'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    dns_ip = self.json_inputs['dns_default_ip']&.first
    start_service = self.json_inputs['start_service']&.first.to_s != 'false'
    dns_probe = "python3 -c #{Shellwords.escape(DNS_PROBE)}"
    http_probe = "python3 -c #{Shellwords.escape(HTTP_PROBE)}"

    tier(1) do
      test_local_command('inetsim package installed?', "dpkg -l inetsim | grep '^ii'", 'inetsim')
      test_local_command('services bound to all interfaces', "grep '^service_bind_address' /etc/inetsim/inetsim.conf", 'service_bind_address 0.0.0.0')
      if dns_ip
        test_local_command('DNS answers set to dns_default_ip', "grep '^dns_default_ip' /etc/inetsim/inetsim.conf", "dns_default_ip #{dns_ip}")
        # used where INetSim's own DNS cannot run (Kali); written everywhere
        test_local_command('dnsmasq stand-in answers with dns_default_ip', "grep '^address=' /etc/inetsim/dnsmasq.conf", "address=/#/#{dns_ip}")
      end
    end

    add_evidence('inetsim log', 'tail -n 60 /var/log/inetsim/main.log 2>&1')
    add_evidence('dnsmasq stand-in', 'systemctl status inetsim-dns --no-pager 2>&1; tail -n 20 /var/log/inetsim/dns.log 2>&1')
    add_evidence('listening sockets', 'ss -lntup 2>&1')

    tier(2) do
      if start_service
        test_http('/', match: 'INetSim', port: 80, label: 'fake HTTP server answers with its default page')
        test_local_command('fake DNS answers any name with dns_default_ip', dns_probe, "answer=#{dns_ip}") if dns_ip
      else
        test_command_succeeds('INetSim left stopped and disabled (start_service false)',
                              '! systemctl is-active --quiet inetsim && ! systemctl is-enabled --quiet inetsim')
        # A student starts it by hand: HTTP and DNS must then answer. Stop it again afterwards.
        expect = dns_ip ? "answer=#{dns_ip}" : 'http=INetSim'
        test_local_command('INetSim answers HTTP and DNS once started by hand',
                           "systemctl start inetsim && sleep 8; #{http_probe}; #{dns_probe}; systemctl stop inetsim",
                           expect)
        test_command_succeeds('INetSim stopped again after the check',
                              '! systemctl is-active --quiet inetsim && ! systemctl is-active --quiet inetsim-dns')
      end
    end
  end
end

InetsimTest.new.run
