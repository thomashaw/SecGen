class metactf::install {
  $secgen_params = secgen_functions::get_parameters($::base64_inputs_file)
  $install_dir = '/opt/metactf'

  Exec { path => ['/bin', '/usr/bin', '/usr/local/bin', '/sbin', '/usr/sbin'] }

  ensure_packages('zsh')
  ensure_packages('build-essential')
  ensure_packages('gcc-multilib')
  # The challenges' build.zsh scripts derive their randomised values with bc. install.sh would install it, but its
  # apt line names python-pip, which no longer exists, so the whole line fails (on Kali, which lacks bc, challenges
  # then fail to compile or silently build with empty values).
  ensure_packages('bc')

  file { $install_dir:
    ensure  => directory,
    recurse => true,
    source  => 'puppet:///modules/metactf/repository',
  }

  exec { 'set install.sh mode':
    command => "chmod +x $install_dir/install.sh",
    logoutput => on_failure,
  }

  exec { 'install metactf dependencies':
    command => "/bin/bash $install_dir/install.sh",
    logoutput => on_failure,
  }

  # For now just build all of the binaries.
  exec { 'build src_angr binaries':
    cwd     => "$install_dir/src_angr/",
    logoutput => on_failure,
    command => "/usr/bin/make",
    require => Package['bc'],
  }

  # Build src_csp
  exec { 'src_csp chmod executable':
    command => 'chmod -R +x */*/*.zsh',
    cwd     => "$install_dir/src_csp/",
    logoutput => on_failure,
  }

  exec { 'build src_csp binaries':
    cwd     => "$install_dir/src_csp/",
    command => "/usr/bin/make",
    require => [Exec['src_csp chmod executable'], Package['bc']],
  }

  # Build src_malware
  exec { 'src_malware chmod executable':
    command => 'chmod -R +x */*/*.zsh',
    cwd     => "$install_dir/src_malware/",
    logoutput => on_failure,
  }

  # Build src_malware
  exec { 'build src_malware binaries':
    cwd     => "$install_dir/src_malware/",
    command => "/usr/bin/make",
    logoutput => true,
    require => [Exec['src_malware chmod executable'], Package['bc']],
  }

  # Build src_sse
  exec { 'src_sse chmod executable':
    command => 'chmod -R +x */*/*.zsh',
    cwd     => "$install_dir/src_sse/",
    logoutput => true,
  }

  # Build src_sse
  exec { 'build src_sse binaries':
    cwd     => "$install_dir/src_sse/",
    command => "/usr/bin/make",
    logoutput => true,
    require => [Exec['src_sse chmod executable'], Package['bc']],
  }

}
