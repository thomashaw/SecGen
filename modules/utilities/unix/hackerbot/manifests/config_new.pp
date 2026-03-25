class hackerbot::config {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $ssh_key_pair  = parsejson($secgen_parameters['ssh_key_pair'][0])
  $private_key   = $ssh_key_pair['private']

  # Key directory — /opt/hackerbot is already created by hackerbot::install
  file { '/opt/hackerbot/keys':
    ensure  => directory,
    owner   => 'root',
    group   => 'root',
    mode    => '0700',
    require => File['/opt/hackerbot'],
  }

  # Private key used by hackerbot Ruby process for all SSH connections
  file { '/opt/hackerbot/keys/id_rsa':
    ensure  => file,
    owner   => 'root',
    group   => 'root',
    mode    => '0600',
    content => $private_key,
    require => File['/opt/hackerbot/keys'],
  }

  # ... existing config resources below (hackerbot XML configs, lab sheets, etc.) ...
}
