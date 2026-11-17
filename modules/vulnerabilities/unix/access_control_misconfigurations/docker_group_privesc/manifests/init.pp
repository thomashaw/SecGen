class docker_group_privesc::init {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)

  $account = parsejson($secgen_parameters['accounts'][0])
  $username = $account['username']

  $strings_to_leak  = $secgen_parameters['strings_to_leak']
  $leaked_filenames = $secgen_parameters['leaked_filenames']

  # Create the account and add it to the docker group. The docker group is
  # created by the required Docker Installation utility, which is provisioned
  # first, so it exists by the time this account is created.
  #
  # No strings are leaked into this user's home: the flag lives in /root and is
  # only readable once the docker-group member escalates to root (that is the
  # point of the exercise).
  ::parameterised_accounts::account { "docker_group_privesc_$username":
    username         => $username,
    password         => $account['password'],
    groups           => ['docker'],
    super_user       => str2bool($account['super_user']),
    strings_to_leak  => [],
    leaked_filenames => [],
  }

  # Leak the flag into /root, owned by root and readable only by root (0600).
  ::secgen_functions::leak_files { 'docker_group_privesc-root-leak':
    storage_directory => '/root/',
    leaked_filenames  => $leaked_filenames,
    strings_to_leak   => $strings_to_leak,
    owner             => 'root',
    group             => 'root',
    mode              => '0600',
    leaked_from       => 'docker_group_privesc',
  }
}
