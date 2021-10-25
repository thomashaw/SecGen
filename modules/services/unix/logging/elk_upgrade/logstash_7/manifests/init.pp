class logstash_7 ($elasticsearch_ip, $elasticsearch_port = '9200', $logstash_port = '5044') {

  class { 'logstash_7::install': } ->
  class { 'logstash_7::config':
    elasticsearch_ip   => $elasticsearch_ip,
    elasticsearch_port => $elasticsearch_port,
    logstash_port      => $logstash_port,
  } ->
  class { 'logstash_7::service': }

}