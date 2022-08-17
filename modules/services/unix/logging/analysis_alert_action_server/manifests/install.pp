class analysis_alert_action_server::install (
  $install_path = '/opt/alert_actioner/'
) {

  ensure_packages(['libcurl4-openssl-dev', 'libxml2-dev', 'ruby-dev', 'sshpass', 'mailutils', 'postgresql', 'postgresql-contrib', 'libpq-dev'])

  package {  'pg':
    provider => 'gem',
    require => [Package['ruby-dev'], Package['postgresql'], Package['postgresql-contrib'], Package['libpq-dev']]
  }


  package { 'activesupport':
    provider => 'gem',
    ensure => '6.1.6.1'
  }

  package { 'gci':
    provider => 'gem',
    require => Package['activesupport']
  }

  # TODO: Needed for VDI actioners (later)
  # package {  'ovirt-engine-sdk':
  #   ensure => '4.3.0',
  #   provider => 'gem',
  #   require => Package['ruby-dev']
  # }

  file { $install_path:
    ensure  => directory,
    recurse => true,
    source  => 'puppet:///modules/analysis_alert_action_server/alert_actioner',
    owner => 'aaa_admin',   # TODO: parameterise me into aaa_config
    group => 'aaa_admin',  # TODO: parameterise me into aaa_config
    # TODO: add mode
  }

}