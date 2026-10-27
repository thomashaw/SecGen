class gitlist_040::install {

  Exec { path => ['/bin', '/usr/bin', '/usr/local/bin', '/sbin', '/usr/sbin'] }

  ensure_packages('git')

  $archive = 'gitlist-0.4.0.tar.gz'

  # apache2 is installed later (apache.pp); create the web root now.
  file { '/var/www':
    ensure => directory,
    mode   => '0755',
  }

  file { "/usr/local/src/$archive":
    ensure => file,
    source => "puppet:///modules/gitlist_040/$archive",
  }

  exec { 'unpack-gitlist':
    cwd     => '/usr/local/src',
    command => "tar -xzf $archive -C /var/www",
    require => File['/var/www'],
  }

  exec { 'copy-ini-file':
    require => Exec['unpack-gitlist'],
    command => 'cp /var/www/gitlist/config.ini-example /var/www/gitlist/config.ini',
  }

  file { '/var/www/gitlist/cache':
    require => Exec['unpack-gitlist'],
    ensure  => directory,
    mode    => '777',
  }

  # GitList routes pretty URLs to index.php via .htaccess (needs mod_rewrite and
  # AllowOverride All, set in apache.pp).
  file { '/var/www/gitlist/.htaccess':
    require => Exec['unpack-gitlist'],
    ensure  => present,
    content => template('gitlist_040/.htaccess.erb'),
  }
}
