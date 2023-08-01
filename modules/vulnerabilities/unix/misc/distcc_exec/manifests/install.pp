class distcc_exec::install{
  package { 'distcc':
    ensure => installed,
    install_options => ['--allow-unauthenticated'],
  }
}
