class elasticsearch::update_apt {
  file { '/etc/apt/sources.list.d/elastic-7.x.list':
    ensure => file,
    source => 'puppet:///modules/elasticsearch/elastic-7.x.list'
  }

  exec { 'apt update':
    command => '/usr/bin/apt-get update --fix-missing',
    require => File['/etc/apt/sources.list.d/elastic-7.x.list']
  }
}
