class unix_update::unix{
  case $operatingsystem {
    'Debian': {
      exec { 'update':
        command => "/usr/bin/apt-get update --fix-missing && /usr/bin/apt-get install apt-transport-https ca-certificates --fix-missing -y",
        tries => 5,
        try_sleep => 30,
      }
    }
    'Ubuntu': {
      exec { 'update':
        command => "/usr/bin/apt-get update --fix-missing",
        tries => 5,
        try_sleep => 30,
      }
    }
    'RedHat': {
      exec { 'update':
        command => "yum update",
        tries => 5,
        try_sleep => 30,
      }
    }
    'CentOS': {
      exec { 'update':
        command => "su -c 'yum update'",
        tries => 5,
        try_sleep => 30,
      }
    }
    'Solaris': {
      exec { 'update':
        command => "pkg update",
        tries => 5,
        try_sleep => 30,
      }
    }
  }
}
