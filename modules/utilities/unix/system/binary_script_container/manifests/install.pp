class binary_script_container::install {
  # The wrapper is compiled on the box; a bare Debian base has no compiler or make.
  # ensure_packages avoids duplicate declarations if another module wants build-essential.
  ensure_packages(['build-essential'])

  # Create install directory
  file { '/root/suid/':
    ensure => directory,
  }

  # Move wrapper.c onto box
  file { "/root/suid/suid.c":
    ensure  => file,
    source  => 'puppet:///modules/binary_script_container/wrapper.c',
    require => File['/root/suid/'],
  }

  # Make and install (setuid root, stripped)
  exec { "wrapper make install":
    command => 'make suid && install -m a+rx,u+ws -s ./suid /usr/local/bin/suid',
    cwd     => '/root/suid/',
    path    => '/bin:/sbin:/usr/bin:/usr/sbin',
    creates => '/usr/local/bin/suid',
    require => [Package['build-essential'], File['/root/suid/suid.c']],
  }
}
