require 'json'

require_relative 'alerts/alert'
require_relative 'lib/print'
require_relative 'lib/xml_reader'

class AlertRouter

  ALERTER_DIRECTORY = '/opt/alert_actioner/'

  attr_accessor :alerts

  def initialize
    self.alerts = []
  end

  def parameter_check
    if ARGV.length != 1
      Print.err 'ERROR: Incorrect number of parameters'
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
      self.alerts << Alert.new(rule_name, alert_body)
    rescue JSON::ParserError, StandardError => e
      Print.err e.to_s
      # log error to file
      exit(1)
    end
  end

  def load_config

    conf_filenames = Dir["#{ALERTER_DIRECTORY}*.xml"]

    # Open files in /config/
    # Validate against schema
  end

  def rule_comparison
    # code here
  end

  def action_alert

  end

  def usage
    Print.std "Usage:
   #{$0} <alert>"
    exit
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




