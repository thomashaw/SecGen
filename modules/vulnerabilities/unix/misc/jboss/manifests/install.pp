class jboss::install {
  Exec {
    path => ['/bin', '/usr/bin', '/usr/local/bin', '/sbin', '/usr/sbin'], 
    environment => ['http_proxy=http://172.22.0.51:3128',
        'https_proxy=http://172.22.0.51:3128',
        'ftp_proxy=http://172.22.0.51:3128',
        'JAVA_HOME="/usr/lib/jvm/adoptopenjdk-8-hotspot-amd64/bin/java"']}

  ensure_packages(['apt-transport-https', 'ca-certificates', 'wget', 'dirmngr', 'gnupg', 'software-properties-common'])

  file { '/usr/local/java':
    ensure => 'directory'
  } ->
  file { '/usr/local/java/jre-archive-files':
    source => 'puppet:///modules/jboss/jre-archive-files',
    ensure => directory,
    recurse => true,
  } ->
  exec { 'extract-java':
    command => 'cat jre-archive-files/jre* > jre-8u351-linux-x64.tar.gz; tar -xvzf jre-8u351-linux-x64.tar.gz',
    cwd => '/usr/local/java',
    creates => '/usr/local/java/jre1.8.0_351'
  } ->
  tidy {'delete-jre-archive-parts':
    path => '/usr/local/java/jre-archive-files',
    recurse => true,
  }
  tidy {'delete-jre-archive':
    path => '/usr/local/java/jre-8u351-linux-x64.tar.gz'
  }
  exec { 'change-java-install-dir-permissions':
    command => 'chmod -R 755 /usr/local/java',
  } ->
  exec { 'update-java-location':
    command => 'sudo update-alternatives --install "/usr/bin/java" "java" "/usr/local/java/jre1.8.0_351/bin/java" 1',
  } ->
  file { '/opt/jboss-archive-files':
    source => 'puppet:///modules/jboss/jboss-archive-files',
    ensure => directory,
    recurse => true,
  } ->
  exec { 'unzip-jboss':
    command => 'cat jboss-archive-files/jboss* > jboss-as-distribution-6.1.0.Final.zip; unzip jboss-as-distribution-6.1.0.Final.zip',
    cwd => '/opt',
    creates => '/opt/jboss-6.1.0.Final/bin'
  } ->
  tidy {'delete-jboss-archive-parts':
    path => '/opt/jboss-archive-files',
    recurse => true,
  }
  tidy {'delete-jboss-archive':
    path => '/opt/jboss-as-distribution-6.1.0.Final.zip'
  }
  exec { 'set-listening-interface':
    command => 'echo "JAVA_OPTS=\"\$JAVA_OPTS -Djboss.bind.address=0.0.0.0 -Djboss.bind.address.management=0.0.0.0\"" >> /opt/jboss-6.1.0.Final/bin/run.conf; mkdir /opt/made-interface',
    creates => '/opt/made-interface'
  } ->
  exec { 'change-permissions':
    command => 'chmod a+x /opt/jboss-6.1.0.Final',
  } ->
  file { '/etc/systemd/system/jboss.service':
    source => 'puppet:///modules/jboss/jboss.service'
  } ->
  exec { 'enable-jboss-service-using-systemd':
    command => 'systemctl enable --now jboss'
  }
}
