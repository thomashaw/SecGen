class kibana::service {
  service { 'kibana':
    ensure     => running,
    enable     => true,
    hasrestart => true,
  }
}
