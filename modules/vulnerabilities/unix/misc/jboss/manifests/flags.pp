class jboss::flags {
  # this is how secgen
##  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $leaked_filenames = ["flagsecret"] ##$secgen_parameters['leaked_filenames']
  $strings_to_leak = ["flag message"] ##$secgen_parameters['strings_to_leak']
  
  ::secgen_functions::leak_files { 'jboss-flag':
    storage_directory => "/opt/jboss-6.1.0.Final",
    leaked_filenames  => $leaked_filenames,
    strings_to_leak   => $strings_to_leak,
    leaked_from       => 'jboss',
  }
}
