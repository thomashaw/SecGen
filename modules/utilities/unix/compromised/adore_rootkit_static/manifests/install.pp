class adore_rootkit_static::install {

  # TODO: rootkit configuration
  # $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  # $hidden_ports = join($secgen_parameters['hidden_ports'], "\|")
  # $hidden_strings = join($secgen_parameters['hidden_strings'], "\|")

  # The bundled binaries are 32-bit dynamically linked (ld-linux.so.2, libc.so.6),
  # so they only run on a 64-bit Debian once i386 multiarch and libc6:i386 are present.
  exec { 'adore_add_i386_arch':
    command  => '/usr/bin/dpkg --add-architecture i386 && /usr/bin/apt-get update',
    unless   => '/usr/bin/dpkg --print-foreign-architectures | /bin/grep -qx i386',
    provider => shell,
  }
  package { 'libc6:i386':
    ensure  => installed,
    require => Exec['adore_add_i386_arch'],
  }

  file { '/bin/ls_a':
    source => 'puppet:///modules/adore_rootkit_static/ls',
    mode   => '0755',
    owner   => 'root',
    group   => 'root',
    require => Package['libc6:i386'],
  }
  file { '/bin/netstat_a':
    source => 'puppet:///modules/adore_rootkit_static/netstat',
    mode   => '0755',
    owner   => 'root',
    group   => 'root',
    require => Package['libc6:i386'],
  }
  file { '/bin/ps_a':
    source => 'puppet:///modules/adore_rootkit_static/ps',
    mode   => '0755',
    owner   => 'root',
    group   => 'root',
    require => Package['libc6:i386'],
  }

}
