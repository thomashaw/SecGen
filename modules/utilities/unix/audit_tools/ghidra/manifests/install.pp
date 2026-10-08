class ghidra::install{

  ensure_packages('zip')

  if $operatingsystem == 'Kali' {
    # Use the bundled release on Kali too: Kali's own ghidra package (12.1.3+ds, Java 25) crashes in JavaHelp
    # ("view is invalid") when opening help. Pin the release to JDK 21, as kali-rolling defaults to a newer Java.
    ensure_packages(['openjdk-21-jre', 'openjdk-21-jdk'])
  } elsif ($operatingsystem == 'Debian') {
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

  # put `ghidra` on the PATH (ghidraRun resolves its own location via readlink -f, so a symlink works)
  file { '/usr/local/bin/ghidra':
    ensure  => link,
    target  => '/opt/ghidra/ghidraRun',
    require => File['/opt/ghidra'],
  }

  if $operatingsystem == 'Kali' {
    file_line { 'ghidra java home override':
      path    => '/opt/ghidra/support/launch.properties',
      match   => '^JAVA_HOME_OVERRIDE=',
      line    => 'JAVA_HOME_OVERRIDE=/usr/lib/jvm/java-21-openjdk-amd64',
      require => [File['/opt/ghidra'], Package['openjdk-21-jdk']],
    }
  }

}
