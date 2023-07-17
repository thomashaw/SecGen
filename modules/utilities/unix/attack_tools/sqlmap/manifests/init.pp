class sqlmap (
  $installdir = '/usr/share/sqlmap',
  $source = 'https://github.com/sqlmapproject/sqlmap.git',
  $path = '/usr/local/bin',
  $revision = 'HEAD',
) {

  # Install Git package
  ensure_packages(['git'])

  # Create directory to install into
  file { $installdir:
    ensure => directory,
  }

  # Clone sqlmap from Github using 'exec' resource
  exec { 'clone_sqlmap':
    command => "git clone ${source} ${installdir}",
    creates => "${installdir}/sqlmap.py",
    require => [File[$installdir], Package['git']],
  }

  # Symlink the main script into a bin dir
  file { "${path}/sqlmap":
    ensure  => link,
    target  => "${installdir}/sqlmap.py",
    require => Exec['clone_sqlmap'],
  }
}
