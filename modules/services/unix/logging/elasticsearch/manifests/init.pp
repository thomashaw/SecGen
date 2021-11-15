class elasticsearch (
  $api_host,
  $api_port,
  $package_url = 'https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-7.10.0-amd64.deb',
) {

  Exec { path => ['/bin','/sbin','/usr/bin', '/usr/sbin'] }

  class { 'elasticsearch::install':
    package_url => $package_url,
  }->
  class { 'elasticsearch::config':
    elasticsearch_ip => $api_host,
    elasticsearch_port => $api_port,
  }->
  class { 'elasticsearch::service': }

}
