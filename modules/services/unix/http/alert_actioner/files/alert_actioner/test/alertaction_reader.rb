require 'nokogiri'
require 'digest'

# TODO: Fix the pathing etc after we're done.
require_relative '../lib/aa_constants'
require_relative '../actioners/alert_actioner'
require_relative '../actioners/web_actioner'
require_relative 'xml_reader.rb'
require_relative '../../../../../../../../lib/helpers/constants.rb'


# TODO: Might want to keep this reader in the module rather than in the 'lib/readers' folder, but we'll see.
class AlertActionReader < XMLReader
  include Logging

  # TODO: Fix the pathing + remove this after we're done.
  ALERTER_DIRECTORY = SERVICES_DIR + 'unix/http/alert_actioner/files/alert_actioner/'
  CONFIG_DIRECTORY = ALERTER_DIRECTORY + 'config/'
  # AA_CONFIG_SCHEMA = ALERTER_DIRECTORY + 'lib/alertactioner_config_schema.xsd'
  AA_CONFIG_SCHEMA = ALERTER_DIRECTORY + 'test/alertactioner_config_schema.xsd'

  # uses nokogiri to extract all system information from alertaction config.xml files
  # @return [Array] Array containing AlertActioner objects
  def self.get_alertactioners(conf_path)
    alert_actioners = []
    config_filename = conf_path.split('/')[-1]
    # Parse and validate the schema
    doc = parse_doc(conf_path, AA_CONFIG_SCHEMA, 'alertaction_config')

    doc.xpath('//alertaction').each_with_index do |alertaction_node, alertaction_index|

      Print.verbose "alertaction: #{alertaction_node}"
      alert_name = alertaction_node.at_xpath('alert_name').text

      # for each action type:
      alertaction_node.xpath('WebAction | CommandAction | MessageAction | VDIAction | IRCAction').each do |action_node|
        type = action_node.name

        case type
        when 'WebAction'
          target = action_node.xpath('target').text
          request_type = action_node.xpath('request_type').text
          data = action_node.xpath('data').text

          web_actioner = WebActioner.new(config_filename, alertaction_index, alert_name, target, request_type, data)
          Print.info "Created #{web_actioner.to_s}"
          alert_actioners << web_actioner
        when 'CommandAction'
          # todo
        when 'MessageAction'
          # todo
        when 'VDIAction'
          # todo
        when 'IRCAction'
          # todo
        else
          Print.err("Invalid actioner type.", AlertRouter.logs)
          exit(1)
        end
      end
    end
    return alert_actioners
  end
end