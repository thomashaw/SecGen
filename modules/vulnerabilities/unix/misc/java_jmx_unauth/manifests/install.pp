# Class: java_jmx_unauth::install
# Create the service user, deploy and compile a tiny long-running Java process,
# and leak a flag into the service user's home. The JMX remote agent itself is
# enabled by the launcher/systemd unit in ::service.
class java_jmx_unauth::install {
  Exec { path => [ '/bin', '/usr/bin', '/usr/local/bin', '/sbin', '/usr/sbin' ] }
  $modulename = 'java_jmx_unauth'

  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $user             = $secgen_parameters['unix_username'][0]
  $leaked_filenames = $secgen_parameters['leaked_filenames']
  $strings_to_leak  = $secgen_parameters['strings_to_leak']
  $user_home        = "/home/${user}"

  $app_dir     = '/opt/java_jmx_unauth'
  $src_dir     = "${app_dir}/src/com/secgen"
  $classes_dir = "${app_dir}/classes"

  user { $user:
    ensure     => present,
    home       => $user_home,
    managehome => true,
  }

  file { [$app_dir, "${app_dir}/src", "${app_dir}/src/com", $src_dir, $classes_dir]:
    ensure => directory,
    mode   => '0755',
  }

  file { "${src_dir}/JmxApp.java":
    ensure  => file,
    source  => "puppet:///modules/${modulename}/JmxApp.java",
    mode    => '0644',
    require => File[$src_dir],
  }

  # The MLet exploit path needs javax.management.loading.MLet, which recent JDKs
  # deprecate for removal. Fail the build loudly if the installed JDK has dropped
  # it rather than shipping a box the exploit cannot work against.
  exec { 'java_jmx_unauth-verify-mlet':
    command  => 'javap javax.management.loading.MLet > /dev/null && touch /opt/java_jmx_unauth_mlet_ok',
    creates  => '/opt/java_jmx_unauth_mlet_ok',
    provider => shell,
    require  => File[$src_dir],
  }

  # Compile on the target with the JDK provided by the required java utility.
  exec { 'java_jmx_unauth-compile':
    command => "javac -d ${classes_dir} ${src_dir}/JmxApp.java",
    creates => "${classes_dir}/com/secgen/JmxApp.class",
    timeout => 300,
    require => [File["${src_dir}/JmxApp.java"], File[$classes_dir], Exec['java_jmx_unauth-verify-mlet']],
  }
  -> exec { 'java_jmx_unauth-chown':
    command => "chown -R ${user}:${user} ${app_dir}",
    require => User[$user],
  }

  ::secgen_functions::leak_files { 'java_jmx_unauth-flag':
    storage_directory => $user_home,
    leaked_filenames  => $leaked_filenames,
    strings_to_leak   => $strings_to_leak,
    owner             => $user,
    group             => $user,
    mode              => '0600',
    leaked_from       => 'java_jmx_unauth',
    require           => User[$user],
  }
}
