# Class: java_jmx_unauth::service
# Deploy the launcher script that starts the JVM with a remote, unauthenticated
# JMX agent on the configured port, and run it as a systemd unit.
class java_jmx_unauth::service {
  $modulename = 'java_jmx_unauth'
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $port = $secgen_parameters['port'][0]
  $user = $secgen_parameters['unix_username'][0]

  $app_dir     = '/opt/java_jmx_unauth'
  $classes_dir = "${app_dir}/classes"
  $launcher    = "${app_dir}/jmx-launcher.sh"

  file { $launcher:
    ensure  => file,
    content => template("${modulename}/jmx-launcher.sh.erb"),
    mode    => '0755',
  }

  file { '/etc/systemd/system/java_jmx_unauth.service':
    ensure  => file,
    content => template("${modulename}/java_jmx_unauth.service.erb"),
    mode    => '0644',
    require => File[$launcher],
  }
  ~> exec { 'java_jmx_unauth-daemon-reload':
    command     => 'systemctl daemon-reload',
    path        => [ '/bin', '/usr/bin', '/sbin', '/usr/sbin' ],
    refreshonly => true,
  }
  -> service { 'java_jmx_unauth':
    ensure => running,
    enable => true,
  }
}
