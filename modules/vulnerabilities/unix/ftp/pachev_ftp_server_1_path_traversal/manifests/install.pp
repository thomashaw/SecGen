# Remove proxy environment settings.
# Adjust file paths to suite SecGen.
class pachev_ftp_server_1_path_traversal::install {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $user = $secgen_parameters['leaked_username'][0]

  Exec { path => [ '/bin/', '/sbin/' , '/usr/bin/', '/usr/sbin/' ] }

  ensure_packages('rustc')

  # Create /opt/pachev_ftp directory
  file { '/opt/pachev_ftp':
    ensure => 'directory',
    owner  => $user,
    mode   => '0755', # Full permissions for ftpusr, read-execute, read-execute.
    require => User[$user],
  } ->

  # Require .zip
  file { '/opt/pachev_ftp/pachev_ftp-master.zip':
    source  => 'puppet:///modules/pachev_ftp_server_1_path_traversal/pachev_ftp-master.zip',
    owner   => $user,
    mode    => '0755', # Full permissions for ftpusr, read-execute, read-execute.
  } ->

  # Unzip
  exec { 'unzip-pachev-ftp-master':
    command => 'unzip pachev_ftp-master.zip',
    cwd     => '/opt/pachev_ftp/',
    creates => '/opt/pachev_ftp/pachev_ftp-master/',
    require => Package['rustc'],
  } ->

  # Update Cargo
  exec { 'update-cargo':
    command => 'cargo update',
    cwd     => '/opt/pachev_ftp/pachev_ftp-master/ftp_server/',
  } ->

  # Cargo build
  exec { 'build-ftpserver':
    command => 'cargo build --release',
    cwd     => '/opt/pachev_ftp/pachev_ftp-master/ftp_server/',
  }

}
