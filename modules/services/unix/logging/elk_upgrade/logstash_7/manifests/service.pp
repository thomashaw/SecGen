class logstash_7::service {
  service { 'logstash':
    enable  => true,
    hasrestart  => true,
  }
}
