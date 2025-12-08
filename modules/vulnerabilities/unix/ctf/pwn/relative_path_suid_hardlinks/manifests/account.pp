define relative_path_suid_hardlinks::account($username, $password, $strings_to_leak, $leaked_filenames) {
  # Create matching group first
  group { $username:
    ensure => present,
  }

  # Create user account
  user { $username:
    ensure     => present,
    shell      => '/bin/bash',
    password   => pw_hash($password, 'SHA-512', 'mysalt'),
    managehome => true,
    home       => "/home/${username}",
    gid        => $username,
    require    => Group[$username],
  }

  # Ensure home directory has correct permissions for the exploit to work
  # The attacker needs to be able to traverse into the directory to create hardlinks
  file { "/home/${username}":
    ensure  => directory,
    mode    => '0755',
    owner   => $username,
    group   => $username,
    require => User[$username],
  }

  # Leak strings in a text file in the users home directory
  # Hardlinks can still be created with fs.protected_hardlinks=0
  ::secgen_functions::leak_files { "$username-file-leak":
    storage_directory => "/home/$username/",
    leaked_filenames  => $leaked_filenames,
    strings_to_leak   => $strings_to_leak,
    owner             => $username,
    group             => $username,
    mode              => '0600',
    leaked_from       => "accounts_$username",
  }

  file { "/home/$username/access_my_flag.c":
    owner  => $username,
    group  => $username,
    mode   => '0644',
    ensure => file,
    source => 'puppet:///modules/relative_path_suid_hardlinks/access_my_flag.c',
  } ->

  exec { "$username-compileandsetup2":
    cwd     => "/home/$username/",
    command => "gcc -o access_my_flag access_my_flag.c && sudo chown $username access_my_flag && sudo chmod 4755 access_my_flag",
    path    => [ '/bin/', '/sbin/' , '/usr/bin/', '/usr/sbin/' ],
  }

  # overwrite any existing content (exists on Debian Buster)
  # Debian 12 requires additional settings for the hardlink exploit to work:
  # - fs.protected_hardlinks = 0: Disables hardlink protections (necessary but not sufficient)
  # - fs.protected_regular = 0: Allows creating hardlinks to files in world-writable sticky directories
  # - fs.protected_fifos = 0: Additional protection that may interfere
  if !defined(File['/etc/sysctl.d/protect-links.conf']) {
    file { '/etc/sysctl.d/protect-links.conf':
      content => "fs.protected_hardlinks = 0\nfs.protected_symlinks = 0\nfs.protected_regular = 0\nfs.protected_fifos = 0\n",
      notify  => Exec['reload-sysctl-hardlinks'],
    }
  }

  # Apply the sysctl settings immediately for Debian 12
  if !defined(Exec['reload-sysctl-hardlinks']) {
    exec { 'reload-sysctl-hardlinks':
      command     => '/sbin/sysctl -p /etc/sysctl.d/protect-links.conf',
      refreshonly => true,
    }
  }

}