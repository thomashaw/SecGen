# Leaks the flag into the service account's home directory, where a shell
# obtained through the debug agent can read it.
class java_jdwp_exposed::config {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $user              = $secgen_parameters['unix_username'][0]
  $user_home         = "/home/${user}"
  $leaked_filenames  = $secgen_parameters['leaked_filenames']
  $strings_to_leak   = $secgen_parameters['strings_to_leak']

  ::secgen_functions::leak_files { 'java_jdwp_exposed-flag':
    storage_directory => $user_home,
    leaked_filenames  => $leaked_filenames,
    strings_to_leak   => $strings_to_leak,
    owner             => $user,
    mode              => '0600',
    leaked_from       => 'java_jdwp_exposed',
    require           => User[$user],
  }
}
