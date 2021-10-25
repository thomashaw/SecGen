class kibana_7::config (
  $elasticsearch_ip,
  $elasticsearch_port = '9200',
  $kibana_port = '5601',
) {

  Exec { path => ['/bin','/sbin','/usr/bin', '/usr/sbin'] }

  # Configure Kibana
  file { '/etc/kibana/kibana.yml':
    ensure => file,
    mode => '0660',
    owner => 'kibana',
    group => 'kibana',
    content => template('kibana_7/kibana.yml.erb')
  }

}
