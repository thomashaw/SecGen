class suid_root_man::config {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $leaked_filenames = $secgen_parameters['leaked_filenames']
  $strings_to_leak = $secgen_parameters['strings_to_leak']

  file { '/usr/bin/man':
    mode => "4755",
    owner => "root",
  }

  ::secgen_functions::leak_files { 'setuid-root-man-flag-leak':
    storage_directory => '/root',
    leaked_filenames  => $leaked_filenames,
    strings_to_leak   => $strings_to_leak,
    owner             => 'root',
    mode              => '0600',
    leaked_from       => 'setuid-root-man-flag-leak',
  }

}
