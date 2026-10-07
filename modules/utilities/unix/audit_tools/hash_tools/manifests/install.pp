class hash_tools::install{
  # kali-rolling dropped the md5deep transitional package; hashdeep provides the md5deep/sha*deep commands
  if $operatingsystem == 'Kali' {
    package { ['hashdeep']:
      ensure => 'installed',
    }
  } else {
    package { ['md5deep']:
      ensure => 'installed',
    }
  }
  case $operatingsystem {
    'Debian': {
      package { ['debsums']:
        ensure => 'installed',
      }
    }
  }
}
