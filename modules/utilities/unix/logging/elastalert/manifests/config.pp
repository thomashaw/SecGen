class elastalert::config ($elasticsearch_ip,
                          $elasticsearch_port,
                          $installdir = '/opt/elastalert/',
                          $source='http://github.com/Yelp/elastalert',
                          $rules_dir = '/opt/elastalert/rules/') {
  file { '/opt/elastalert/config.yaml':
    ensure => file,
    content => template('elastalert/config.yaml.erb'),
    require => File[$installdir],
  }

  # Load the rules
  file { $rules_dir:
    ensure => directory,
    recurse => true,
    source => 'puppet:///modules/elastalert/rules/',
    require => File[$installdir],
  }

  # Move the custom alerter (outputs rulename:alert)

  file { ['/opt/elastalert/elastalert/', '/opt/elastalert/elastalert/modules/', '/opt/elastalert/elastalert/modules/alerter/']:
    ensure => directory,
  }

  file { ['/opt/elastalert/elastalert/modules/__init__.py','/opt/elastalert/elastalert/modules/alerter/__init__.py']:
    ensure => file,
    require => File['/opt/elastalert/elastalert/modules/alerter/'],
  }

  file { '/opt/elastalert/elastalert/modules/alerter/exec.py':
    ensure => file,
    source => 'puppet:///modules/elastalert/exec_alerter.py',
    require => File['/opt/elastalert/elastalert/modules/alerter/'],
  }

}