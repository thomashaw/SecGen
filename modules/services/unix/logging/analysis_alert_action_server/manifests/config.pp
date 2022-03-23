class analysis_alert_action_server::config (
  $install_path = '/opt/alert_actioner',
  $db_username = 'aaa_admin',
  $db_password,
) {

  # Configure the DB
  ## Ensure the DB service runs on reboot
  service { 'postgresql':
    enable => true,
    ensure => 'running',
  }

  ## Moving across the shell script which setups the database
  file { "$install_path/lib/postgresql_setup.sh":
    owner  => root,
    group  => root,
    mode   => '0700',
    ensure => file,
    source => 'puppet:///modules/analysis_alert_action_server/postgresql_setup.sh',
    notify => Exec['setup_postgresql'],
  }

  ## Run DB setup script
  exec { 'setup_postgresql':
    cwd     => $install_path,
    command => "sudo ./lib/postgresql_setup.sh $db_username $db_password",
    path    => [ '/bin/', '/sbin/' , '/usr/bin/', '/usr/sbin/' ],
  }

  # Configure alert_router
  ## Add the alert_router service file
  file { '/etc/systemd/system/alert_router.service':
    ensure => present,
    source => 'puppet:///modules/analysis_alert_action_server/alert_router.service',
    notify => Service['alert_router']
  }

  ## Enable it on restarts
  service { 'alert_router':
    enable => true,
    ensure => 'running',
    require => [File['/etc/systemd/system/alert_router.service'], Exec['setup_postgresql']],
  }

}