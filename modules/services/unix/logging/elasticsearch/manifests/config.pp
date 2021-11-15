class elasticsearch::config (
  $elasticsearch_ip,
  $elasticsearch_port = '9200',
  $node_name = 'my_es_node',
  $log_path = '/var/log/elasticsearch',
  $data_path = '/var/lib/elasticsearch',
) {

  Exec { path => ['/bin','/sbin','/usr/bin', '/usr/sbin'] }

  # Configure Elasticsearch
  file { '/etc/elasticsearch/elasticsearch.yml':
    ensure => file,
    mode => '0644',
    owner => 'root',
    group => 'elasticsearch',
    content => template('elasticsearch_7/elasticsearch.yml.erb')
  }

}
