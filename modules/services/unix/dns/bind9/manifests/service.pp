class bind9::install {
  service { 'bind9':
    ensure  => running,
    enable  => true,
    require => Package['bind9'],
  }
}