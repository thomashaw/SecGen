require_relative './print.rb'
class Rules

  # Generate audit and alerting rules

  # @type current valid values are ['elastalert', 'auditbeat']
  def self.generate_rules(type, mod)
    rules = []
    if type == 'elastalert'
      mod.goals.keys.each do |key|
        case key
        when 'read_file'
        when 'write_file'
        when 'access_account'
        else
        end
      end
    elsif type == 'auditbeat'
      mod.goals.keys.each do |key|
        case key
        when 'read_file'
          # Generate auditbeat read_file rules based on paths
          read_files = mod.goals[key]
          read_files.each do |path|
            rules << greedy_auditbeat_rule(path, 'r')
          end
        when 'write_file'
          # TODO: do something
          read_files = mod.goals[key]
          read_files.each do |path|
            rules << greedy_auditbeat_rule(path, 'w')
          end
        when 'access_account'
        else
          Print.err('Unknown goal type')
          raise
        end
      end
    else
      Print.err("Error, no valid rule type specified")
      raise
    end
    rules.join("\n")
  end

  # Generates a greedy read or write rule for auditbeat (e.g. /home/user/file_name resolves to /home)
  def self.greedy_auditbeat_rule(path, r_w)
    base_path = path.split('/')[0..1].join('/') + '/'
    key = base_path.gsub(/[^A-Za-z0-9\-\_]/, '')
    "-w #{base_path} -p -#{r_w} -k #{key}"
  end
end