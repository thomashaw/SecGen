class webwolf::service{

  exec { 'systemctl-daemon-reload':
    command => '/bin/systemctl daemon-reload',
    path    => '/bin:/usr/bin',
  }->
  service { 'webwolf':
    ensure   => running,
    enable   => true,
    provider => 'systemd',
  }
}
