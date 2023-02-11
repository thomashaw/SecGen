# Class: lucee_rce::service
# Service behaviour
#
class lucee_rce::service {
  file { '/etc/systemd/system/lucee.service':
    source => 'puppet:///modules/lucee_rce/lucee.service',
    owner  => 'root',
    mode   => '0777',
  }

  service { 'lucee':
    ensure => 'running',
    enable => 'true',
  }
}
