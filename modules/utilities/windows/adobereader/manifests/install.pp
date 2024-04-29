class adobereader::install {
  package { 'adobereader':
    provider => chocolatey,
    ensure => '2015.007.20033.02',
  }
}
