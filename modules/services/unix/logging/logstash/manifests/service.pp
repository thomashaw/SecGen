class logstash::service {
  service { 'logstash':
    enable  => true,
    hasrestart  => true,
  }
}
