class ghidra::install{

  ensure_packages('zip')

  if ($operatingsystem == 'Debian') {
    case $operatingsystemrelease {
      /^(12).*/: { # do 12.x bookworm stuff
        ensure_packages(['openjdk-17-jre', 'openjdk-17-jdk'])
      }
      /^(9|10).*/: { # do 9.x stretch stuff
        ensure_packages(['openjdk-11-jre', 'openjdk-11-jdk'])
      }
      /^7.*/: { # do 7.x wheezy stuff
        # Will error -- TODO needs repo
        ensure_packages(['openjdk-11-jre', 'openjdk-11-jdk'])
      }
      'kali-rolling': { # do kali
        ensure_packages(['openjdk-11-jre', 'openjdk-11-jdk'])
      }
      default: {
      }
    }
  }

  file { '/opt/ghidra':
    ensure => directory,
    recurse => true,
    source => 'puppet:///modules/ghidra/release',
    mode   => '0777',
    owner => 'root',
    group => 'root',
  } ->
  file { '/usr/share/applications/ghidra.desktop':
    source => 'puppet:///modules/ghidra/ghidra.desktop',
    mode   => '0644',
    owner => 'root',
    group => 'root',
  }

}
