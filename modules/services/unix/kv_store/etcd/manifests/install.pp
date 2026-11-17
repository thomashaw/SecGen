# Installs the Debian etcd server and etcdctl client.
class etcd::install {
  ensure_packages(['etcd-server', 'etcd-client'])
}
