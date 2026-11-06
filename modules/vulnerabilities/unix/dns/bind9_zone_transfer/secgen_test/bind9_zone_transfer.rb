require_relative '../../../../../lib/post_provision_test'
require 'shellwords'

class Bind9ZoneTransferTest < PostProvisionTest
  def initialize
    self.module_name = 'bind9_zone_transfer'
    self.module_path = get_module_path(__FILE__)
    super
  end

  # Inputs (all read_facts) and how each is covered:
  #   domain         - the zone name; used to target dig/AXFR and checked below.
  #   ns_hostname    - the SOA/NS name; checked below.
  #   serial         - the zone serial; checked below.
  #   dns_records    - every record is iterated: each is asserted to resolve, and
  #                    the AXFR is asserted to leak each internal host and the flag.
  #   allow_transfer - the misconfiguration; asserted 'any' in named.conf.local and
  #                    proven by a successful AXFR. (This module's default is 'any';
  #                    a scenario that overrides it to 'none' would harden the box,
  #                    so the AXFR-allowed assertions are gated on this input.)
  def test_module
    super
    domain         = self.json_inputs['domain']&.first
    ns_hostname    = self.json_inputs['ns_hostname']&.first
    serial         = self.json_inputs['serial']&.first
    allow_transfer = self.json_inputs['allow_transfer']&.first
    records        = self.json_inputs['dns_records'] || []

    # Parse the dns_record strings ("hostname|type|value[|priority]", or the
    # legacy "hostname=ip"). Collect the internal A/AAAA hosts and any flag
    # planted in a TXT record so we can prove the AXFR leaks them.
    parsed = records.map do |record|
      if record.include?('|')
        hostname, type, value, priority = record.split('|')
        [hostname, type&.upcase, value, priority]
      else
        hostname, value = record.split('=')
        [hostname, 'A', value, nil]
      end
    end.reject { |h, t, v, _| h.nil? || t.nil? || v.nil? }

    leaked_hosts = parsed.select { |_, t, _, _| %w[A AAAA].include?(t) }.map { |h, _, _, _| h }
    flag_values  = parsed.select { |_, t, v, _| t == 'TXT' && v =~ /flag\{.*\}/ }.map { |_, _, v, _| v }

    tier(1) do
      test_local_command('bind9 installed?', 'dpkg -l bind9 | grep -E "^ii"', 'bind9')
      test_local_command('named config is valid', 'named-checkconf && echo conf-ok', 'conf-ok')
      test_local_command('bind9 enabled at boot', 'systemctl is-enabled named || systemctl is-enabled bind9', 'enabled')
      if domain
        test_local_command("zone #{domain} declared", 'cat /etc/bind/named.conf.local', "zone \"#{domain}\"")
        test_local_command("zone file for #{domain} exists", "test -f /etc/bind/db.#{domain} && echo present", 'present')
        test_local_command("zone #{domain} is valid", "named-checkzone #{domain.shellescape} /etc/bind/db.#{domain.shellescape}", 'OK')
      end
      # the misconfiguration is written into the zone's transfer policy
      if allow_transfer
        test_local_command('allow-transfer policy set', 'cat /etc/bind/named.conf.local', "allow-transfer { #{allow_transfer}; }")
      end
    end

    tier(2) do
      test_local_command('named running?', 'ps -ef | grep "[n]amed"', 'named')
      test_service_up(port: 53)

      if domain
        soa = "dig +short @127.0.0.1 #{domain.shellescape} SOA"
        test_local_command("#{domain} SOA names #{ns_hostname}", soa, "#{ns_hostname}.") if ns_hostname
        test_local_command("#{domain} SOA serial #{serial}", soa, serial) if serial

        # The vulnerability: an unrestricted AXFR hands the whole zone to anyone.
        # Only assert this when the policy actually allows it (the module default).
        if allow_transfer == 'any'
          axfr = "dig axfr @127.0.0.1 #{domain.shellescape}"
          test_local_command('unrestricted zone transfer allowed',
                             "#{axfr} | grep -E 'XFR size|Transfer failed'", 'XFR size')
          # the AXFR leaks every internal host record...
          leaked_hosts.each do |host|
            fqdn = host == '@' ? domain : "#{host}.#{domain}"
            test_local_command("AXFR leaks host #{fqdn}", axfr, fqdn)
          end
          # ...and the flag planted in a TXT record
          flag_values.each do |flag|
            test_local_command('AXFR leaks the TXT flag', axfr, flag)
          end
        end
      end
    end

    # Every configured record resolves over a normal query (parameterisation-aware:
    # whatever records this deployment was given, each must answer correctly).
    parsed.each do |hostname, type, value, priority|
      next unless domain
      fqdn = hostname == '@' ? domain : "#{hostname}.#{domain}"
      expected = case type
                 when 'A', 'AAAA' then value
                 when 'TXT' then value
                 when 'MX' then "#{priority || '10'} #{value}."
                 when 'CNAME', 'NS', 'PTR' then "#{value}."
                 end
      next if expected.nil?
      tier(2) { test_local_command("#{fqdn} #{type} resolves", "dig +short @127.0.0.1 #{fqdn.shellescape} #{type}", expected) }
    end

    # Tier 3: prove the misconfiguration with the stock MSF auxiliary module.
    # run_msf_module runs it from the attacker VM (RHOSTS set automatically) and
    # returns :msf_output. The assertion below is a generic success heuristic (an
    # MSF "[+]" finding line). TODO t3-worker: tighten it to a module-specific
    # token from the msf_output_enum_dns evidence, and add any options the module
    # needs (credentials, RPORT) from the module's json_inputs.
    tier(3) do
      aux = run_msf_module('auxiliary/gather/enum_dns',
                           options: {},
                           label: 'bind9_zone_transfer')
      next unless aux

      out = aux[:msf_output].to_s
      if out.include?('[+]')
        pass_check('bind9_zone_transfer proves the misconfiguration',
                   "auxiliary/gather/enum_dns on #{aux[:attacker]} reported a finding against #{aux[:rhost]}",
                   tier: 3)
      else
        fail_check('bind9_zone_transfer proves the misconfiguration',
                   'No MSF "[+]" success line in the auxiliary output; see msf_output evidence.',
                   tier: 3)
      end
    end
  end
end

Bind9ZoneTransferTest.new.run
