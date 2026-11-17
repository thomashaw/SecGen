# Runs Jupyter Notebook under systemd as the unprivileged service user so it
# returns after the reboot onto the isolated VLAN. HOME is set explicitly: the
# guest-agent/systemd environment has none, and Jupyter needs it to find its
# config and runtime directories.
class jupyter_no_token::service {
  file { '/etc/systemd/system/jupyter.service':
    ensure  => file,
    owner   => 'root',
    group   => 'root',
    mode    => '0644',
    content => "[Unit]
Description=Jupyter Notebook (no authentication)
After=network.target

[Service]
Type=simple
User=jupyter
Group=jupyter
Environment=HOME=/home/jupyter
WorkingDirectory=/home/jupyter
ExecStart=/usr/bin/jupyter-notebook --config=/home/jupyter/.jupyter/jupyter_notebook_config.py
Restart=on-failure

[Install]
WantedBy=multi-user.target
",
    require => Class['jupyter_no_token::config'],
  }

  exec { 'jupyter systemd daemon-reload':
    command     => '/bin/systemctl daemon-reload',
    refreshonly => true,
    subscribe   => File['/etc/systemd/system/jupyter.service'],
  }

  service { 'jupyter':
    ensure  => running,
    enable  => true,
    require => [Exec['jupyter systemd daemon-reload'], File['/etc/systemd/system/jupyter.service']],
  }
}
