require 'net/http'
require 'uri'
require_relative 'alert_actioner'

class CommandActioner < AlertActioner

  attr_accessor :host
  attr_accessor :root_password
  attr_accessor :command
  attr_accessor :parameters


  def initialize(config_filename, alertaction_index, alert_name, host, root_password, command, parameters)
    super(config_filename, alertaction_index, alert_name)
    self.host = host
    self.root_password = root_password
    self.command = command
    self.parameters = parameters
  end

  def perform_action

    Print.info 'Running CommandActioner', logger


    # To test this, we can run the script locally and ssh into a user that i create on my own machine.

  end

  # TODO: Override me in superclass to print actioner type + all parameters??
  def to_s
    "CommandActioner:\n  Target: #{self.target}\n  Request Type: #{self.request_type}\n  Data: #{self.data.to_s}"
  end

end