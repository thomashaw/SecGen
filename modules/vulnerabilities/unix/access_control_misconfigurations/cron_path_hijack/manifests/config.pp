class cron_path_hijack::config {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $leaked_filenames = $secgen_parameters['leaked_filenames']
  $strings_to_leak = $secgen_parameters['strings_to_leak']

  # The misconfiguration: this directory is first in the cron job's PATH and anyone can write to it.
  $writable_dir = '/usr/local/cron-bin'
  # The legitimate script, found later in PATH.
  $script_name = 'backup-check'
  $script_path = "/usr/local/sbin/${script_name}"

  # cron is not guaranteed on the desktop base; cron.d is read by the cron daemon
  ensure_packages(['cron'])

  file { $writable_dir:
    ensure => directory,
    owner  => 'root',
    group  => 'root',
    mode   => '0777',
  }

  file { $script_path:
    ensure  => file,
    owner   => 'root',
    group   => 'root',
    mode    => '0755',
    content => "#!/bin/sh\n# Housekeeping: record that the nightly backup check ran\ndate >> /var/log/backup-check.log\n",
  }

  # Called by bare name, so PATH decides which file runs, as root, every minute.
  # No dot in the file name (cron.d ignores those); trailing newline is required.
  file { '/etc/cron.d/cron_path_hijack':
    ensure  => file,
    owner   => 'root',
    group   => 'root',
    mode    => '0644',
    content => "PATH=${writable_dir}:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin\n* * * * * root ${script_name}\n",
    require => [File[$writable_dir], File[$script_path], Package['cron']],
  }

  ::secgen_functions::leak_files { 'cron_path_hijack-flag-leak':
    storage_directory => '/root',
    leaked_filenames  => $leaked_filenames,
    strings_to_leak   => $strings_to_leak,
    owner             => 'root',
    mode              => '0600',
    leaked_from       => 'cron_path_hijack-flag-leak',
  }
}
