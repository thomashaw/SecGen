# TODO: remove after testing
require_relative '../lib/print'

class Alert
  attr_accessor :rule_name
  attr_accessor :alert_json

  def initialize(rule_name, alert_json)
    self.rule_name = rule_name
    self.alert_json = alert_json
    # self.alert_type =
    # self.alert_actions = [{action_type => 'msg', }]
    Print.info("Alert created.")
  end

end