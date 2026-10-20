class hadoop_yarn_rce::hadoop {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $strings_to_leak   = $secgen_parameters['strings_to_leak']
  $leaked_filenames  = $secgen_parameters['leaked_filenames']

  $hadoop_home = '/usr/local/hadoop'
  $java_home   = '/usr/local/jdk11'

  Exec { path => ['/bin', '/usr/bin', '/usr/local/bin', '/sbin', '/usr/sbin'] }

  # Point Hadoop at the Java 11 JDK installed alongside the system default.
  file_line { 'hadoop-env-java-home':
    path  => "${hadoop_home}/etc/hadoop/hadoop-env.sh",
    line  => "export JAVA_HOME=${java_home}",
    match => '^#?\s*export JAVA_HOME=',
  }

  file { "${hadoop_home}/logs":
    ensure => directory,
    owner  => 'hadoop',
    group  => 'hadoop',
  }

  # core-site.xml is left at its shipped default. The ResourceManager addresses
  # in yarn-site.xml are likewise left at their defaults (bind 0.0.0.0, no
  # authentication/ACLs on the REST API) - that open API *is* the vulnerability
  # this module demonstrates. yarn-site.xml only adds single-node NodeManager
  # capacity settings (see the template), so the ApplicationMaster container a
  # submitted application asks for is actually scheduled and launched - which is
  # what turns the open REST API into command execution as the hadoop account.
  file { "${hadoop_home}/etc/hadoop/yarn-site.xml":
    ensure  => file,
    content => template('hadoop_yarn_rce/yarn-site.xml.erb'),
  }

  file { '/etc/systemd/system/hadoop-resourcemanager.service':
    ensure  => file,
    content => template('hadoop_yarn_rce/hadoop-resourcemanager.service.erb'),
  }
  ~> Exec['hadoop-systemd-reload']

  file { '/etc/systemd/system/hadoop-nodemanager.service':
    ensure  => file,
    content => template('hadoop_yarn_rce/hadoop-nodemanager.service.erb'),
  }
  ~> Exec['hadoop-systemd-reload']

  exec { 'hadoop-systemd-reload':
    command     => 'systemctl daemon-reload',
    refreshonly => true,
  }

  service { 'hadoop-resourcemanager':
    ensure    => running,
    enable    => true,
    require   => [
      File['/etc/systemd/system/hadoop-resourcemanager.service'],
      File["${hadoop_home}/etc/hadoop/yarn-site.xml"],
      File_Line['hadoop-env-java-home'],
      File["${hadoop_home}/logs"],
    ],
    subscribe => [
      File['/etc/systemd/system/hadoop-resourcemanager.service'],
      File["${hadoop_home}/etc/hadoop/yarn-site.xml"],
    ],
  }

  # The NodeManager runs the containers the ResourceManager schedules; without
  # it a submitted application sits unassigned and no command ever runs. Starts
  # after the ResourceManager and registers with it on the default local ports.
  service { 'hadoop-nodemanager':
    ensure    => running,
    enable    => true,
    require   => [
      File['/etc/systemd/system/hadoop-nodemanager.service'],
      File["${hadoop_home}/etc/hadoop/yarn-site.xml"],
      File_Line['hadoop-env-java-home'],
      File["${hadoop_home}/logs"],
      Service['hadoop-resourcemanager'],
    ],
    subscribe => [
      File['/etc/systemd/system/hadoop-nodemanager.service'],
      File["${hadoop_home}/etc/hadoop/yarn-site.xml"],
    ],
  }

  # Leak the solution payload into the hadoop service account's home
  # directory: reachable only after exploiting the ResourceManager (the
  # exploit runs commands as this account), not before.
  if $strings_to_leak and $leaked_filenames {
    ::secgen_functions::leak_files { 'hadoop_yarn_rce-leak':
      storage_directory => '/home/hadoop',
      leaked_filenames   => $leaked_filenames,
      strings_to_leak    => $strings_to_leak,
      owner              => 'hadoop',
      group              => 'hadoop',
      mode               => '0600',
      leaked_from        => 'hadoop_yarn_rce',
      require            => User['hadoop'],
    }
  }
}
