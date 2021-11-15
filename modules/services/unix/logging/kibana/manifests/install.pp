class kibana::install () {
  package { 'kibana':
    ensure => present,
  }
}
