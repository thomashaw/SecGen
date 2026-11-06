class apache_bash_cgi::init {
  file { '/usr/lib/cgi-bin/':
    ensure => directory,
  }

  file { '/usr/lib/cgi-bin/test.cgi':
    ensure  => file,
    source  => 'puppet:///modules/apache_bash_cgi/test.cgi',
    mode    => '0755',
    require => File['/usr/lib/cgi-bin/'],
  }

  # ScriptAlias for /cgi-bin/. Dropped into the apache module's conf.d, which its
  # apache2.conf pulls in via IncludeOptional. The required apache module runs in
  # an earlier, separate puppet apply (SecGen applies one catalogue per module),
  # so its resources — Class['apache'], File['/etc/apache2/conf.d'] — are not in
  # this catalogue and must not be referenced. The <requires> tag orders the
  # applies, so the conf.d directory already exists on disk by the time we run.
  file { '/etc/apache2/conf.d/apache-bash-cgi.conf':
    ensure  => file,
    source  => 'puppet:///modules/apache_bash_cgi/cgi-bin.conf',
    owner   => 'root',
    group   => 'root',
    mode    => '0644',
    require => File['/usr/lib/cgi-bin/test.cgi'],
    notify  => Exec['apache-bash-cgi-reload'],
  }

  # Debian's apache2.service sets PrivateTmp=true, giving the CGI (and any shell a
  # Shellshock exploit spawns through it) a private /tmp that nothing else on the box
  # can see. That breaks lab flows where another process stages a file in /tmp for the
  # www-data shell to read, or reads a marker the shell drops there. Turn it off so the
  # exploited shell shares the real /tmp. (This box is deliberately vulnerable, so the
  # lost hardening does not matter here.)
  file { '/etc/systemd/system/apache2.service.d':
    ensure => directory,
    owner  => 'root',
    group  => 'root',
    mode   => '0755',
  }

  file { '/etc/systemd/system/apache2.service.d/no-privatetmp.conf':
    ensure  => file,
    owner   => 'root',
    group   => 'root',
    mode    => '0644',
    content => "[Service]\nPrivateTmp=false\n",
    require => File['/etc/systemd/system/apache2.service.d'],
    notify  => Exec['apache-bash-cgi-reload'],
  }

  # Restart apache so it reads the new ScriptAlias and the PrivateTmp override.
  # refreshonly + the notifies above means this only fires when a config file is
  # (re)written. daemon-reload is needed for the systemd drop-in to take effect.
  exec { 'apache-bash-cgi-reload':
    command     => 'systemctl daemon-reload && systemctl restart apache2',
    path        => ['/usr/bin', '/bin', '/usr/sbin', '/sbin'],
    refreshonly => true,
  }
}
