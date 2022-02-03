require 'nokogiri'

require_relative '../helpers/rules'

# Convert systems objects into alertactioner xml configuration
class XmlAlertActionConfigGenerator

  # @param [Object] systems the list of systems
  # @param [Object] scenario the scenario file used to generate
  # @param [Object] time the current time as a string
  # @param [Array[Hash]] the alert_actioner configuration settings (list of aa_conf JSON hashes)
  def initialize(systems, scenario, time, aa_confs, options)
    @systems = systems
    @scenario = scenario
    @time = time
    @aa_confs = aa_confs
    @options = options
    @alert_actions = []
    @goal_flags = []
  end

  # outputs a XML AlertActioner configuration file
  # @return [Object] xml string
  def output
    create_alert_actions
    generate_xml_config
  end

  def create_alert_actions
    Print.info 'AlertActioner: Creating alert actions from aa_conf.'
    @aa_confs.each do |aa_conf|
      if aa_conf['mapping_type']
        case aa_conf['mapping_type']
        when 'all_goal_flags_to_hacktivity'
          all_goal_flags_to_hacktivity(aa_conf)

        when 'all_goal_messages_to_host'
          all_goal_message_host(aa_conf)
        else
          Print.err("AlertActioner Config: Invalid mapping type #{aa_conf['mapping_type']}")
          exit(1)
        end
      elsif aa_conf['mapping']
        # TODO: Implement me later
      else
        Print.err "AlertActioner Config: Either mapping_type or mapping required."
        exit(1)
      end
    end
  end

  def all_goal_message_host(aa_conf)
    @systems.each do |system|
      system.module_selections.each do |module_selection|
        module_name = module_selection.module_path_end
        module_goals = module_selection.goals
        if module_goals != []
          # Iterate over the goals
          module_selection.goals.each_with_index do |goal, i|
            @alert_actions << {'alert_name' => Rules.get_ea_rulename(system.hostname, module_name, goal, i),
                               'action_type' => 'MessageAction',
                               'host' => aa_conf['host'],
                               'sender' => aa_conf['sender'],
                               'password' => aa_conf['password'],
                               'recipient' => aa_conf['recipient'],
                               'message_header' => aa_conf['message_header'],
                               'message_subtext' => aa_conf['message_subtext']
            }
          end
        end
      end
    end
  end

  def all_goal_flags_to_hacktivity(aa_conf)
    Print.info("**** sending all_goal_flags_to_hacktivity ****")
    auto_grader_hostname = get_auto_grader_hostname
    Print.info("auto_grader_hostname: " + auto_grader_hostname)
    Print.info("systems.size: " + @systems.size.to_s)

    @goal_flags = @goal_flags + $datastore['goal_flags']

    @systems.each do |system|
      Print.info("System goals: " + system.goals.to_s)
      if system.goals != []
        Print.info("System level goals found for system:"+ system.name)
        @alert_actions = @alert_actions + get_web_alertactions(aa_conf, system.name, system.goals, system.hostname, auto_grader_hostname)
      end
      system.module_selections.each do |module_selection|
        if module_selection.goals != []
          Print.info("Module goals found for module:" + module_selection.module_path)
          Print.info("Module goals: " + module_selection.goals.to_s)
          @alert_actions = @alert_actions + get_web_alertactions(aa_conf, module_selection.module_path_end, module_selection.goals, system.hostname, auto_grader_hostname)
        end
      end
    end
  end

  def get_web_alertactions(aa_conf, name, goals, hostname, auto_grader_hostname)
    Print.info("**** get_web_alertactions() ****")
    alert_actions = []

    # Validate whether there are an equal number of goals and goal_flags + warn / error here if not...
    if goals != [] and @goal_flags != []
      goals_qty = goals.size
      flags_qty = @goal_flags.size
        Print.err "Goals qty: #{goals_qty}  vs   Flags qty: #{flags_qty}"

        # Iterate over the goals
        goals.each_with_index do |goal, i|
          Print.info("goal number" + i.to_s )

          alert_actions << {'alert_name' => Rules.get_ea_rulename(hostname, name, goal, i),
                             'action_type' => 'WebAction',
                             'hacktivity_url' => aa_conf['hacktivity_url'],
                             'request_type' => 'POST',
                             'data' => "vm_name=" + auto_grader_hostname + "&amp;flag=" + @goal_flags.pop # TODO: test if this works
                             # 'data' => goal_flags[i] # TODO: Update this to the correct format
          }
        end
    else
      Print.err("goals: " + goals)
      Print.err("goal_flags: " + @goal_flags)
    end
    alert_actions
  end

  def generate_xml_config
    Print.info 'Creating AlertActioner xml config...'
    ns = {
        'xmlns' => "http://www.github/cliffe/SecGen/alertactioner_config",
        'xmlns:xsi' => "http://www.w3.org/2001/XMLSchema-instance",
        'xsi:schemaLocation' => "http://www.github/cliffe/SecGen/alertactioner_config"
    }
    builder = Nokogiri::XML::Builder.new do |xml|
      xml.alertactioner(ns) {
        xml.comment 'This AlertActioner configuration file was generated by SecGen'
        xml.comment "#{@time}"
        xml.comment "Based on a fulfilment of scenario: #{@scenario}"

        @alert_actions.each {|alert_action|
          xml.alertaction {
            xml.alert_name alert_action['alert_name']
            case alert_action['action_type']
            when 'WebAction'
              xml.WebAction {
                xml.hacktivity_url alert_action['hacktivity_url']
                xml.request_type alert_action['request_type']
                xml.data alert_action['data']
              }
            when 'MessageAction'
              xml.MessageAction {
                xml.host alert_action['host']
                xml.sender alert_action['sender']
                xml.password alert_action['password']
                xml.recipient alert_action['recipient']
                xml.message_header alert_action['message_header']
                xml.message_subtext alert_action['message_subtext']
              }
            else
              # TODO: Add more actions
              Print.err "XmlAlertActionConfigGenerator: Invalid alertaction type - #{alert_action['action_type']}"
            end
          }
        }
      }
    end
    builder.to_xml
  end

  def get_auto_grader_hostname
    ag_hostname = ''
    @systems.each do |system|
      if system.hostname.include? 'grading'
        ag_hostname = system.hostname
      end
    end
    ag_hostname
  end
end
