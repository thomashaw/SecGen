class suid_root_cp::config {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $leaked_filenames = $secgen_parameters['leaked_filenames']
  $strings_to_leak = $secgen_parameters['strings_to_leak']

  # The misconfiguration: the real cp binary is setuid root. On Debian bookworm
  # (merged /usr) /bin is a symlink to /usr/bin, and /usr/bin/cp is the regular
  # coreutils file, so this is the path to set; setting the mode on the /bin
  # symlink would manage the link, not the binary.
  file { '/usr/bin/cp':
    ensure => file,
    owner  => 'root',
    group  => 'root',
    mode   => '4755',
  }

  # The flag, readable by root only: the student has to escalate to reach it.
  ::secgen_functions::leak_files { 'suid-root-cp-flag-leak':
    storage_directory => '/root',
    leaked_filenames  => $leaked_filenames,
    strings_to_leak   => $strings_to_leak,
    owner             => 'root',
    mode              => '0600',
    leaked_from       => 'suid-root-cp-flag-leak',
  }
}
