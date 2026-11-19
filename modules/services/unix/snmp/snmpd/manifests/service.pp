# Enables snmpd at boot and keeps it running.
class snmpd::service {
  service { 'snmpd':
    ensure  => running,
    enable  => true,
    require => Class['snmpd::install'],
  }
}
