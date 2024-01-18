class webgoat::install {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $port = $secgen_parameters['port'][0]
  $hostname = $secgen_parameters['hostname'][0]

  $docroot = '/opt/webgoat'

  # owned by root, but run by www-data
  file { $docroot:
    ensure => directory,
    mode   => '0755',
    owner => 'root',
    group => 'root',
  } ->

  # owned by root, but run by www-data
  file { "$docroot/webgoat-server-8.0.0.M26.jar":
    ensure  => present,
    mode   => '0755',
    owner => 'root',
    group => 'root',
    source => 'puppet:///modules/webgoat/webgoat-server-8.0.0.M26.jar',
  } ->

  file { "/etc/systemd/system/webgoat.service":
    ensure  => present,
    mode   => '0644',
    owner => 'root',
    group => 'root',
    content  => template('webgoat/webgoat.service.erb')
  }

  user { 'webgoat':
	  ensure           => 'present',
    home             => '/tmp',
    shell            => '/bin/bash',
  }


  # TODO this is only compatible with some systems, such as Kali
  # TODO: need to add repo for other distros
  # Consider using the java class
  if ($operatingsystem == 'Debian') {
    case $operatingsystemrelease {
      /^(9|10).*/: { # do 9.x stretch stuff
        # Will error -- TODO needs repo
        package { 'openjdk-11-jre':
          ensure => installed,
        }
      }
      /^7.*/: { # do 7.x wheezy stuff
        # Will error -- TODO needs repo
        package { 'openjdk-11-jre':
          ensure => installed,
        }
      }
      'kali-rolling': { # do kali
        package { 'openjdk-11-jre':
          ensure => installed,
        }
      }
      default: {
      }
    }
  } else {
  }

}
