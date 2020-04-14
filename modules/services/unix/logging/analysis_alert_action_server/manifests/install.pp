class analysis_alert_action_server::install (
  $install_path = '/opt/alert_actioner/'
) {

  package{ 'sshpass':
    ensure => installed,
  }

  file { $install_path:
    ensure  => directory,
    recurse => true,
    source  => 'puppet:///modules/analysis_alert_action_server/alert_actioner',
  }

}