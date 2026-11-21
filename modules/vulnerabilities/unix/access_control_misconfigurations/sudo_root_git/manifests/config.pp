class sudo_root_git::config {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $leaked_filenames = $secgen_parameters['leaked_filenames']
  $strings_to_leak = $secgen_parameters['strings_to_leak']

  # The base may not ship git. Each module is applied in its own puppet run,
  # so declare it here (idempotent).
  ensure_packages(['git'], { 'ensure' => 'installed' })

  # purge => false: the vendored class defaults to purge => true, which empties
  # /etc/sudoers.d on every run, so two sudo modules on one VM would keep only
  # the last module's rule. With false, this module stacks with the others.
  class { 'sudo':
    config_file_replace => false,
    purge               => false,
  }

  # Allow all users to run git (any arguments) as root without a password.
  # On bookworm git is /usr/bin/git and /bin is a symlink to /usr/bin; both
  # spellings are named so either path a student types matches.
  sudo::conf { 'users_sudo_git':
    ensure  => present,
    content => "ALL  ALL=(root) NOPASSWD: /bin/git, /usr/bin/git",
    require => Package['git'],
  }

  # Allow all users to run sudo -l without a password
  sudo::conf { 'users_sudo_list':
    ensure  => present,
    content => "ALL  ALL=(root) NOPASSWD: /usr/bin/sudo -l",
  }

  ::secgen_functions::leak_files { 'sudo-root-git-flag-leak':
    storage_directory => '/root',
    leaked_filenames  => $leaked_filenames,
    strings_to_leak   => $strings_to_leak,
    owner             => 'root',
    mode              => '0600',
    leaked_from       => 'sudo-root-git-flag-leak',
  }
}
