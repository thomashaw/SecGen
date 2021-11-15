class logstash($elasticsearch_ip, $elasticsearch_port = '9200', $logstash_port = '5044') {

  class { 'logstash::install': } ->
  class { 'logstash::config':
    elasticsearch_ip   => $elasticsearch_ip,
    elasticsearch_port => $elasticsearch_port,
    logstash_port      => $logstash_port,
  } ->
  class { 'logstash::service': }

}