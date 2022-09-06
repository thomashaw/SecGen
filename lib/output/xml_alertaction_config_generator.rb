require 'nokogiri'
require 'json'

require_relative '../helpers/rules'

# Convert systems objects into alertactioner xml configuration
class XmlAlertActionConfigGenerator

  # @param [Object] systems the list of systems
  # @param [Object] scenario the scenario file used to generate
  # @param [Object] time the current time as a string
  # @param [Array[Hash]] the alert_actioner configuration settings (list of aa_conf JSON hashes)
  def initialize(systems, scenario, time, aa_confs, options, goal_flags)
    @systems = systems
    @scenario = scenario
    @time = time
    @aa_confs = aa_confs
    @options = options
    @alert_actions = []
    @goal_flags = goal_flags.clone
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
        Print.info("**** Generating AlertActions for #{aa_conf['mapping_type']}")
        case aa_conf['mapping_type']
        when 'hacktivity_flags'
          generate_hacktivity_flags(aa_conf)
        when 'message_host'
          unless aa_conf['mappings']
            Print.err 'AlertActioner Config: message_host mapping type requires mappings'
          end
          generate_message_host(aa_conf)
        when 'message_host_all'
          generate_message_host_all(aa_conf)
        else
          Print.err("AlertActioner Config: Invalid mapping type #{aa_conf['mapping_type']}")
          exit(1)
        end
      else
        Print.err "AlertActioner Config: Either mapping_type or mapping required."
        exit(1)
      end
    end
  end

  def generate_message_host(aa_conf)
    # create objects from each mapping
    aa_conf['mappings'].each_with_index do |mapping, i|
      # Create an aa for each object

      aa_mapping = JSON.parse mapping
      # find the goal using the unique id

      # different behaviour for finding system goals and vulnerability goals
      if aa_mapping['unique_id'].include? 'scenariosystem' and aa_mapping['unique_id'].include? 'goal' # system level goal

        test = ''
      else
        # vulnerability goal

        # TODO
        message_host_aa = nil

        # Initialise with the default data (from aa_conf)
        @systems.each do |system|
          system.module_selections.each do |mod|
            if mod.unique_id == aa_mapping['unique_id']
              mod.goals.each_with_index do |goal, i|
                message_host_aa = get_message_host_aa(system.hostname, mod.module_path_end, aa_conf, goal, i)

                # Overwrite  where not nil
                message_host_aa['host'] = aa_mapping['host'] unless aa_mapping['host'] == ''
                message_host_aa['sender'] = aa_mapping['sender'] unless aa_mapping['sender'] == ''
                message_host_aa['password'] = aa_mapping['password'] unless aa_mapping['password'] == ''
                message_host_aa['recipient'] = aa_mapping['recipient'] unless aa_mapping['recipient'] == ''
                message_host_aa['message_header'] = aa_mapping['message_header'] unless aa_mapping['message_header'] == ''
                message_host_aa['message_subtext'] = aa_mapping['message_subtext'] unless aa_mapping['message_subtext'] == ''

                @alert_actions << message_host_aa

              end
            end
          end
        end
      end
    end
  end

  def generate_message_host_all(aa_conf)
    @systems.each do |system|
      # System goals
      if system.goals != []
        system.goals.each_with_index do |goal, i|
          @alert_actions << get_message_host_aa(system.hostname, system.name, aa_conf, goal, i)
        end
      end
      # Module goals
      system.module_selections.each do |module_selection|
        module_name = module_selection.module_path_end
        module_goals = module_selection.goals
        if module_goals != []
          module_selection.goals.each_with_index do |goal, i|
            @alert_actions << get_message_host_aa(system.hostname, module_name, aa_conf, goal, i)
          end
        end
      end
    end
  end

  def generate_hacktivity_flags(aa_conf)
    auto_grader_hostname = get_auto_grader_hostname

    Print.info("auto_grader_hostname: " + auto_grader_hostname)
    Print.info("systems.size: " + @systems.size.to_s)

    @systems.each do |system|
      Print.info("System goals: " + system.goals.to_s)
      if system.goals != []
        Print.info("System level goals found for system:" + system.name)
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
    alert_actions = []

    # Validate whether there are an equal number of goals and goal_flags + warn / error here if not...
    if goals != [] and @goal_flags != []
      goals_qty = goals.size
      flags_qty = @goal_flags.size
      Print.err "Goals qty: #{goals_qty}  vs   Flags qty: #{flags_qty}"

      # Iterate over the goals
      goals.each_with_index do |goal, i|
        Print.info("goal number" + i.to_s)

        alert_actions << {'alert_name' => Rules.get_ea_rulename(hostname, name, goal, i),
                          'action_type' => 'WebAction',
                          'target_host' => aa_conf['hacktivity_url'],
                          'request_type' => 'POST',
                          'data' => "vm_name=" + auto_grader_hostname + "&flag=" + @goal_flags.pop # TODO: update this to the format: Vm ID + Scenario name + auto_grader_hostname
                          # 'data' => goal_flags[i] # TODO: Update this to the correct format
        }
      end
    else
      Print.err("goals: " + goals.to_s)
      Print.err("goal_flags: " + @goal_flags.to_s)
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

        @alert_actions.each { |alert_action|
          xml.alertaction {
            xml.alert_name alert_action['alert_name']
            case alert_action['action_type']
            when 'WebAction'
              xml.WebAction {
                xml.target_host alert_action['target_host']
                xml.request_type alert_action['request_type']
                xml.data alert_action['data']
              }
            when 'MessageAction'
              xml.MessageAction {
                xml.target_host alert_action['host']
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

  # source_name: either module name or system name
  def get_message_host_aa(hostname, source_name, aa_conf, goal, i)
    {'alert_name' => Rules.get_ea_rulename(hostname, source_name, goal, i),
     'action_type' => 'MessageAction',
     'host' => aa_conf['host'],
     'sender' => aa_conf['sender'],
     'password' => aa_conf['password'],
     'recipient' => aa_conf['recipient'],
     'message_header' => aa_conf['message_header'],
     'message_subtext' => aa_conf['message_subtext']}
  end
end
