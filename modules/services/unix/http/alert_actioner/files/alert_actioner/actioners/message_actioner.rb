require_relative 'command_actioner'

class MessageActioner < CommandActioner

  def initialize(config_filename, alertaction_index, alert_name, host, username, password, message)
    super(config_filename, alertaction_index, alert_name, host, username, password, "bash -s < ./#{msg_script}" message)
  end

  # TODO: Override me in superclass to print actioner type + all parameters??
  def to_s
    "MessageActioner:\n TODO: to_s parameters"
  end

end

MessageActioner.new('config_filename', 1, 'example-alert', '127.0.0.1', 'tg5196571lap', ['-u critical', "'Well done'", "'here is some subtext'" '"']).action_alert


# TODO: Convert the below into the automatic creation of a_script.sh (based on the message etc) and then the running of it via sshpass/ssh
# TODO: Update the parameters to include the username of the account we want to inform, and their password
# TODO: Play around with this stuff on the VMs. There could be an easier way to graphically alert all users on KDE/XFCE and we can forget about gnome.
#
# [root@localhost thomashaw]# sshpass -p tg5196571lap ssh -T -oStrictHostKeyChecking=no thomashaw@127.0.0.1 bash -s < ./a_script.sh
# touch: cannot touch '/home/thomashaw/new_test': Permission denied
# [root@localhost thomashaw]# cat a_script.sh
# #!/bin/bash
# DISPLAY=:0 /usr/bin/notify-send -u critical 'Well done' 'here is some subtext'