class elastalert::config {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $elasticsearch_ip = $secgen_parameters['elasticsearch_ip'][0]
  $elasticsearch_port = 0 + $secgen_parameters['elasticsearch_port'][0]
  $elastalert_dir = '/opt/elastalert/'
  $rules_dir = '/opt/elastalert/rules/'

  file { $elastalert_dir:
    ensure => directory,
  }

  file { '/opt/elastalert/config.yaml':
    ensure => file,
    content => template('elastalert/config.yaml.erb'),
    require => File[$elastalert_dir],
  }

  file { $rules_dir:
    ensure => directory,
    recurse => true,
    source => 'puppet:///modules/elastalert/rules/',
    require => File[$elastalert_dir],
  }
}