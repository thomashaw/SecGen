# Enables tftpd-hpa at boot and keeps it running.
class tftpd_hpa::service {
  service { 'tftpd-hpa':
    ensure  => running,
    enable  => true,
    require => [Class['tftpd_hpa::install'], File['/etc/default/tftpd-hpa']],
  }
}
