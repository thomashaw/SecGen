class hackerbot_client::service{
  require hackerbot_client::config

  file { '/etc/systemd/system/hackerbot_client.service':
    ensure => 'link',
    target => '/opt/hackerbot_client/hackerbot_client.service',
  }->
  exec { 'hackerbot_client-systemd-reload':
    command     => 'systemctl daemon-reload',
    path        => [ '/usr/bin', '/bin', '/usr/sbin' ],
    refreshonly => true,
  }->
  service { 'hackerbot_client':
    ensure   => running,
    provider => systemd,
    enable   => true,
  }
}
