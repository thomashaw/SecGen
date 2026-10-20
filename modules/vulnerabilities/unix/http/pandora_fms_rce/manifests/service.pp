class pandora_fms_rce::service {
  Exec { path => ['/bin', '/usr/bin', '/usr/local/bin', '/sbin', '/usr/sbin'] }

  exec { 'enable-apache-pandora':
    command => 'systemctl enable apache2',
  }
  -> exec { 'restart-apache-pandora':
    command => 'systemctl restart apache2',
  }
}
