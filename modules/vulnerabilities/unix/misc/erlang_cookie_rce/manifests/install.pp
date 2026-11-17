class erlang_cookie_rce::install {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $username = $secgen_parameters['unix_username'][0]
  $home     = "/home/${username}"

  # erlang-nox pulls erlang-base and the headless runtime, which provides
  # /usr/bin/erl and /usr/bin/epmd and the kernel 'rex' rpc server the exploit
  # talks to. No X11 is needed on a server VM.
  ensure_packages(['erlang-nox'])

  # The node runs as this unprivileged, non-sudoer service account, so a shell
  # from the exploit lands as a normal user (matches <privilege>user_rwx</privilege>).
  user { $username:
    ensure     => present,
    home       => $home,
    managehome => true,
    shell      => '/bin/bash',
  }

  group { $username:
    ensure => present,
  }

  file { '/var/log/erlang_cookie_rce':
    ensure => directory,
    owner  => 'root',
    group  => 'root',
    mode   => '0755',
  }
}
