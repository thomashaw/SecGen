class routing_layer::install (
  $install_path = '/opt/alert_actioner/'
) {

  file { $install_path:
    ensure  => directory,
    recurse => true,
    source  => 'puppet:///modules/routing_layer/routing_layer',
  }

}