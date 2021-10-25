class kibana_7::service {
  service { 'kibana':
    enable  => true,
    hasrestart  => true,
  }
}
