# Adjust file paths to suite SecGen.
class pachev_ftp_server_1_path_traversal::service {
  require pachev_ftp_server_1_path_traversal::config
  Exec { path => [ '/bin/', '/sbin/' , '/usr/bin/', '/usr/sbin/' ] }

  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $port = $secgen_parameters['port'][0]
  $release_dir = '/opt/pachev_ftp/pachev_ftp-master/ftp_server/target/release'
  $user = $secgen_parameters['leaked_username'][0]

  file { '/etc/network/if-pre-up.d':
    ensure  => directory,
  } ->
  file { '/etc/network/if-pre-up.d/iptables':
    ensure  => present,
    owner   => 'root',
    mode    => '0755', # execute permissions.
    content => '#!/bin/sh'
  } ->
  file_line { 'ftp_redirect_port_rule':
    path => '/etc/network/if-pre-up.d/iptables',
    line => "iptables -t nat -I PREROUTING -p tcp --dport $port -j DNAT --to 127.0.0.1:2121",
  }

  # recreates in /etc/systemd/, but could link to the copy in /opt/
  file { '/etc/systemd/system/pachevftp.service':
    ensure  => present,
    content  => template('pachev_ftp_server_1_path_traversal/pachevftp.service.erb'),
    owner   => 'root',
    mode    => '0777', # Full permissions.
    require => File['/opt/pachev_ftp/pachevftp.service'],
  } ->

  exec { 'set-perm-one':
    command => "sudo setfacl -m u:$user:rwx $release_dir/ftp_server",
  } ->

  exec { 'set-perm-two':
    command => "sudo setfacl -m u:$user:rwx /etc/systemd/system/pachevftp.service",
  } ->

  service { 'pachevftp':
    ensure  => running,
    enable  => true,
  }
}
