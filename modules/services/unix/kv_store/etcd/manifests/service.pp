# Enables etcd at boot and keeps it running.
class etcd::service {
  service { 'etcd':
    ensure  => running,
    enable  => true,
    require => Class['etcd::install'],
  }
}
