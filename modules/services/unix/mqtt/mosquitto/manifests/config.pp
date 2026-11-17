# Writes mosquitto.conf and the password file holding the one configured user.
class mosquitto::config {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $port           = $secgen_parameters['port'][0]
  $listen_address = $secgen_parameters['listen_address'][0]
  $username       = $secgen_parameters['username'][0]
  $password       = $secgen_parameters['password'][0]

  $passwd_file = '/etc/mosquitto/passwd'

  # Owns the whole file: conf.d is still included at the end for other modules
  file { '/etc/mosquitto/mosquitto.conf':
    ensure  => file,
    owner   => 'root',
    group   => 'root',
    mode    => '0644',
    content => template('mosquitto/mosquitto.conf.erb'),
    require => Class['mosquitto::install'],
  }

  file { '/etc/mosquitto/conf.d':
    ensure  => directory,
    owner   => 'root',
    group   => 'root',
    mode    => '0755',
    require => Class['mosquitto::install'],
  }

  # -c recreates the file, so the exec is idempotent. The password goes in through
  # the environment rather than Puppet's command string, and is not logged.
  exec { 'create mosquitto password file':
    command     => "/usr/bin/mosquitto_passwd -b -c ${passwd_file} \"\$MQ_USER\" \"\$MQ_PASS\"",
    provider    => shell,
    environment => ["MQ_USER=${username}", "MQ_PASS=${password}"],
    logoutput   => false,
    require     => Class['mosquitto::install'],
  }

  # The broker runs as the mosquitto user and must be able to read it
  file { $passwd_file:
    ensure  => file,
    owner   => 'mosquitto',
    group   => 'mosquitto',
    mode    => '0640',
    require => Exec['create mosquitto password file'],
  }
}
