require_relative '../../../../../lib/post_provision_test'

class Bind9Test < PostProvisionTest
  def initialize
    self.module_name = 'bind9'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) { test_local_command('bind9 installed?', 'dpkg -l bind9 | grep -E "^ii"', 'bind9') }

    tier(2) do
      test_local_command('named running?', 'ps -ef | grep named', 'named')
      test_service_up(port: 53)
    end

    domain = self.json_inputs['domain']&.first
    (self.json_inputs['dns_records'] || []).each do |record|
      if record.include?('|')
        hostname, type, value = record.split('|')
        type = type&.upcase
      else
        hostname, value = record.split('=')
        type = 'A'
      end
      next unless %w[A AAAA].include?(type)
      fqdn = domain ? "#{hostname}.#{domain}" : hostname
      tier(2) { test_local_command("#{fqdn} #{type} resolves", "dig +short @127.0.0.1 #{fqdn} #{type}", value) }
    end
  end
end

Bind9Test.new.run
