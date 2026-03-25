class hackerbot_client::config {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $ssh_key_pair = parsejson($secgen_parameters['ssh_key_pair'][0])
  $public_key   = $ssh_key_pair['public']

  # /root/.ssh directory
  file { '/root/.ssh':
    ensure => directory,
    owner  => 'root',
    group  => 'root',
    mode   => '0700',
  }

  # Append Hackerbot's public key to authorized_keys
  # ssh_authorized_key is idempotent — safe to apply repeatedly
  ssh_authorized_key { 'hackerbot':
    ensure  => present,
    user    => 'root',
    type    => 'ssh-rsa',
    key     => split($public_key, ' ')[1],
    require => File['/root/.ssh'],
  }

  # Ensure correct permissions on authorized_keys
  file { '/root/.ssh/authorized_keys':
    ensure  => file,
    owner   => 'root',
    group   => 'root',
    mode    => '0600',
    require => Ssh_authorized_key['hackerbot'],
  }

  # Allow root login via key but not password
  file_line { 'permit_root_login':
    path  => '/etc/ssh/sshd_config',
    line  => 'PermitRootLogin prohibit-password',
    match => '^#?PermitRootLogin',
  }

  service { 'ssh':
    ensure    => running,
    enable    => true,
    subscribe => File_line['permit_root_login'],
  }
}
