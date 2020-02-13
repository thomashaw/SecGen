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
          read_files = mod.goals[key]
          read_files.each do |entry|
            paths = (entry.is_a? String) ? [entry] : entry
            paths.each do |path|
              rules << specific_elastalert_rule(path, 'r')
            end
          end
        when 'modify_file'
        when 'access_account'
        when 'service_down'
        when 'system_down'
          # TODO: Add exfiltration rule
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
        when 'modify_file'
          # TODO: do something
          read_files = mod.goals[key]
          read_files.each do |path|
            rules << greedy_auditbeat_rule(path, 'w')
          end
        when 'access_account'
        when 'service_down'
        when 'system_down'
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
    "-w #{base_path} -p #{r_w} -k #{key}"
  end


  # TODO: path == a string of an array - "["/etc/shadow", "/home/carolmiller/flag.txt"]"
  # TODO: fix the eval problem + read arrays out into array format
  def self.specific_elastalert_rule(path, r_w)

    # TODO: add AND read/write events into rule
    # TODO: add AND some unique idenitifier for a user/vm (ip address or hostname are probably best)
    "name: #{get_ea_rulename(path)}\n" +
        "type: any\n" +
        "index: auditbeat-*\n" +
        "filter:\n" +
        "  - query:\n" +
        "    query_string:\n" +
        '      query: "combined_path: ' + get_escaped_path(path) + "\n" +
        "alert:\n" +
        "  - command\n" +
        "command: [\"/usr/bin/tee\", \"-a\", \"/root/alerts\"]\n" +
        "pipe_match_json: true\n"
  end

  def self.get_ea_rulename(path)
    # TODO: Make this unique by combining the path with unique identifier for the scenario (e.g. IP address or hostname)
    return "example-rulename-#{rand(0..99999)}"
  end

  def self.get_escaped_path(path)
    # Working, but doesn't include the asterisks. Check if this is necessary later.
    # e.g. "/home/vagrant/testfile" =>  \"\\/home\\/vagrant\\/testfile\""
    #                               not \"*\\/home\\/vagrant\\/*testfile\""
    '\\"' + "#{path.gsub('/','\\\/').gsub('/','\\\\/')}" + '\\""'
  end

end