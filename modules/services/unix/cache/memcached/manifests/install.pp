# Installs the Debian memcached server.
class memcached::install {
  ensure_packages(['memcached'])
}
