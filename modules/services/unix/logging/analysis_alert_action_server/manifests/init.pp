class analysis_alert_action_server (
  $install_path = '/opt/alert_actioner',
  $db_username = 'aaa_admin',
  $db_password,
) {
  class { '::analysis_alert_action_server::install':
    install_path => $install_path,
    db_username => $db_username,
    db_password => $db_password

  }
  class { '::analysis_alert_action_server::config':
    install_path => $install_path,
    db_username => $db_username,
    db_password => $db_password
  }
}