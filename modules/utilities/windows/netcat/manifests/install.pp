class netcat::install {
  package { 'netcat':
    provider => chocolatey,
    ensure => installed,
  }
}
