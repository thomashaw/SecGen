class analysis_alert_action_server::install (
  $install_path = '/opt/alert_actioner/'
) {

  ensure_packages(['libcurl4-openssl-dev', 'libxml2-dev', 'ruby-dev', 'sshpass', 'mailutils', 'postgresql', 'postgresql-contrib', 'libpq-dev'])

  package {  'pg':
    provider => 'gem',
    ensure   => '1.5.9',
    require => [Package['ruby-dev'], Package['postgresql'], Package['postgresql-contrib'], Package['libpq-dev']]
  }

   package { 'nokogiri':
     provider => 'gem',
     ensure   => '1.12.5',
     require  => [Package['ruby-dev']]
   }

    package { 'process_helper':
      provider => 'gem',
      ensure   => present,
      require  => [Package['ruby-dev']]
    }

  file { $install_path:
    ensure  => directory,
    recurse => true,
    source  => 'puppet:///modules/analysis_alert_action_server/alert_actioner',
    owner => 'aaa_admin',   # TODO: parameterise me into aaa_config
    group => 'aaa_admin',  # TODO: parameterise me into aaa_config
    # TODO: add mode
  }

}