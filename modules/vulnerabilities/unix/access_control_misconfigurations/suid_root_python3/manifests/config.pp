class suid_root_python3::config {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $leaked_filenames = $secgen_parameters['leaked_filenames']
  $strings_to_leak = $secgen_parameters['strings_to_leak']

  ensure_packages(['python3'])

  # /usr/bin/python3 is a symlink to the versioned binary (python3.11 on
  # bookworm). chmod follows symlinks, but resolving it explicitly keeps the
  # intent clear and the check below honest: the bit goes on the real file.
  exec { 'suid_root_python3_set_bit':
    provider => shell,
    command  => 'chown root:root "$(readlink -f /usr/bin/python3)" && chmod 4755 "$(readlink -f /usr/bin/python3)"',
    unless   => '[ "$(stat -c "%a %U" "$(readlink -f /usr/bin/python3)")" = "4755 root" ]',
    path     => ['/usr/bin', '/bin', '/usr/sbin', '/sbin'],
    require  => Package['python3'],
  }

  # Flag only root can read
  ::secgen_functions::leak_files { 'suid-root-python3-flag-leak':
    storage_directory => '/root',
    leaked_filenames  => $leaked_filenames,
    strings_to_leak   => $strings_to_leak,
    owner             => 'root',
    mode              => '0600',
    leaked_from       => 'suid-root-python3-flag-leak',
  }
}
