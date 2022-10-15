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

    if path.count('/') == 1 # is root dir
      Print.err("ERROR: auditd cannot monitor the root directory - revise your scenario goals!")
      Print.err(" path: #{path}")
      Print.err(" r_w: #{r_w}")
      exit(1)
    else
      base_path = path.split('/')[0..1].join('/') + '/'
      key = base_path.gsub(/[^A-Za-z0-9\-\_]/, '')
      "-w #{base_path} -p #{r_w} -k #{key}"
    end
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
      rule = generate_elastalert_rule_acc(hostname, module_name, goal, counter)
    when RuleTypes::SERVICE_DOWN
      # rule = generate_elastalert_rule_svcd(hostname, module_name, goal, sub_goal)
    when RuleTypes::SYSTEM_DOWN
      # rule = generate_elastalert_rule_sysd(hostname, module_name, goal, sub_goal)
    else
      raise 'unknown_goal_type'
    end
    rule
  end

  # source_name: either system name or module name,
  def self.generate_elastalert_rule_rf(hostname, source_name, goal, counter)
    "name: #{get_ea_rulename(hostname, source_name, goal, counter)}\n" +
        "type: any\n" +
        "index: auditbeat-*\n" +
        "filter:\n" +
        "  - query:\n" +
        "      query_string:\n" +
        '        query: "combined_path: \"' + goal['file_path'] + '\""' + "\n" +
        "  - query:\n" +
        "      query_string:\n" +
        '        query: "auditd.result: success"' + "\n" +
        "  - query:\n" +
        "      query_string:\n" +
        '        query: "event.action: opened-file"' + "\n" +
        "  - query:\n" +
        "      query_string:\n" +
        "        query: \"process.executable: \\\"/bin/cat\\\" OR \\\"/usr/bin/vim.basic\\\" OR \\\"/usr/bin/less\\\" OR \\\"/bin/more\\\" OR \\\"/bin/nano\\\" OR  \\\"/usr/bin/kate\\\"\"" + "\n" +
        # Different OR clause in EA
        #
        # TODO: WIP - improve this rule!
        #
        #
        # '        query: "combined_path: \"' + goal['file_path'] + '\" AND auditd.result: success AND event.action: opened-file"' + "\n" +
        #
        # process: {
        #     "executable": "/bin/su",
        #     "name": "su",
        #     "pid": 11853,
        #     "ppid": 5982,
        #     "title": "su vagrant",
        #     "working_directory": "/home/challenger"/
        # }
        #
        # process: {
        #     "executable": "/usr/bin/passwd",
        #     "name": "passwd",
        #     "pid": 13027,
        #     "ppid": 13020,
        #     "title": "passwd",
        #     "working_directory": "/home/vagrant"
        # }
        #
        #
        #
        "alert:\n" +
        "  - \"elastalert.modules.alerter.exec.ExecAlerter\"\n" +
        "command: [\"/usr/bin/ruby\", \"/opt/alert_actioner/alert_router.rb\", \"raise\", \"--alert-name\", \"" + get_ea_rulename(hostname, source_name, goal, counter) +"\"]\n" +
        "pipe_match_json: true\n" +
        "realert:\n" +
        "  minutes: 0\n"
  end

  def self.generate_elastalert_rule_acc(hostname, source_name, goal, counter)
    "name: #{get_ea_rulename(hostname, source_name, goal, counter)}\n" +
        "type: any\n" +
        "index: auditbeat-*\n" +
        "filter:\n" +
        "  - query:\n" +
        "      query_string:\n" +
        # on box as:
                # query: "related.user: 'crackme' AND (event.category: 'authentication' OR event.category: 'session') AND (event.action: 'user_login' OR event.action: 'started-session' OR event.action: 'acquired-credentials') AND event.outcome: 'success'"
        '        query: "related.user: \'' + goal['account_name'] +'\' AND (event.action: \'user_login\' OR event.action: \'started-session\' OR event.action: \'acquired-credentials\') AND event.outcome: \'success\'"' + "\n" +
        "alert:\n" +
        "  - \"elastalert.modules.alerter.exec.ExecAlerter\"\n" +
        "command: [\"/usr/bin/ruby\", \"/opt/alert_actioner/alert_router.rb\", \"raise\", \"--alert-name\", \"" + get_ea_rulename(hostname, source_name, goal, counter) +"\"]\n" +
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
    ACCESS_ACCOUNT = 'acc'
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