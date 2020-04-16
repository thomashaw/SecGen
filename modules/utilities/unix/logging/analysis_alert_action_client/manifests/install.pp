class analysis_alert_action_client::install {
  package{ ['mailutils', 'libnotify-bin']:
    ensure => installed,
  }

  case $::lsbdistcodename {
    'stretch': {
      package { 'mail-notification': ensure => installed }
      # TODO: Add config stuff for mail-notification on debian desktop
    }
    'kali-rolling': {
      package { 'xfce-mailwatch-plugin': ensure => installed }
      # TODO: Add config stuff for mailwatch on kali desktop
    }
  }

}
