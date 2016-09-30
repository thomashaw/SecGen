# include apache
#
# apache::vhost { 'ssl.example.com':
#   port    => '443',
#   docroot => '/var/www/ssl',
#   ssl     => true,
# }

file { '/tmp/httpd-2.4.23.tar.gz':
  ensure => present,
  source => 'puppet:///modules/apache_ssl/httpd-2.4.23.tar.gz',
}