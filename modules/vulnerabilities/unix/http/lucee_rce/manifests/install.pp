# Class: lucee_rce::install
# Install process for lucee can be found at:
# https://docs.lucee.org/guides/installing-lucee/download-and-install.html
class lucee_rce::install {
  Exec { path => [ '/bin/', '/sbin/' , '/usr/bin/', '/usr/sbin/' ] }
  $modulename = 'lucee_rce'
  $releasename = 'lucee-express-5.3.7.43.zip'
  $splits = ["${releasename}.partaa",
  "${releasename}.partab"]

  ensure_packages(['openjdk-11-jdk'], { ensure => 'installed'})

  $splits.each |String $split| {
    file { "/tmp/${split}":
      ensure => file,
      source => "puppet:///modules/${modulename}/${split}",
    }
  }

  exec { 'rebuild-archive':
    cwd     => '/tmp/',
    command => "cat ${releasename}.parta* >/usr/local/src/${releasename}",
  }
  -> exec { 'unpack-lucee':
    cwd     => '/usr/local/src/',
    command => 'unzip -n lucee-express-5.3.7.43.zip',
  }
  -> file { '/usr/local/src/logs/':
    ensure => directory,
  }
  -> exec { 'giveperms-lucee':
    command => 'chmod -R 777 /usr/local/src/bin/',
  }
  #-> file { '/usr/local/src/lucee-express-5.3.7.43.zip':
  #  ensure => absent
  #}
}
