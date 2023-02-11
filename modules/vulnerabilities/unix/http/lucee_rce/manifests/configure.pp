# Class: lucee_rce::configure
# Configuration for lucee with secgen
#
class lucee_rce::configure {
  $leaked_filenames = ['flagtest'] ##$secgen_parameters['leaked_filenames']
  $strings_to_leak = ['this is a list of strings that are secrets / flags','another secret'] ##$secgen_parameters['strings_to_leak']

  Exec { path => ['/bin', '/usr/bin', '/usr/local/bin', '/sbin', '/usr/sbin'] }

  ::secgen_functions::leak_files { 'lucee-flag-leak':
    storage_directory => '/',
    leaked_filenames  => $leaked_filenames,
    strings_to_leak   => $strings_to_leak,
    owner             => 'root',
    mode              => '0750',
    leaked_from       => 'lucee_rce',
  }
}
