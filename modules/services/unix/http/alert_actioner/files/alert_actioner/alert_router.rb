require 'json'
require 'logger'

require_relative 'alerts/alert'
require_relative 'lib/print'
require_relative 'lib/xml_reader'

class AlertRouter

  ALERTER_DIRECTORY = '/opt/alert_actioner/'
  AA_CONFIG_SCHEMA = ALERTER_DIRECTORY + 'lib/alertactioner_config_schema.xsd'

  attr_accessor :alerts
  attr_accessor :config_docs
  attr_accessor :configs
  attr_accessor :logs

  def initialize
    self.alerts = []
    self.config_docs = []
    self.configs = []
    self.logs = Logger.new('alert-router.log')
    self.logs.debug "Elastalert log file created"
  end

  def parameter_check
    unless ARGV.length == 1
      Print.err 'ERROR: Incorrect number of parameters', self.logs
      usage
      exit(1)
    end
    unless ARGV[1].include? ':||:'
      Print.err 'ERROR: Does not include delimiter, :||:, between alert-name and JSON. Was this run via Elastalert?', self.logs
      usage
      exit(1)
    end
  end

  def read_alert
    begin
      parameter = ARGV[1]
      split_param = parameter.split(':||:')
      raise StandardError if split_param.length != 2
      rule_name = split_param[0]
      alert_body = JSON.parse(split_param[1])
      Print.info("Reading alert for rule: #{rule_name}", self.logs)
      self.alerts << Alert.new(rule_name, alert_body)
    rescue JSON::ParserError, StandardError => e
      Print.info(e.to_s, self.logs)
      exit(1)
    end
  end

  def load_config
    Print.info("Reading config from: #{ALERTER_DIRECTORY}", self.logs)
    conf_filenames = Dir["#{ALERTER_DIRECTORY}*.xml"]
    conf_filenames.each do |conf_filename|
      Print.info("Loading config: #{conf_filename}", self.logs)
      doc = XMLReader.parse_doc(conf_filename, AA_CONFIG_SCHEMA, 'alert_action')
      # TODO: Convert doc to config object
      self.config_docs << doc
    end
  end

  def rule_comparison
    # code here
  end

  def action_alert

  end

  def usage
    Print.std "Usage:
   #{$0} <alert>"
  end

  def run
    # move to initialize
    parameter_check   #[done]
    read_alert
    load_config
    # /move to initialize
    rule_comparison
    action_alert

    # alert = AlertFactory.get_alert(ARGV[1])
    # actioner = AlertActioner.get_actioner(alert.type)
    # AlertActioner.action(alert)
  end
end

AlertRouter.new.run