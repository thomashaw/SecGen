class sudo_root_perl::config {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $leaked_filenames = $secgen_parameters['leaked_filenames']
  $strings_to_leak = $secgen_parameters['strings_to_leak']

  # purge => false keeps other modules' rules in /etc/sudoers.d, so this module
  # stacks with other sudo_root_* modules on one VM. The sudoers file itself is
  # left as it is.
  class { 'sudo':
    config_file_replace => false,
    purge               => false,
  }

  # Allow all users to run perl (real binary and the usrmerge/versioned paths) as root,
  # with any arguments, without a password. /usr/bin/perl is a symlink to perl5.36.0 on bookworm.
  sudo::conf { 'users_sudo_perl':
    ensure  => present,
    content => "ALL  ALL=(root) NOPASSWD: /bin/perl *, /usr/bin/perl *, /usr/bin/perl5.36.0 *",
  }

  # Allow all users to run sudo -l without a password
  sudo::conf { 'users_sudo_list':
    ensure  => present,
    content => "ALL  ALL=(root) NOPASSWD: /usr/bin/sudo -l",
  }

  ::secgen_functions::leak_files { 'sudo-root-perl-flag-leak':
    storage_directory => '/root',
    leaked_filenames  => $leaked_filenames,
    strings_to_leak   => $strings_to_leak,
    owner             => 'root',
    mode              => '0600',
    leaked_from       => 'sudo-root-perl-flag-leak',
  }
}
