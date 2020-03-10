require_relative './print.rb'
require_relative './scenario.rb'

class Rules
  # Generate audit and alerting rules

  def self.generate_auditbeat_rules(mod)
    rules = []
    mod.goals.keys.each do |key|
    # Generate auditbeat rules based on rule type
    case key
      when 'read_file'
        read_files = mod.goals[key]
        read_files.each do |entry|
          paths = (entry.is_a? String) ? [entry] : entry
          paths.each do |path|
            generated_rule = greedy_auditbeat_rule(path, 'r')
            rules << generated_rule
          end
        end
      when 'modify_file'
      when 'access_account'
      when 'service_down'
      when 'system_down'
      else
        Print.err('Unknown goal type')
        raise
      end
    end
    rules
  end

  # Generates a greedy read or write rule for auditbeat (e.g. /home/user/file_name resolves to /home)
  def self.greedy_auditbeat_rule(path, r_w)
    base_path = path.split('/')[0..1].join('/') + '/'
    key = base_path.gsub(/[^A-Za-z0-9\-\_]/, '')
    "-w #{base_path} -p #{r_w} -k #{key}"
  end


  def self.generate_elastalert_rule(hostname, module_name, goal, sub_goal, counter)
    rule = ''
    # switch case to determine which type of rule we're returning (read file, etc.)
    case goal[0]
    when 'read_file'
      rule = generate_elastalert_rule_rf(hostname, module_name, goal, sub_goal, counter)
    when 'modify_file'
      # rule = generate_elastalert_rule_mf(hostname, module_name, goal, sub_goal)
    when 'access_account'
      # rule = generate_elastalert_rule_aa(hostname, module_name, goal, sub_goal)
    when 'service_down'
      # rule = generate_elastalert_rule_svcd(hostname, module_name, goal, sub_goal)
    when 'system_down'
      # rule = generate_elastalert_rule_sysd(hostname, module_name, goal, sub_goal)
    else
      raise 'unknown_goal_type'
    end
    rule
  end

  def self.generate_elastalert_rule_rf(hostname, module_name, goal, sub_goal, counter)
    "name: #{get_ea_rulename(hostname, module_name, goal, counter)}\n" +
    "type: any\n" +
    "index: auditbeat-*\n" +
    "filter:\n" +
    "  - query:\n" +
    "      query_string:\n" +
    '        query: "combined_path: \"' + sub_goal + '\" AND auditd.result: success AND event.action: opened-file"' + "\n" +
    "alert:\n" +
    "  - \"modules.alerter.exec.ExecAlerter\"\n" +
    "command: [\"/usr/bin/ruby\", \"/opt/alert_actioner/alert_router.rb\"]\n" +
    "pipe_match_json: true\n" +
    "realert:\n" +
    "  minutes: 0\n"
  end

  def self.get_ea_rulename(hostname, module_name, goal, counter)
    rule_type = RuleTypes.get_rule_type(goal)
    return "#{hostname}-#{module_name}-#{rule_type}-#{counter}"
  end

  class RuleTypes
    READ_FILE = 'rf'
    MODIFY_FILE = 'mf'
    ACCESS_ACCOUNT = 'aa'
    SERVICE_DOWN = 'svcd'
    SYSTEM_DOWN = 'sysd'

    def self.get_rule_type(goal)
      case goal[0]
      when 'read_file'
        READ_FILE
      when 'modify_file'
        MODIFY_FILE
      when 'access_account'
        ACCESS_ACCOUNT
      when 'service_down'
        SERVICE_DOWN
      when 'system_down'
        SYSTEM_DOWN
      else
        raise 'unknown_rule_type'
      end
    end
  end
end