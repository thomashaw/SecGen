class proftpd_133c_backdoor_nonroot::config {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $raw_org = $secgen_parameters['organisation']
  $leaked_filenames = $secgen_parameters['leaked_filenames']
  $strings_to_leak = $secgen_parameters['strings_to_leak']
  $strings_to_pre_leak = $secgen_parameters['strings_to_pre_leak']
  $pre_leaked_filenames = $secgen_parameters['pre_leaked_filenames']

  if $raw_org and $raw_org[0] and $raw_org[0] != '' {
    $organisation = parsejson($raw_org[0])
  } else {
    $organisation = ''
  }

  $anon_user = 'anon' #$secgen_parameters['leaked_username'][0]
  $anon_user_home = "/home/$anon_user"
  $deploy_user = 'ftp'
  $deploy_user_home = "/home/$deploy_user"

  Exec { path => [ '/bin/', '/sbin/' , '/usr/bin/', '/usr/sbin/' ] }

  # Create user
  user { $anon_user:
    ensure     => present,
    home       => "$anon_user_home",
    managehome => true,
  } ->
  user { $deploy_user:
    ensure     => present,
    home       => "$deploy_user_home",
    managehome => true,
  } ->



  file { '/etc/proftpd/proftpd.conf':
    ensure   => present,
    owner    => 'root',
    group    => 'root',
    mode     => '0644',
    content  => template('proftpd_133c_backdoor_nonroot/proftpd.erb')
  }

  ::secgen_functions::leak_files { 'proftpd_133c_backdoor_nonroot-file-leak':
    storage_directory => '/root',
    leaked_filenames  => $leaked_filenames,
    strings_to_leak   => $strings_to_leak,
    leaked_from       => "proftpd_133c_backdoor_nonroot",
    mode              => '0600'
  }
  ::secgen_functions::leak_files { 'proftpd_133c_backdoor_nonroot-file-pre-leak':
    storage_directory => $anon_user_home,
    leaked_filenames  => $pre_leaked_filenames,
    strings_to_leak   => $strings_to_pre_leak,
    leaked_from       => "proftpd_133c_backdoor_nonroot-pre",
    mode              => '0644',
    owner             => $anon_user
  }
}
