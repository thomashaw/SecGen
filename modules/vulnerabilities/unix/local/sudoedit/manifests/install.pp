class sudoedit::install {
  Exec { path => ['/bin', '/usr/bin', '/usr/local/bin', '/sbin', '/usr/sbin'] }

  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $leaked_filenames = $secgen_parameters['leaked_filenames']
  $strings_to_leak = $secgen_parameters['strings_to_leak']
  $username = $secgen_parameters['unix_username'][0]
  $password = $secgen_parameters['used_password'][0]

  # Magic touch
  # EDITOR='nano -- /etc/sudoers' sudoedit /etc/hosts

  # This exploit relies on a user being in sudo group but we dont want access to everything! :) 
  exec { 'goodbye-sudo':
    command => "sed -i 's/%sudo/%root/' /etc/sudoers"
  }
  -> user { $username:
    ensure   => present,
    managehome => true,
    # Make sure we are in the sudo group
    groups => 'sudo',
    shell    => '/bin/bash',
    password => pw_hash($password, 'SHA-512', 'mysalt'),
  }
  # Let access to the hosts file via sudoedit
  -> exec { 'i-can-edit-now':
    command => "echo '${username} ALL=(ALL:ALL) sudoedit /etc/hosts' >> /etc/sudoers"
  }
  -> file { '/tmp/sudo_1.8.26-2_amd64.deb':
    ensure => file,
    source => 'puppet:///modules/sudoedit/sudo_1.8.26-2_amd64.deb',
  }
  -> package { 'downgrade sudo':
    ensure   => installed,
    provider => dpkg,
    source   => '/tmp/sudo_1.8.26-2_amd64.deb'
  }

  # Leak a file containing a string/flag to /root/
  ::secgen_functions::leak_files { 'sudoedit-file-leak':
    storage_directory => '/root',
    leaked_filenames => $leaked_filenames,
    strings_to_leak => $strings_to_leak,
    leaked_from => "",
    mode => '0600'
  }
}
