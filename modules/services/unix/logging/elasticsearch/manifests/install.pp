class elasticsearch::install (
  String $package_url  = 'https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-7.10.0-amd64.deb',
  String $version = '7.10.0',
) {

  Exec { path => ['/bin','/sbin','/usr/bin', '/usr/sbin'] }

  $key_url = secgen_functions::mirror_url('https://artifacts.elastic.co/GPG-KEY-elasticsearch')
  $repo_url = secgen_functions::mirror_url('https://artifacts.elastic.co/packages/7.x/apt')

  # apt-key and sudo are not reliably present on Debian bookworm: use a
  # dearmored keyring and signed-by instead
  ensure_packages(['wget', 'gnupg', 'apt-transport-https', 'ca-certificates'])

  # Install Elasticsearch
  exec { 'es add gpg key':
    command => "wget -qO - ${key_url} | gpg --batch --yes --dearmor -o /usr/share/keyrings/elasticsearch-keyring.gpg",
    creates => '/usr/share/keyrings/elasticsearch-keyring.gpg',
    require => Package['wget', 'gnupg', 'ca-certificates'],
  }->
  file { '/etc/apt/sources.list.d/elastic-7.x.list':
    ensure  => file,
    content => "deb [signed-by=/usr/share/keyrings/elasticsearch-keyring.gpg] ${repo_url} stable main\n",
  }->
  exec { 'es update apt':
    command => 'apt-get update',
  }->
  # pinned: the 7.x repository otherwise installs the latest 7.17.x
  package { 'elasticsearch':
    ensure => $version,
  }
}
