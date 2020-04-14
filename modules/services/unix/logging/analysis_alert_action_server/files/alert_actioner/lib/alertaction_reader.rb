require 'nokogiri'
require 'digest'

require_relative './logging'
require_relative 'xml_reader'
require_relative '../actioners/web_actioner'
require_relative '../actioners/message_actioner'

class AlertActionReader < XMLReader
  include Logging

  # uses nokogiri to extract all system information from alertaction config.xml files
  # @return [Array] Array containing AlertActioner objects
  def self.get_alertactioners(conf_path)
    alert_actioners = []
    config_filename = conf_path.split('/')[-1]
    # Parse and validate the schema
    doc = parse_doc(conf_path, AA_CONFIG_SCHEMA, 'alertaction_config')

    doc.xpath('//alertaction').each_with_index do |alertaction_node, alertaction_index|

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
          Print.info("Created #{web_actioner.to_s}", Logging.logger)
          alert_actioners << web_actioner
        when 'CommandAction'
          # todo
        when 'MessageAction'
          host = action_node.xpath('host').text
          sender = action_node.xpath('sender').text
          password = action_node.xpath('password').text
          recipient = action_node.xpath('recipient').text
          message_header = action_node.xpath('message_header').text
          message_subtext = action_node.xpath('message_subtext').text
          message_actioner = MessageActioner.new(config_filename, alertaction_index, alert_name, host, sender, password, recipient, message_header, message_subtext)
          Print.info  "Created #{message_actioner.to_s}", Logging.logger
          alert_actioners << message_actioner
        when 'VDIAction'
          # todo
        when 'IRCAction'
          # todo
        else
          Print.err("Invalid actioner type.", Logging.logger)
          exit(1)
        end
      end
    end
    return alert_actioners
  end
end