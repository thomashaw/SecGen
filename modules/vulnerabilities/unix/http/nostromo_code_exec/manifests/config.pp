#
class nostromo_code_exec::config {
  Exec { path => [ '/bin/', '/sbin/' , '/usr/bin/', '/usr/sbin/' ]}
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $port = $secgen_parameters['port'][0]
  $strings_to_leak = $secgen_parameters['strings_to_leak']
  $leaked_filenames = $secgen_parameters['leaked_filenames']
  $strings_to_pre_leak = $secgen_parameters['strings_to_pre_leak']

  $user = 'nostromousr'#$secgen_parameters['leaked_username'][0]
  $user_home = "/home/${user}"
  $nostromo_root_var_dir = '/var/nostromo/'


  # Copy the config file to /var/nostromo/conf/
  file { "${nostromo_root_var_dir}/conf/nhttpd.conf":
    content  => template('nostromo_code_exec/nhttpd.conf.erb'),
    owner   => $user,
    require => Exec['make-nostromo-install'],
  } ->

  file { "${nostromo_root_var_dir}/htdocs/index.html":
    content  => template('nostromo_code_exec/pre_leak.html.erb'),
    owner   => $user,
  } ->

  # Set /var/nostromo/logs to 777
  exec { 'set-log-dir-perms':
    command => 'sudo chmod 777 /var/nostromo/logs',
  }

  ::secgen_functions::leak_files { 'nostromo-file-leak':
    storage_directory => $user_home,
    leaked_filenames  => $leaked_filenames,
    strings_to_leak   => $strings_to_leak,
    owner             => $user,
    leaked_from       => "nostromo",
    mode              => '0600'
  }
  # Next steps in Service file
}
