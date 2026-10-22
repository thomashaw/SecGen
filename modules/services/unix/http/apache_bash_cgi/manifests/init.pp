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

  # ScriptAlias for /cgi-bin/. Dropped into the apache module's conf.d (which it
  # includes via IncludeOptional) and the service is restarted to pick it up.
  # Ordered against the apache class rather than File['/etc/apache2/conf.d'],
  # which the build reported as missing from the catalogue.
  file { '/etc/apache2/conf.d/apache-bash-cgi.conf':
    ensure  => file,
    source  => 'puppet:///modules/apache_bash_cgi/cgi-bin.conf',
    owner   => 'root',
    group   => 'root',
    mode    => '0644',
    require => Class['apache'],
    notify  => Class['apache::service'],
  }
}
