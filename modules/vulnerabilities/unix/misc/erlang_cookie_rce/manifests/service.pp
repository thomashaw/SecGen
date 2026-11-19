class erlang_cookie_rce::service {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $username  = $secgen_parameters['unix_username'][0]
  $home      = "/home/${username}"
  $dist_port = pick($secgen_parameters['erlang_dist_port'], ['25672'])[0]

  # Fixed short node name. The exploit connects straight to the distribution
  # listener, so the attacker never needs to resolve this name; it only has to
  # register locally with epmd, which -sname does.
  $node_name = 'secgen'

  # A distributed Erlang node under systemd, running as the unprivileged service
  # user. -sname enables distribution and registers with epmd (4369). The
  # distribution listener is pinned to $dist_port on every interface (Erlang
  # binds the listener to 0.0.0.0 by default). The cookie is read from
  # ${home}/.erlang.cookie. -noshell -noinput keeps the runtime alive with no
  # terminal, and the standard kernel 'rex' rpc server is what the exploit drives.
  file { '/etc/systemd/system/erlang-cookie-rce.service':
    ensure  => file,
    owner   => 'root',
    group   => 'root',
    mode    => '0644',
    content => @("UNIT"),
      [Unit]
      Description=Erlang distributed node with a guessable cookie (erlang_cookie_rce)
      After=network-online.target
      Wants=network-online.target

      [Service]
      Type=simple
      User=${username}
      Group=${username}
      WorkingDirectory=${home}
      Environment=HOME=${home}
      ExecStart=/usr/bin/erl -sname ${node_name} -kernel inet_dist_listen_min ${dist_port} inet_dist_listen_max ${dist_port} -noshell -noinput
      Restart=on-failure
      RestartSec=3
      StandardOutput=append:/var/log/erlang_cookie_rce/node.log
      StandardError=append:/var/log/erlang_cookie_rce/node.log

      [Install]
      WantedBy=multi-user.target
      | UNIT
    notify  => Exec['erlang-cookie-rce-daemon-reload'],
    require => [
      Package['erlang-nox'],
      File["${home}/.erlang.cookie"],
      File['/var/log/erlang_cookie_rce'],
      User[$username],
    ],
  }

  exec { 'erlang-cookie-rce-daemon-reload':
    command     => '/bin/systemctl daemon-reload',
    path        => ['/usr/bin', '/bin', '/usr/sbin', '/sbin'],
    refreshonly => true,
  }

  service { 'erlang-cookie-rce':
    ensure    => running,
    enable    => true,
    provider  => 'systemd',
    subscribe => File['/etc/systemd/system/erlang-cookie-rce.service'],
    require   => Exec['erlang-cookie-rce-daemon-reload'],
  }
}
