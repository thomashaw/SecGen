class analysis_alert_action_server::install (
  $install_path = '/opt/alert_actioner/'
) {

  ensure_packages(['libcurl4-openssl-dev', 'libxml2-dev', 'ruby-dev', 'sshpass', 'mailutils'])

  package {  'ovirt-engine-sdk':
    ensure => '4.3.0',
    provider => 'gem',
    require => Package['ruby-dev']
  }

  file { $install_path:
    ensure  => directory,
    recurse => true,
    source  => 'puppet:///modules/analysis_alert_action_server/alert_actioner',
  }

}