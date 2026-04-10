#
# apache::fastcgi::server { 'php':
#   host       => '127.0.0.1:9000',
#   timeout    => 15,
#   flush      => false,
#   faux_path  => '/var/www/php.fcgi',
#   fcgi_alias => '/php.fcgi',
#   file_type  => 'application/x-httpd-php'
# }
#
# apache::vhost { 'www':
# custom_fragment => 'AddType application/x-httpd-php .php',
#   docroot => '/var/www/wordpress'
# }

class { 'apache':
  mpm_module => 'prefork'
}

exec { 'apache2-systemd-reload':
  command     => 'systemctl daemon-reload; systemctl enable apache2',
  path        => [ '/usr/bin', '/bin', '/usr/sbin' ],
}
#->
#service { 'apache2':
#  ensure   => running,
#  provider => systemd,
#  enable   => true,
#}
