class reversing_tools::install {

  Exec { path => ['/bin', '/usr/bin', '/usr/local/bin', '/sbin', '/usr/sbin'] }
  ensure_packages(['gdb', 'git', 'ltrace', 'strace', 'valgrind', 'pax-utils', 'binwalk', 'vbindiff', 'ssdeep', 'gcc-multilib','yara'])

  # UPX: upx-ucl is not in the Debian 12 repos, so install the static UPX 4.2.4 release binary
  file { '/usr/local/bin/upx':
    ensure => file,
    source => 'puppet:///modules/reversing_tools/upx',
    mode   => '0755',
  }

  # pwntools, radare2 and GEF need Debian 12+ or Kali (older bases e.g. the Buster reversing CTFs lack the packages, gdb >= 10 and glibc >= 2.35)
  if $operatingsystem == 'Kali' or ($operatingsystem == 'Debian' and versioncmp($operatingsystemmajrelease, '12') >= 0) {

    # pwntools: packaged on both Debian 12 (4.9) and Kali
    ensure_packages(['python3-pwntools'])

    # radare2: not in the Debian 12 repos, so install the bundled upstream .deb (needs glibc >= 2.35, depends only on libc6).
    # Kali packages radare2 itself (and kali-tools-reverse-engineering may pull it in), so use apt there to avoid dpkg conflicts.
    if $operatingsystem == 'Kali' {
      ensure_packages(['radare2'])
    } else {
      file { '/opt/radare2_6.2.4_amd64.deb':
        ensure => file,
        source => 'puppet:///modules/reversing_tools/radare2_6.2.4_amd64.deb',
      }
      package { 'radare2':
        ensure   => installed,
        provider => dpkg,
        source   => '/opt/radare2_6.2.4_amd64.deb',
        require  => File['/opt/radare2_6.2.4_amd64.deb'],
      }
    }

    # GEF (2026.01, needs gdb >= 10 with python >= 3.10): loaded for every user via the system gdbinit shipped by the gdb package
    file { '/opt/gef':
      ensure => directory,
    }
    file { '/opt/gef/gef.py':
      ensure  => file,
      source  => 'puppet:///modules/reversing_tools/gef.py',
      mode    => '0644',
      require => File['/opt/gef'],
    }
    file_line { 'gdbinit load gef':
      path    => '/etc/gdb/gdbinit',
      line    => 'source /opt/gef/gef.py',
      require => [Package['gdb'], File['/opt/gef/gef.py']],
    }
  }

  # java
  ensure_packages(['procyon-decompiler'])

  # ensure ncat is installed for testing purposes
  ensure_packages("nmap")
  case $operatingsystemrelease {
    /^(1[0-9]).*/: { # do buster stuff
      ensure_packages("ncat")
    }
  }

  # # Install Cutter
  # $cutter_dir = '/opt/Cutter'
  # $cutter_appimage_url = 'https://github.com/radareorg/cutter/releases/download/v1.7.2/Cutter-v1.7.2-x86_64.Linux.AppImage'
  # $cutter_filename = 'Cutter-v1.7.2-x86_64.Linux.AppImage'
  # file { $cutter_dir:
  #   ensure => directory,
  # }
  #
  # # Download image
  # exec { 'download cutter appimage':
  #   command => "/usr/bin/wget -q $cutter_appimage_url -O $cutter_dir/$cutter_filename",
  #   cwd => $cutter_dir,
  #   require => File[$cutter_dir],
  # }
  #
  # exec { 'chmod cutter':
  #   command => "/bin/chmod +x $cutter_dir/$cutter_filename",
  #   cwd => $cutter_dir,
  #   require => Exec['download cutter appimage'],
  # }
  #
  # exec { 'install cutter':
  #   command => "/usr/bin/install $cutter_dir/$cutter_filename /usr/bin/cutter",
  #   cwd => $cutter_dir,
  #   require => Exec['download cutter appimage'],
  # }


  # TODO: Fix me
  # Install angr
  # exec { 'clone angr-dev repo':
  #   command => 'git clone https://github.com/angr/angr-dev',
  #   cwd     => '/usr/share/'
  # }
  #
  # exec { 'run angr-dev setup.sh':
  #   command   => '/bin/bash /usr/share/angr-dev/setup.sh -i -e angr-dev',
  #   cwd       => '/usr/share/angr-dev',
  #   logoutput => true,
  #   loglevel => info,
  #   timeout   => 0,
  #   require => Exec['clone angr-dev repo'],
  # }


  # TODO: Test all this!
  #
  # if $accounts {
  #   $accounts.each |$raw_account| {
  #     $account = parsejson($raw_account)
  #     $username = $account['username']
  #     notice ("Enabling angr virtualenv for account: [$username]")
  #
  #     $home_dir = "/home/$username"
  #
  #     exec { "$username-angr-workon-env-append":
  #       command => "echo \"export WORKON_ENV=/.virtualenvs\" >> $home_dir/.bashrc",
  #       require => Exec['run angr-dev setup.sh'],
  #     }
  #
  #     file { "$home_dir/angr-instructions.txt":
  #       content => 'The angr binary-analysis framework has been installed within a python virtual environment.
  #
  #       Run `workon angr-dev` to use the virtualenv.
  #
  #       If this fails, try adding adding the environment variable first by running `export WORKON_DEV=/.virtualenvs`'
  #     }
  #   }
  # }

  # Install packer detection tool? (e.g. Detect It Easy) (TODO)
  # Install AFL?(TODO)
  # Install Driller?(TODO)
  # Install Qira? (TODO)
}
