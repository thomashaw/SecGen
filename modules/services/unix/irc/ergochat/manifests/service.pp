class ergochat::service {
  file { '/etc/systemd/system/ergochat.service':
    ensure  => file,
    owner   => 'root',
    group   => 'root',
    mode    => '0644',
    content => "[Unit]
Description=Ergo IRC daemon
After=network.target

[Service]
Type=simple
User=ergochat
Group=ergochat
WorkingDirectory=/var/lib/ergochat
ExecStart=/usr/local/bin/ergo run --conf /etc/ergochat/ircd.yaml
ExecReload=/bin/kill -HUP \$MAINPID
Restart=on-failure
LimitNOFILE=1048576

[Install]
WantedBy=multi-user.target
",
    notify  => Exec['ergochat-systemd-reload'],
  }
  ->
  exec { 'ergochat-systemd-reload':
    command     => 'systemctl daemon-reload',
    path        => [ '/usr/bin', '/bin', '/usr/sbin' ],
    refreshonly => true,
  }
  ->
  service { 'ergochat':
    enable   => true,
    ensure   => 'running',
    provider => systemd,
  }
}
