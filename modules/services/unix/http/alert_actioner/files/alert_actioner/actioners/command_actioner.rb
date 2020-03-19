require 'net/http'
require 'uri'
require 'open3'
require_relative 'alert_actioner'

class CommandActioner < AlertActioner

  attr_accessor :host
  attr_accessor :root_password
  attr_accessor :command
  attr_accessor :parameters


  def initialize(config_filename, alertaction_index, alert_name, host, username, password, command, parameters)
    super(config_filename, alertaction_index, alert_name)
    self.host = host
    self.username = username
    self.password = password
    self.command = command
    self.parameters = parameters
  end

  def perform_action

    ssh_command = "sshpass -p #{self.password} ssh -oStrictHostKeyChecking=no #{self.username}@#{self.host} "
    shell_command = self.command.strip + ' ' + self.parameters.join(' ')

    Print.info "  Command string: #{shell_command}"

    stdout, stderr, status = Open3.capture3(ssh_command + shell_command)
    Print.info "  stdout: #{stdout}", logger
    Print.info "  stderr: #{stderr}", logger if stderr != ''
    Print.info "  STATUS: #{status}", logger

    unless status.exitstatus == 0
      Print.info "  ERROR: non-zero exit code.", logger
      exit(1)
    end
  end

  # TODO: Override me in superclass to print actioner type + all parameters??
  def to_s
    "CommandActioner:\n  Host: #{self.host}\n  Command: #{self.command}\n  Parameters: #{self.parameters.join(',')}"
  end

end