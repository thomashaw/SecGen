ensure_packages(['nginx'])

# Overwrite the distribution default landing page so the served content is
# predictable. /var/www/html is created by the nginx package.
file { '/var/www/html/index.html':
  ensure  => file,
  owner   => 'root',
  group   => 'root',
  mode    => '0644',
  content => "<!DOCTYPE html>\n<html>\n<head><title>Welcome to nginx!</title></head>\n<body>\n<h1>Welcome to nginx!</h1>\n<p>This is an nginx HTTP server.</p>\n</body>\n</html>\n",
  require => Package['nginx'],
}

service { 'nginx':
  ensure  => running,
  enable  => true,
  require => Package['nginx'],
}
