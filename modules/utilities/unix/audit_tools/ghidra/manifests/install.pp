class ghidra::install{

  ensure_packages('zip')

  if $operatingsystem == 'Kali' {
    # Kali's ghidra package (12.1.3+ds) crashes in JavaHelp ("view is invalid") on kali-rolling's default Java 25.
    # Pin it to JDK 21 (min supported; the launcher requires a full JDK, not just a JRE).
    ensure_packages(['ghidra', 'openjdk-21-jdk'])
    file_line { 'ghidra java home override':
      path    => '/usr/share/ghidra/support/launch.properties',
      match   => '^JAVA_HOME_OVERRIDE=',
      line    => 'JAVA_HOME_OVERRIDE=/usr/lib/jvm/java-21-openjdk-amd64',
      require => Package['ghidra', 'openjdk-21-jdk'],
    }
  } else {
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

}
