class webgoat::service{

  exec { 'systemctl-daemon-reload':
    command => '/bin/systemctl daemon-reload',
    path    => '/bin:/usr/bin',
  }->
  service { 'webgoat':
    ensure   => running,
    enable   => true,
    provider => 'systemd',
  }
}
