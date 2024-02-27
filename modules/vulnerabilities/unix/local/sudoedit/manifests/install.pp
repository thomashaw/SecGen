class sudoedit::install {
  Exec { path => ['/bin', '/usr/bin', '/usr/local/bin', '/sbin', '/usr/sbin'] }

  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $leaked_filenames = $secgen_parameters['leaked_filenames']
  $strings_to_leak = $secgen_parameters['strings_to_leak']

  exec { 'i-can-sudoedit-now':
    command => "echo 'ALL ALL = (root) NOPASSWD: sudoedit /etc/hosts' >> /etc/sudoers"
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
    leaked_from => "sudoedit",
    mode => '0600'
  }
}
