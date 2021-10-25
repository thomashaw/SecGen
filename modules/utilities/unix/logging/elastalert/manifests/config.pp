class elastalert::config ($elasticsearch_ip,
                          $elasticsearch_port,
                          $installdir = '/opt/elastalert/',
                          $source='http://github.com/Yelp/elastalert',
                          $rules_dir = '/opt/elastalert/rules',
                          $tmp_rules_dir = '/opt/elastalert/tmp_rules') { # TODO: change this to automatically copy once we've got all rules working
  file { '/opt/elastalert/config.yaml':
    ensure => file,
    content => template('elastalert/config.yaml.erb'),
    require => File[$installdir],
  }

  file { $rules_dir:
    ensure => directory,
  }

  # Load the rules
  # TODO: (Remove me after dev) Manually copy the rules we've
  # TODO: (Remove me after dev) update me to $rules_dir once we've got full working rules
  file { $tmp_rules_dir:
    ensure => directory,
    recurse => true,
    source => 'puppet:///modules/elastalert/rules/',
    require => File[$installdir],
  }

  # TODO: (Remove me after dev) Currently manually copies complete rules into the correct rules directory
  exec { 'tmp copy working ea rules':
    command => "/bin/cp $tmp_rules_dir/*rf* $rules_dir/.",
    require => [File[$tmp_rules_dir], File[$rules_dir]]
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