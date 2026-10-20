class hadoop_yarn_rce::install {
  # Hadoop 3.3.x needs Java 8 or 11 at runtime; Debian 12's default JDK is 17,
  # which Hadoop does not support. Debian 12 (bookworm) ships no openjdk-11
  # package, so install a self-contained Eclipse Temurin 11 JDK under
  # /usr/local alongside (not replacing) the system default JDK.
  # Latest 3.3.x. Pulled from the Apache CDN (dlcdn) rather than the archival
  # host (archive.apache.org), which throttles the ~700MB tarball hard enough
  # to blow the provisioning timeout. 3.3.6 keeps the Java 8/11 requirement and
  # the default-config unauthenticated ResourceManager this module relies on.
  $hadoop_version = '3.3.6'
  $hadoop_src     = "/usr/local/src/hadoop-${hadoop_version}.tar.gz"
  $extract_dir    = "/usr/local/hadoop-${hadoop_version}"
  $hadoop_home    = '/usr/local/hadoop'
  $hadoop_url     = "https://dlcdn.apache.org/hadoop/common/hadoop-${hadoop_version}/hadoop-${hadoop_version}.tar.gz"

  # Pinned Temurin 11 build; extracted with --strip-components so the install
  # path stays /usr/local/jdk11 regardless of the patch level.
  $jdk_url  = 'https://github.com/adoptium/temurin11-binaries/releases/download/jdk-11.0.32.1%2B1/OpenJDK11U-jdk_x64_linux_hotspot_11.0.32.1_1.tar.gz'
  $jdk_src  = '/usr/local/src/temurin-jdk11.tar.gz'
  $jdk_home = '/usr/local/jdk11'

  Exec { path => ['/bin', '/usr/bin', '/usr/local/bin', '/sbin', '/usr/sbin'] }

  ensure_packages(['wget'])

  file { '/usr/local/src':
    ensure => directory,
  }

  # Java 11 (Temurin), downloaded as data and placed by Puppet.
  exec { 'download-jdk11':
    command => "wget -q ${jdk_url} -O ${jdk_src}",
    creates => $jdk_src,
    timeout => 900,
    require => [Package['wget'], File['/usr/local/src']],
  }
  ->
  file { $jdk_home:
    ensure => directory,
  }
  ->
  exec { 'extract-jdk11':
    command => "tar -xzf ${jdk_src} -C ${jdk_home} --strip-components=1",
    creates => "${jdk_home}/bin/java",
    timeout => 900,
  }

  File['/usr/local/src']
  ->
  exec { 'download-hadoop':
    command => "wget -q --tries=3 ${hadoop_url} -O ${hadoop_src}",
    creates => $hadoop_src,
    timeout => 1800,
    require => Package['wget'],
  }
  ->
  exec { 'extract-hadoop':
    command => "tar -xzf ${hadoop_src} -C /usr/local",
    creates => $extract_dir,
    timeout => 900,
  }
  ->
  file { $hadoop_home:
    ensure => link,
    target => $extract_dir,
  }

  # Dedicated service account to run the Hadoop daemons as (not root, no
  # student login needed, so this is a plain Puppet account, not the SecGen
  # `accounts` module).
  group { 'hadoop':
    ensure => present,
  }
  ->
  user { 'hadoop':
    ensure     => present,
    gid        => 'hadoop',
    home       => '/home/hadoop',
    managehome => true,
    shell      => '/bin/bash',
  }
  ->
  exec { 'chown-hadoop-install':
    command => "chown -R hadoop:hadoop ${extract_dir}",
    unless  => "test \"\$(stat -c '%U' ${extract_dir})\" = 'hadoop'",
    require => Exec['extract-hadoop'],
  }
}
