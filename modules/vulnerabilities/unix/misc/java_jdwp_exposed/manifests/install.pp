# Creates the unprivileged service account and compiles the bundled Java
# service with the JDK installed by the required java utility.
class java_jdwp_exposed::install {
  Exec { path => ['/bin', '/usr/bin', '/usr/local/bin', '/sbin', '/usr/sbin'] }

  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $user              = $secgen_parameters['unix_username'][0]
  $user_home         = "/home/${user}"
  $app_dir           = '/opt/jdwp_service'

  # Service account: no sudo rights, so a shell obtained through the debug
  # agent is unprivileged.
  user { $user:
    ensure     => present,
    home       => $user_home,
    managehome => true,
    shell      => '/bin/bash',
    system     => false,
  }

  file { $app_dir:
    ensure => directory,
    owner  => 'root',
    group  => 'root',
    mode   => '0755',
  }
  -> file { "${app_dir}/JdwpService.java":
    ensure => file,
    owner  => 'root',
    group  => 'root',
    mode   => '0644',
    source => 'puppet:///modules/java_jdwp_exposed/JdwpService.java',
  }
  # javac ships with the openjdk JDK pulled in by the java utility (a <requires>
  # dependency, so it has already provisioned by this point).
  -> exec { 'compile-jdwp-service':
    command => '/usr/bin/javac JdwpService.java',
    cwd     => $app_dir,
    creates => "${app_dir}/JdwpService.class",
  }
}
