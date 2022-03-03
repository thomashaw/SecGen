# Remove proxy environment settings.
# Adjust file paths to suite SecGen.
class pachev_ftp_server_1_path_traversal::config {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $port = $secgen_parameters['port'][0]
  $strings_to_leak = $secgen_parameters['strings_to_leak']
  $leaked_filenames = $secgen_parameters['leaked_filenames']
  $strings_to_pre_leak = $secgen_parameters['strings_to_pre_leak']
  $pre_leaked_filenames = $secgen_parameters['pre_leaked_filenames']
  $user = $secgen_parameters['leaked_username'][0]
  $leaked_password = $secgen_parameters['leaked_password'][0]
  $user_home = "/home/$user"
  $release_dir = '/opt/pachev_ftp/pachev_ftp-master/ftp_server/target/release'
  $ftp_root = "$release_dir/ftproot"
  $anony_ftp_home = "$ftp_root/anonymous"
  $user_ftp_home = "$ftp_root/$user"

  Exec { path => [ '/bin/', '/sbin/' , '/usr/bin/', '/usr/sbin/' ] }

  # Create user
  user { $user:
    ensure     => present,
    home       => "$user_home",
    managehome => true,
  }


  # Create directories ftproot and ftpusr and conf
  file { [$ftp_root,
          $anony_ftp_home,
          $user_ftp_home,
          "$release_dir/conf",
          "$release_dir/logs"]:
    ensure  => 'directory',
    require => Exec['build-ftpserver'],
    owner   => $user,
    mode    => '0755', # Full permissions for $user, read-execute, read-execute.
  } ->


  # Create pachevftp.service
  file { '/opt/pachev_ftp/pachevftp.service':
    ensure => present,
    owner  => $user,
    mode   => '0755', # Full permissions for ftpusr, read-execute, read-execute.
    content => template('pachev_ftp_server_1_path_traversal/pachevftp.service.erb'),
  } ->

  # Create conf/fsys.cfg
  file { "$release_dir/conf/fsys.cfg":
    ensure  => present,
    owner   => $user,
    mode    => '0755', # Full permissions for ftpusr, read-execute, read-execute.
    source  => 'puppet:///modules/pachev_ftp_server_1_path_traversal/fsys.cfg',
  } ->

  # Create conf/users.cfg
  file { "$release_dir/conf/users.cfg":
    ensure  => present,
    owner   => $user,
    mode    => '0755', # Full permissions for ftpusr, read-execute, read-execute.
    content  => template('pachev_ftp_server_1_path_traversal/users.cfg.erb'),
  } ->

  # Create logs/fserver.log
  file { "$release_dir/logs/fserver.log":
    ensure  => present,
    source  => 'puppet:///modules/pachev_ftp_server_1_path_traversal/fserver.log',
    owner   => $user,
    mode    => '0755', # Full permissions for ftpusr, read-execute, read-execute.
  } ->

  exec { 'port-forward-route':
    command => 'sysctl -w net.ipv4.conf.all.route_localnet=1',
  } ->

  exec { 'port-forward-route-persist':
    command => "echo 'net.ipv4.conf.all.route_localnet=1' >> /etc/sysctl.conf",
  } ->

  ::secgen_functions::leak_files { 'pachev-file-leak':
    storage_directory => $user_ftp_home,
    leaked_filenames  => $leaked_filenames,
    strings_to_leak   => $strings_to_leak,
    owner             => $user,
    leaked_from       => "pachev",
    mode              => '0600'
  }
  ::secgen_functions::leak_files { 'pachev-file-pre-leak':
    storage_directory => $anony_ftp_home,
    leaked_filenames  => $pre_leaked_filenames,
    strings_to_leak   => $strings_to_pre_leak,
    owner             => $user,
    leaked_from       => "pachev-pre",
    mode              => '0600'
  }

}
