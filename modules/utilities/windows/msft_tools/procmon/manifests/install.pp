class procmon::install {
  package { 'procmon':
    provider => chocolatey,
    ensure => installed,
  }
}
