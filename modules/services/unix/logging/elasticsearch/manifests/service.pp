class elasticsearch::service {
  service { 'elasticsearch':
    enable  => true,
  }

  # remove startup timeout
  file { '/etc/systemd/system/elasticsearch.service.d/':
    ensure => directory,
  }->
  file { '/etc/systemd/system/elasticsearch.service.d/startup-timeout.conf':
    ensure => present,
    content => "[Service]\nTimeoutStartSec=180"
  }
}
