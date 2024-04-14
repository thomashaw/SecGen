class procexp::install {
  package { 'procexp':
    provider => chocolatey,
    ensure => installed,
  }
}
