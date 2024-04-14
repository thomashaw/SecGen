class nginx::install {
  package { 'nginx-service':
    provider => chocolatey,
    ensure => installed,
  }
}
