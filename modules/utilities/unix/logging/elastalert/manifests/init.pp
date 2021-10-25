class elastalert ($elasticsearch_ip, $elasticsearch_port) {
  class { 'elastalert::install':
    elasticsearch_ip => $elasticsearch_ip,
    elasticsearch_port => $elasticsearch_port,
  }
  ~>
  class {'elastalert::config':
    elasticsearch_ip => $elasticsearch_ip,
    elasticsearch_port => $elasticsearch_port,
  }~>
  class {'elastalert::service':
    elasticsearch_ip => $elasticsearch_ip,
    elasticsearch_port => $elasticsearch_port,
  }
}