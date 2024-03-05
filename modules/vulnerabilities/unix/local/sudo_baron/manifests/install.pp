class sudo_baron::install {
  Exec { path => ['/bin', '/usr/bin', '/usr/local/bin', '/sbin', '/usr/sbin'] }

  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $leaked_filenames = $secgen_parameters['leaked_filenames']
  $strings_to_leak = $secgen_parameters['strings_to_leak']

  file { '/tmp/sudo_1.8.19p1-2.1_amd64.deb':
    ensure => file,
    source => 'puppet:///modules/sudo_baron/sudo_1.8.19p1-2.1_amd64.deb',
  }
  -> package { 'downgrade sudo':
    ensure   => installed,
    provider => dpkg,
    source   => '/tmp/sudo_1.8.19p1-2.1_amd64.deb'
  }

  # Leak a file containing a string/flag to /root/
  ::secgen_functions::leak_files { 'sudo-baron-file-leak':
    storage_directory => '/root',
    leaked_filenames => $leaked_filenames,
    strings_to_leak => $strings_to_leak,
    leaked_from => "sudobaron",
    mode => '0600'
  }
}
