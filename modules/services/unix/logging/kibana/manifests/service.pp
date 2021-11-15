class kibana::service {
  service { 'kibana':
    enable  => true,
    hasrestart  => true,
  }
}
