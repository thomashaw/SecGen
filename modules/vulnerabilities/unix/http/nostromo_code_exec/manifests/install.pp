class nostromo_code_exec::install {
  Exec { path => [ '/bin/', '/sbin/' , '/usr/bin/', '/usr/sbin/' ] }
  #$secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $user = 'nostromousr'
  $user_home = "/home/${user}"

  # Install dependancies - make, gcc libssl-dev
  ensure_packages(['make','gcc','libssl-dev'])

  user { "${user}":
    ensure => present,
    home   => "${user_home}",
    require    => Package['libssl-dev'],
  } ->
  group {"${user}":
    ensure => present,
  } ->
  # distccd home directory permissions
  file { "${user_home}":
    ensure => directory,
    owner => "${user}",
    mode  =>  '0750',
  } ->

  # TODO: install into /opt/ rather than user home
  # Move tar ball to /home/nostromo/
  file { "${user_home}/nostromo_1_9_6.tar.gz":
    source  => 'puppet:///modules/nostromo_code_exec/nostromo_1_9_6.tar.gz',
    owner   => $user,
    mode    => '0777',
  } ->

  # Extract the tar ball
  exec { 'mellow-file':
    cwd     => "${user_home}/",
    command => 'tar -xzvf nostromo_1_9_6.tar.gz',
    creates => "${user_home}/nostromo-1.9.6/",
  } ->

  # Make the application
  exec { 'make-nostromo':
    cwd     => "${user_home}/nostromo-1.9.6/",
    command => 'sudo make',
  } ->

  # Install the application
  exec { 'make-nostromo-install':
    cwd     => "${user_home}/nostromo-1.9.6/",
    command => 'sudo make install',
  }
}
