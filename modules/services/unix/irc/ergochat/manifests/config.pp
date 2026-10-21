class ergochat::config {
  file { '/etc/ergochat/ircd.yaml':
    ensure  => present,
    source  => 'puppet:///modules/ergochat/ircd.yaml',
    owner   => 'root',
    group   => 'ergochat',
    mode    => '0640',
    require => Exec['ergochat-install-files'],
    notify  => Service['ergochat'],
  }

  # Create the database before first start.
  exec { 'ergochat-initdb':
    command => 'ergo initdb --conf /etc/ergochat/ircd.yaml',
    path    => ['/usr/local/bin', '/usr/bin', '/bin'],
    user    => 'ergochat',
    creates => '/var/lib/ergochat/ircd.db',
    require => [File['/etc/ergochat/ircd.yaml'], File['/var/lib/ergochat']],
    before  => Service['ergochat'],
  }
}
