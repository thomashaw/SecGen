class kibana_7 ($elasticsearch_ip, $elasticsearch_port = '9200', $kibana_port = '5601') {

  class { 'kibana_7::install': }->
  class { 'kibana_7::config':
    elasticsearch_ip => $elasticsearch_ip,
    elasticsearch_port => $elasticsearch_port,
    kibana_port => $kibana_port
  } ->
  class { 'kibana_7::service': }

}