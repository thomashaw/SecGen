# Class: apache_spark_rce::configure
# Configure apache spark and secgen
#
class apache_spark_rce::configure {
  $sparkconf='spark-defaults.conf'
  $leaked_filenames = ['flagtest'] ##$secgen_parameters['leaked_filenames']
  $strings_to_leak = ['this is a list of strings that are secrets / flags','another secret'] ##$secgen_parameters['strings_to_leak']

  Exec { path => [ '/bin/', '/sbin/' , '/usr/bin/', '/usr/sbin/' ] }

  # We set the acls flag in the config - This ensures its vulnerable
  file { "/usr/local/spark/conf/${sparkconf}":
    ensure => file,
    source => "puppet:///modules/apache_spark_rce/${sparkconf}"
  }

  ::secgen_functions::leak_files { 'spark-flag-leak':
    storage_directory => '/usr/local/spark/bin/',
    leaked_filenames  => $leaked_filenames,
    strings_to_leak   => $strings_to_leak,
    owner             => 'root',
    mode              => '0750',
    leaked_from       => 'apache_spark_rce',
  }
}
