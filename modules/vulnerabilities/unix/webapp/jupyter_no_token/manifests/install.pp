# Installs Jupyter Notebook from the Debian package and creates the
# unprivileged service account it runs as. The account's home is the notebook
# directory, so the kernel and the flag both live under a non-root user.
class jupyter_no_token::install {
  $user = 'jupyter'
  $home = '/home/jupyter'

  Exec { path => ['/bin', '/usr/bin', '/usr/local/bin', '/sbin', '/usr/sbin'] }

  group { $user:
    ensure => present,
  }

  # A normal, non-sudoer account with a real home. managehome creates the home;
  # a login shell lets a kernel-spawned shell behave normally.
  user { $user:
    ensure     => present,
    gid        => $user,
    home       => $home,
    managehome => true,
    shell      => '/bin/bash',
    require    => Group[$user],
  }

  # jupyter-notebook pulls in jupyter-core (the `jupyter` dispatcher) and
  # python3-notebook. The ExecStart calls /usr/bin/jupyter-notebook directly.
  ensure_packages(['jupyter-notebook'])

  # Config lives in the standard per-user location; create it before config.pp
  # writes the server config into it.
  file { "${home}/.jupyter":
    ensure  => directory,
    owner   => $user,
    group   => $user,
    mode    => '0755',
    require => User[$user],
  }
}
