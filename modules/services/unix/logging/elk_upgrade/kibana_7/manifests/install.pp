class kibana_7::install () {
  package { 'kibana':
    ensure => present,
  }
}
