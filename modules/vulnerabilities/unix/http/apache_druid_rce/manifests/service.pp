# Class: apache_druid_rce::service
# Service behaviour
#
class apache_druid_rce::service {
  file { '/etc/systemd/system/druid.service':
    source => 'puppet:///modules/apache_druid_rce/druid.service',
    owner  => 'root',
    mode   => '0777',
  }

  service { 'druid':
    ensure => running,
    enable => true,
  }
}
