class alert_actioner::install (
  $install_path = '/opt/alert_actioner/'
) {

  file { $install_path:
    ensure  => directory,
    recurse => true,
    source  => 'puppet:///modules/alert_actioner/alert_actioner',
  }

}