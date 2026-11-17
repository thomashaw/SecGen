class erlang_cookie_rce::config {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $username         = $secgen_parameters['unix_username'][0]
  $home             = "/home/${username}"
  $cookie           = $secgen_parameters['cookie'][0]
  $leaked_filenames = $secgen_parameters['leaked_filenames']
  $strings_to_leak  = $secgen_parameters['strings_to_leak']

  # The guessable distribution cookie lives in the service user's home, mode 0400
  # (Erlang refuses a cookie file that is group/other readable). No trailing
  # newline: the file content is the cookie exactly, so it matches the exploit's
  # COOKIE value byte for byte.
  file { "${home}/.erlang.cookie":
    ensure  => file,
    owner   => $username,
    group   => $username,
    mode    => '0400',
    content => $cookie,
    require => User[$username],
  }

  # The flag lands in the service user's home, readable only by that user, so it
  # is the payoff for getting a shell as the node's account.
  ::secgen_functions::leak_files { 'erlang_cookie_rce-flag':
    storage_directory => $home,
    leaked_filenames  => $leaked_filenames,
    strings_to_leak   => $strings_to_leak,
    owner             => $username,
    mode              => '0600',
    leaked_from       => 'erlang_cookie_rce',
    require           => User[$username],
  }
}
