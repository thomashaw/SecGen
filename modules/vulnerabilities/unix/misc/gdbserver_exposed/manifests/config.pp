class gdbserver_exposed::config {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $username         = $secgen_parameters['username'][0]
  $leaked_filenames = $secgen_parameters['leaked_filenames']
  $strings_to_leak  = $secgen_parameters['strings_to_leak']

  $user_home = "/home/${username}"

  # Unprivileged service account the debug stub runs as: the code execution an
  # attacker gains is this account's, not root's (matches <privilege>user_rwx).
  user { $username:
    ensure     => present,
    home       => $user_home,
    managehome => true,
    shell      => '/bin/bash',
  }

  # Flag lands in the service account's home, readable only by that account, so
  # it is reachable once the attacker has code execution as the account.
  ::secgen_functions::leak_files { 'gdbserver_exposed-file-leak':
    storage_directory => $user_home,
    leaked_filenames  => $leaked_filenames,
    strings_to_leak   => $strings_to_leak,
    owner             => $username,
    group             => $username,
    mode              => '0600',
    leaked_from       => 'gdbserver_exposed',
    require           => User[$username],
  }
}
