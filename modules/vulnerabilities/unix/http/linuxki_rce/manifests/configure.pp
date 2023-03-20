# Class: linuxki_rce::configure
# LinuxKI configuration
#
class linuxki_rce::configure {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $leaked_filenames = $secgen_parameters['leaked_filenames']

  $strings_to_leak = $secgen_parameters['strings_to_leak']

  Exec { path => ['/bin', '/usr/bin', '/usr/local/bin', '/sbin', '/usr/sbin'] }

  ::secgen_functions::leak_files { 'linuxki-flag-leak':
    storage_directory => '/opt/linuxki/experimental/vis',
    leaked_filenames  => $leaked_filenames,
    strings_to_leak   => $strings_to_leak,
    owner             => 'www-data',
    mode              => '0750',
    leaked_from       => 'linuxki_rce',
  }
}
