class elastalert::config {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $elasticsearch_ip = $secgen_parameters['elasticsearch_ip'][0]
  $elasticsearch_port = 0 + $secgen_parameters['elasticsearch_port'][0]
  $elastalert_dir = '/opt/elastalert/'
  $rules_dir = '/opt/elastalert/rules/'

  file { '/opt/elastalert/config.yaml':
    ensure => file,
    content => template('elastalert/config.yaml.erb'),
    require => File[$elastalert_dir],
  }

  # Load the rules
  file { $rules_dir:
    ensure => directory,
    recurse => true,
    source => 'puppet:///modules/elastalert/rules/',
    require => File[$elastalert_dir],
  }

  # Move the custom alerter (outputs rulename:alert)

  file { '/opt/elastalert/elastalert_modules':
    ensure => directory,
  }

  file { '/opt/elastalert/elastalert_modules/exec_alerter.py':
    ensure => file,
    source => 'puppet:///modules/elastalert/exec_alerter.py',
    require => File['/opt/elastalert/elastalert_modules'],
  }

}