require_relative 'command_actioner'

class MessageActioner < CommandActioner

  attr_accessor :message_header
  attr_accessor :message_subtext
  attr_accessor :recipient

  def initialize(config_filename, alertaction_index, alert_name, host, sender, password, recipient, message_header, message_subtext)
    super(config_filename, alertaction_index, alert_name, host, sender, password)
    self.message_header = message_header
    self.message_subtext = message_subtext
    self.recipient = recipient
  end

  # Return [Array] of command strings
  def command_strings
    ["DISPLAY=:0 /usr/bin/notify-send '#{self.message_header}' '#{self.message_subtext}' --icon=dialog-information",
     "/usr/bin/wall #{self.username == 'root' ? '-n ' : ''}'#{self.message_header}' '#{self.message_subtext}'",  # wall -n requires root
     "/bin/echo '#{self.message_subtext}' | /usr/bin/mail -s '#{self.message_header}' #{self.recipient}"]
    # TODO: Test mail command
  end


  # TODO: Override me in superclass to print actioner type + all parameters??
  def to_s
    "#{self.class}:\n  Message Header: #{self.message_header}\n  Message Subtext: #{self.message_subtext}"
  end

end
