require 'net/http'
require 'uri'
require 'open3'
require_relative 'alert_actioner'

class CommandActioner < AlertActioner

  attr_accessor :host
  attr_accessor :username
  attr_accessor :password
  attr_accessor :commands


  def initialize(config_filename, alertaction_index, alert_name, host, username, password, commands=[])
    super(config_filename, alertaction_index, alert_name)
    self.host = host
    self.username = username
    self.password = password
    self.commands = commands
  end

  def perform_action
    # Create config/commands directory
    FileUtils.mkdir_p COMMANDS_DIRECTORY
    commands_sh_path = COMMANDS_DIRECTORY + "#{self.alertactioner_name}.sh"
    template_path = TEMPLATES_DIRECTORY + 'command.sh.erb'

    # We need to populate an array of commands + their parameters
    @shell_commands = command_strings
    template_based_file_write(template_path, commands_sh_path)

    ssh_command = "sshpass -p #{self.password} ssh -oStrictHostKeyChecking=no #{self.username}@#{self.host} /bin/bash -s < #{commands_sh_path}"

    Print.info "  Command strings:\n    #{@shell_commands.join("\n    ")}"

    stdout, stderr, status = Open3.capture3(ssh_command)
    Print.info "  stdout: #{stdout}", logger
    Print.info "  stderr: #{stderr}", logger if stderr != ''
    Print.info "  STATUS: #{status}", logger

    unless status.exitstatus == 0
      Print.info "  ERROR: non-zero exit code.", logger
      exit(1)
    end
  end

  def command_strings
    self.commands
    # For more specific command-actioners, override me.
  end

  # TODO: Override me in superclass to print actioner type + all parameters??
  def to_s
    "#{self.class}:\n  Host: #{self.host}\n  Command: #{self.command}\n  Parameters: #{self.parameters.join(',')}"
  end

end