require 'nokogiri'

require_relative '../helpers/rules'

# Convert systems objects into alertactioner xml configuration
class XmlAlertActionConfigGenerator

  # @param [Object] systems the list of systems
  # @param [Object] scenario the scenario file used to generate
  # @param [Object] time the current time as a string
  # @param [Array[Hash]] the alert_actioner configuration settings (list of aa_conf JSON hashes)
  def initialize(systems, scenario, time, aa_confs)
    @systems = systems
    @scenario = scenario
    @time = time
    @aa_confs = aa_confs
    @alert_actions = []
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
                               'username' => aa_conf['username'],
                               'password' => aa_conf['password'],
                               'message_header' => aa_conf['message_header'],
                               'message_subtext' => aa_conf['message_subtext']
            }
          end
        end
      end
    end
  end

  def all_goal_flags_to_hacktivity(aa_conf)
    @systems.each do |system|
      if system.goals != []
        @alert_actions = @alert_actions + get_web_alertactions(aa_conf, system.name, system.goals, $datastore['goal_flags'], system.hostname)
      end
      system.module_selections.each do |module_selection|
        @alert_actions = @alert_actions + get_web_alertactions(aa_conf, module_selection.module_path_end, module_selection.goals, module_selection.received_inputs['goal_flags'], system.hostname)
      end
    end
  end

  def get_web_alertactions(aa_conf, name, goals, goal_flags, hostname)
    alert_actions = []

    # Validate whether there are an equal number of goals and goal_flags + warn / error here if not...
    if goals != [] or goal_flags != nil
      goals_qty = goals.size
      flags_qty = goal_flags.size
      unless goals_qty == flags_qty
        Print.err "AlertActioner: ERROR for mapping_type: #{aa_conf['mapping_type']}"
        Print.err "Unequal number of goals and goal_flags for: #{name}"
        Print.err "Goals qty: #{goals_qty}  vs   Flags qty: #{flags_qty}"
        exit(1)
      end

      if goals != [] and goal_flags != nil
        # Iterate over the goals
        goals.each_with_index do |goal, i|
          alert_actions << {'alert_name' => Rules.get_ea_rulename(hostname, name, goal, i),
                             'action_type' => 'WebAction',
                             'target' => aa_conf['target'],
                             'request_type' => 'POST',
                             'data' => goal_flags[i]
          }
        end
      end
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
                xml.target alert_action['target']
                xml.request_type alert_action['request_type']
                xml.data alert_action['data']
              }
            when 'MessageAction'
              xml.MessageAction {
                xml.host alert_action['host']
                xml.username alert_action['username']
                xml.password alert_action['password']
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
end
