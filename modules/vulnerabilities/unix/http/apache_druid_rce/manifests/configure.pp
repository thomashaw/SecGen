# Class: apache_druid_rce::configure
# Configuration for apache druid
#
class apache_druid_rce::configure {
  $leaked_filenames = ['flagtest'] ##$secgen_parameters['leaked_filenames']
  $strings_to_leak = ['this is a list of strings that are secrets / flags','another secret'] ##$secgen_parameters['strings_to_leak']

  Exec { path => [ '/bin/', '/sbin/' , '/usr/bin/', '/usr/sbin/' ] }

  ::secgen_functions::leak_files { 'druid-flag-leak':
    storage_directory => '/root',
    leaked_filenames  => $leaked_filenames,
    strings_to_leak   => $strings_to_leak,
    owner             => 'root',
    mode              => '0750',
    leaked_from       => 'apache_druid_rce',
  }
}
