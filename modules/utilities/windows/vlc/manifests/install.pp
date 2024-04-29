class vlc::install {
  package { 'vlc':
    provider => chocolatey,
    ensure => installed,
  }
}
