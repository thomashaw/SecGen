# Class: spring_cloud_gateway_rce::install
# Builds the vulnerable Spring Cloud Gateway 3.1.0 application from the bundled
# Maven project and runs it as a dedicated non-root user.
class spring_cloud_gateway_rce::install {
  Exec { path => ['/bin', '/usr/bin', '/usr/local/bin', '/sbin', '/usr/sbin'] }

  $modulename = 'spring_cloud_gateway_rce'
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $user = $secgen_parameters['unix_username'][0]
  $user_home = "/home/${user}"
  $app_dir = '/opt/scg'

  # maven pulls in default-jdk-headless on bookworm; keep it explicit so the
  # project compiles and the fat jar runs on the same JDK.
  ensure_packages(['maven', 'default-jdk-headless'])

  user { $user:
    ensure     => present,
    home       => $user_home,
    managehome => true,
  }

  # Maven project layout for the demo gateway app.
  file { [$app_dir,
          "${app_dir}/src",
          "${app_dir}/src/main",
          "${app_dir}/src/main/java",
          "${app_dir}/src/main/java/com",
          "${app_dir}/src/main/java/com/example",
          "${app_dir}/src/main/java/com/example/scg"]:
    ensure => directory,
  }

  file { "${app_dir}/pom.xml":
    ensure  => file,
    source  => "puppet:///modules/${modulename}/pom.xml",
    require => File[$app_dir],
  }

  file { "${app_dir}/src/main/java/com/example/scg/DemoApplication.java":
    ensure  => file,
    source  => "puppet:///modules/${modulename}/src/main/java/com/example/scg/DemoApplication.java",
    require => File["${app_dir}/src/main/java/com/example/scg"],
  }

  # Resolve dependencies from Maven Central and repackage the Spring Boot fat jar.
  exec { 'build-scg':
    command     => 'mvn -q -B -DskipTests package',
    cwd         => $app_dir,
    creates     => "${app_dir}/target/scg-gateway.jar",
    environment => ['HOME=/root'],
    timeout     => 1800,
    logoutput   => true,
    require     => [File["${app_dir}/pom.xml"],
                    File["${app_dir}/src/main/java/com/example/scg/DemoApplication.java"],
                    Package['maven'],
                    Package['default-jdk-headless']],
  }

  # Hand the built app to the service user so the process can read it.
  -> exec { 'chown-scg':
    command => "chown -R ${user} ${app_dir}",
    require => User[$user],
  }
}
