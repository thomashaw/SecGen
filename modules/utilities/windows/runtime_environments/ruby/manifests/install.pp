class ruby::install {
  package { 'ruby':
    ensure => installed,
  }
}