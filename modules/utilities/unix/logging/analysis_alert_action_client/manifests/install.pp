class analysis_alert_action_client::install {
  package{ ['mailutils', 'libnotify-bin']:
    ensure => installed,
  }

  # TODO: Update this - none of this works! Try the $MAILCHECK env variable with /bin/bash...
  # case $::lsbdistcodename {
    # 'stretch': {
    #   package { 'mail-notification': ensure => installed }
      # TODO: Add config stuff for mail-notification on debian desktop
    # }
    # 'kali-rolling': {
    #   package { 'xfce4-mailwatch-plugin': ensure => installed }
      # TODO: Add config stuff for mailwatch on kali desktop
    # }
  # }
}
