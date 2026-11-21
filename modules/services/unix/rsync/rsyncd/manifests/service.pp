# Enables the rsync daemon at boot and keeps it running.
class rsyncd::service {
  service { 'rsync':
    ensure  => running,
    enable  => true,
    require => [Class['rsyncd::install'], Class['rsyncd::config']],
  }
}
