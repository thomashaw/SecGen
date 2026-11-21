# Installs the Debian rsync package (client and daemon).
class rsyncd::install {
  ensure_packages(['rsync'])
}
