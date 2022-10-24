class numpy::install{
  package { ['python3-pip', 'python3-numpy']:
    ensure => 'installed',
  }
}