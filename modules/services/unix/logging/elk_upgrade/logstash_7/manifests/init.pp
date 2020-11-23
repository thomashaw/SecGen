class logstash_7() {

  Exec { path => ['/bin','/sbin','/usr/bin', '/usr/sbin'] }

  ## Add logstash repository

  # exec { 'add apt repository':
  #   command => 'wget -qO - https://artifacts.elastic.co/GPG-KEY-elasticsearch | sudo apt-key add -'
  # }

  # exec { 'logstash update apt':
  #
  # }
  # package { '':
  #
  # }
  #
  ## Install logstash

  ## Configure logstash

}