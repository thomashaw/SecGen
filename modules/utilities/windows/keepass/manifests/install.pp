class keepass::install {
  package { 'keepass':
    provider => chocolatey,
    ensure => installed,
  }
}
