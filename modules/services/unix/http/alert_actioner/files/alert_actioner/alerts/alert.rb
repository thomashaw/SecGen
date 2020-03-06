class Alert

  attr_accessor :rule_name
  attr_accessor :alert_body

  def initialize(rule_name, alert_body)
    self.rule_name = rule_name
    self.alert_body = alert_body
  end

end