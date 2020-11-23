class kibana_7 () {

  Exec { path => ['/bin','/sbin','/usr/bin', '/usr/sbin'] }

  ## Add kibana repository

  # exec { 'add apt repository':
  #   command => 'wget -qO - https://artifacts.elastic.co/GPG-KEY-elasticsearch | sudo apt-key add -'
  # }

  # exec { 'kibana update apt':
  #
  # }
  # package { '':
  #
  # }


  ## Install kibana

  ## Configure kibana
}