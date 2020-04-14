require_relative './print.rb'
require_relative './scenario.rb'

class Rules
  # Generate audit and alerting rules

  def self.generate_auditbeat_rules(goals)
    rules = []
    goals.each do |goal|
      # Generate auditbeat rules based on rule type
      rule_type = RuleTypes.get_rule_type(goal['goal_type'])
      case rule_type
      when RuleTypes::READ_FILE
        rules << greedy_auditbeat_rule(goal['file_path'], 'r')
      when RuleTypes::MODIFY_FILE
      when RuleTypes::ACCESS_ACCOUNT
      when RuleTypes::SERVICE_DOWN
      when RuleTypes::SYSTEM_DOWN
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


  def self.generate_elastalert_rule(hostname, module_name, goal, counter)
    rule = ''
    # switch case to determine which type of rule we're returning (read file, etc.)
    rule_type = RuleTypes.get_rule_type(goal['goal_type'])
    case rule_type
    when RuleTypes::READ_FILE
      rule = generate_elastalert_rule_rf(hostname, module_name, goal, counter)
    when RuleTypes::MODIFY_FILE
      # rule = generate_elastalert_rule_mf(hostname, module_name, goal, sub_goal)
    when RuleTypes::ACCESS_ACCOUNT
      # rule = generate_elastalert_rule_aa(hostname, module_name, goal, sub_goal)
    when RuleTypes::SERVICE_DOWN
      # rule = generate_elastalert_rule_svcd(hostname, module_name, goal, sub_goal)
    when RuleTypes::SYSTEM_DOWN
      # rule = generate_elastalert_rule_sysd(hostname, module_name, goal, sub_goal)
    else
      raise 'unknown_goal_type'
    end
    rule
  end

  def self.generate_elastalert_rule_rf(hostname, module_name, goal, counter)
    "name: #{get_ea_rulename(hostname, module_name, goal, counter)}\n" +
        "type: any\n" +
        "index: auditbeat-*\n" +
        "filter:\n" +
        "  - query:\n" +
        "      query_string:\n" +
        '        query: "combined_path: \"' + goal['file_path'] + '\" AND auditd.result: success AND event.action: opened-file"' + "\n" +
        "alert:\n" +
        "  - \"modules.alerter.exec.ExecAlerter\"\n" +
        "command: [\"/usr/bin/ruby\", \"/opt/alert_actioner/alert_router.rb\"]\n" +
        "pipe_match_json: true\n" +
        "realert:\n" +
        "  minutes: 0\n"
  end

  def self.get_ea_rulename(hostname, module_name, goal, counter)
    rule_type = RuleTypes.get_rule_type(goal['goal_type'])
    return "#{hostname}-#{module_name}-#{rule_type}-#{counter}"
  end

  class RuleTypes
    READ_FILE = 'rf'
    MODIFY_FILE = 'mf'
    ACCESS_ACCOUNT = 'aa'
    SERVICE_DOWN = 'svcd'
    SYSTEM_DOWN = 'sysd'

    def self.get_rule_type(rule_type)
      case rule_type
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