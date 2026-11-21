class suid_root_systemctl::config {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $leaked_filenames = $secgen_parameters['leaked_filenames']
  $strings_to_leak = $secgen_parameters['strings_to_leak']

  # On bookworm /bin is a symlink to /usr/bin, and a file resource's mode on a
  # symlink path is not a reliable way to reach the binary. Resolve the real
  # file and set the bit on that, so /bin/systemctl and /usr/bin/systemctl
  # (the same inode) are both setuid.
  exec { 'suid-root-systemctl':
    command  => 'real=$(readlink -f /usr/bin/systemctl) && chown root:root "$real" && chmod 4755 "$real"',
    provider => shell,
    path     => ['/usr/bin', '/bin', '/usr/sbin', '/sbin'],
    unless   => 'test "$(stat -c %a:%U "$(readlink -f /usr/bin/systemctl)")" = "4755:root"',
  }

  ::secgen_functions::leak_files { 'suid-root-systemctl-flag-leak':
    storage_directory => '/root',
    leaked_filenames  => $leaked_filenames,
    strings_to_leak   => $strings_to_leak,
    owner             => 'root',
    mode              => '0600',
    leaked_from       => 'suid-root-systemctl-flag-leak',
  }
}
