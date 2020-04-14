class analysis_alert_action_client::install {
  package{ ['mailutils', 'libnotify-bin']:
    ensure => installed,
  }
}