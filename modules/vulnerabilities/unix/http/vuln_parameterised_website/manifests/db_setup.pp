class vuln_parameterised_website::db_setup {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)


  $db_username = $secgen_parameters['db_username'][0]
  $db_password = $secgen_parameters['db_password'][0]

  # Table setup file, setting the template
  file { "/tmp/db.sql":
    owner  => root,
    group  => root,
    mode   => '0600',
    ensure => file,
    content => template('vuln_parameterised_website/db.sql.erb'),
    notify => File["/tmp/mysql_setup.sh"],
  }

  # Moving across the shell script which setups the database
  file { "/tmp/mysql_setup.sh":
    owner  => root,
    group  => root,
    mode   => '0700',
    ensure => file,
    source => 'puppet:///modules/vuln_parameterised_website/mysql_setup.sh',
    notify => Exec['setup_mysql'],
  }

  # Execute the shell script with the specifed username and password
  exec { 'setup_mysql':
    cwd     => "/tmp",
    command => "sudo ./mysql_setup.sh $db_username $db_password",
    path    => [ '/bin/', '/sbin/' , '/usr/bin/', '/usr/sbin/' ],
  }

}