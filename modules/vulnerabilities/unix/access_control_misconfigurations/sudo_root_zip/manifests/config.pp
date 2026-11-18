class sudo_root_zip::config {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $leaked_filenames = $secgen_parameters['leaked_filenames']
  $strings_to_leak = $secgen_parameters['strings_to_leak']

  # The base may not ship zip
  ensure_packages(['zip'], { 'ensure' => 'installed' })

  # purge => false: the vendored class empties /etc/sudoers.d by default, which
  # would drop the rules of any other sudo module on the same VM.
  class { 'sudo':
    config_file_replace => false,
    purge               => false,
  }

  # Allow all users to run zip with any arguments as root without a password.
  # /usr/bin/zip is the real binary on bookworm; /bin is a symlink to /usr/bin.
  sudo::conf { 'users_sudo_zip':
    ensure  => present,
    content => "ALL  ALL=(root) NOPASSWD: /bin/zip *, /usr/bin/zip *",
    require => Package['zip'],
  }

  # Allow all users to run sudo -l without a password (own title, so it does
  # not clash with other sudo modules' list rule)
  sudo::conf { 'users_sudo_list_zip':
    ensure  => present,
    content => "ALL  ALL=(root) NOPASSWD: /usr/bin/sudo -l",
  }

  ::secgen_functions::leak_files { 'sudo-root-zip-flag-leak':
    storage_directory => '/root',
    leaked_filenames  => $leaked_filenames,
    strings_to_leak   => $strings_to_leak,
    owner             => 'root',
    mode              => '0600',
    leaked_from       => 'sudo-root-zip-flag-leak',
  }
}
