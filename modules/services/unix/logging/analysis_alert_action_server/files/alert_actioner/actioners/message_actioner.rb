require_relative 'command_actioner'

class MessageActioner < CommandActioner

  attr_accessor :message_header
  attr_accessor :message_subtext

  def initialize(config_filename, alertaction_index, alert_name, host, username, password, message_header, message_subtext)
    super(config_filename, alertaction_index, alert_name, host, username, password)
    self.message_header = message_header
    self.message_subtext = message_subtext
  end

  # Return [Array] of command strings
  def command_strings
    ["DISPLAY=:0 /usr/bin/notify-send -u critical '#{self.message_header}' '#{self.message_subtext}'",
     "/usr/bin/wall #{username == 'root' ? '-n ' : ''}'#{self.message_header}' '#{self.message_subtext}'",  # wall -n requires root
     "/bin/echo '#{self.message_subtext}' | /usr/bin/mail -s '#{self.message.header}' #{self.username}"]
    # TODO: Test mail command
  end


  # TODO: Override me in superclass to print actioner type + all parameters??
  def to_s
    "#{self.class}:\n  Message Header: #{self.message_header}\n  Message Subtext: #{self.message_subtext}"
  end

end