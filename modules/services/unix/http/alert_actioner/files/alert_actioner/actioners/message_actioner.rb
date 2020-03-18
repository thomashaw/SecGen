require 'net/http'
require 'uri'
require_relative 'alert_actioner'

class MessageActioner < CommandActioner

  def initialize(config_filename, alertaction_index, alert_name, host, root_password, message)
    super(config_filename, alertaction_index, alert_name, host, root_password, 'wall', message)
  end

  # TODO: Override me in superclass to print actioner type + all parameters??
  def to_s
    "MessageActioner:\n  Host: #{self.host}\n  Request Type: #{self.request_type}\n  Data: #{self.data.to_s}"
  end

end