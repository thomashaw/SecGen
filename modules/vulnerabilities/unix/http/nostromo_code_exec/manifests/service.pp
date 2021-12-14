#
class nostromo_code_exec::service {
  require nostromo_code_exec::config
  Exec { path => [ '/bin/', '/sbin/' , '/usr/bin/', '/usr/sbin/' ]}
  $user = 'nostromousr'#$secgen_parameters['leaked_username'][0]
  $user_home = "/home/${user}"
  $release_dir = '/home/nostromousr/nostromo-1.9.6/src/nhttpd'
  $service_file_dir = '/etc/systemd/system'

  # Move service file to /home/nostromousr/nostromo-1.9.6/src/nhttpd
  file { "${release_dir}/nhttpd.service":
    source  => 'puppet:///modules/nostromo_code_exec/nhttpd.service',
    owner   => $user,
    mode    => '0777',
    require => Exec['set-log-dir-perms'],
  } ->

  # Service file in /etc/systemd/system/
  file { "${service_file_dir}/nhttpd.service":
    source  => 'puppet:///modules/nostromo_code_exec/nhttpd.service',
    owner   => $user,
    mode    => '0777',
  } ->

  # exec { 'run-nhttpd':
  #   command => "sudo /home/${user}/nostromo-1.9.6/src/nhttpd/nhttpd",
  # } ->
  #
  service { 'nhttpd':
    ensure  => running,
    enable  => true,
  }
}
