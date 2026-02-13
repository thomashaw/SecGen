# Class: apache_druid_rce::install
# Install process for apache druid RCE
# https://archive.apache.org/dist/druid/0.20.0/
class apache_druid_rce::install {
  Exec { path => [ '/bin/', '/sbin/' , '/usr/bin/', '/usr/sbin/' ] }
  $modulename = 'apache_druid_rce'

  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $port = $secgen_parameters['port'][0]
  $user = $secgen_parameters['unix_username'][0]
  $user_home = "/home/${user}"

  # Create user
  user { $user:
    ensure     => present,
    home       => $user_home,
    managehome => true,
  }

  file { '/usr/local/java':
    ensure => 'directory'
  }
  -> file { '/usr/local/java/jre-archive-files':
    ensure  => directory,
    source  => 'puppet:///modules/apache_druid_rce/jre-archive-files',
    recurse => true,
  }
  -> exec { 'extract-java':
    command => 'cat jre-archive-files/jre* > jre-8u351-linux-x64.tar.gz; tar -xvzf jre-8u351-linux-x64.tar.gz',
    cwd     => '/usr/local/java',
    creates => '/usr/local/java/jre1.8.0_351'
  }
  -> tidy {'delete-jre-archive-parts':
    path    => '/usr/local/java/jre-archive-files',
    recurse => true,
  }
  tidy {'delete-jre-archive':
    path => '/usr/local/java/jre-8u351-linux-x64.tar.gz'
  }
  exec { 'change-java-install-dir-permissions':
    command => 'chmod -R 755 /usr/local/java',
  }
  -> exec { 'update-java-location':
    command => 'sudo update-alternatives --install "/usr/bin/java" "java" "/usr/local/java/jre1.8.0_351/bin/java" 1',
  }

  $releasename = "${modulename}.tar.gz"
  $currentsource = ["${releasename}.partaa",
    "${releasename}.partab",
    "${releasename}.partac",
    "${releasename}.partad",
    "${releasename}.partae",
    "${releasename}.partaf",
    "${releasename}.partag"]

  $currentsource.each |String $fsource| {
    file { "/tmp/${fsource}":
      ensure => file,
      source => "puppet:///modules/${modulename}/${fsource}",
    }
  }

  exec { 'rebuild-archive':
    cwd     => '/tmp/',
    command => "cat ${releasename}.parta* > ${releasename}",
  }
  -> exec { 'unpack-druid':
    cwd     => '/tmp',
    command => "tar -xf ${releasename}",
    creates => '/tmp/apache-druid-0.20.0',
  }
  -> exec { 'move-druid':
    cwd     => '/tmp',
    command => 'mv apache-druid-0.20.0 /usr/local/apache-druid/',
    creates => '/usr/local/apache-druid'
  }
  -> exec { 'chmod-druid':
    command => 'chmod -R 777 /usr/local/apache-druid/bin/',
  }
  -> exec { 'chown-druid':
    command => "chown -R ${user}:${user} /usr/local/apache-druid/",
  }
  -> exec { 'change-port':
    command => "sed -i 's/8888/${port}/' /usr/local/apache-druid/conf/druid/single-server/nano-quickstart/router/runtime.properties",
  }
}
