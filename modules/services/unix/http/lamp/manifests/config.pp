# Apache and MySQL are installed by the required modules (services/unix/http/apache
# and database/mysql on Debian, the *_kali_compatible pair on Kali). Enable the PHP
# module for Apache and install the PHP MySQL extension so PHP scripts can talk to
# the database.
#
# The PHP version comes from the lamp_php_version fact (the candidate of the `php`
# package), with a fallback by release. apache::mod::php can't be used for PHP 8:
# the bundled puppetlabs-apache 2.0.0 (services/unix/http/apache) defaults to PHP
# 7.0 and would load libphp8.x.so as php8_module, and 7.0.0 (apache_kali_compatible)
# expects Red Hat's libphp.so; Debian's PHP 8 module is libphp8.x.so with id
# php_module. So PHP 8 is loaded with apache::mod directly, with the SetHandler
# conf that apache::mod::php would otherwise write.
class lamp::config {
  $fallback = $facts['os']['release']['major'] ? {
    '10'    => '7.3',
    '11'    => '7.4',
    '12'    => '8.2',
    '13'    => '8.4',
    default => '8.2',
  }
  $php_version = $facts['lamp_php_version'] ? {
    undef   => $fallback,
    ''      => $fallback,
    default => $facts['lamp_php_version'],
  }

  if versioncmp($php_version, '8') < 0 {
    class { '::apache::mod::php':
      php_version  => $php_version,
      package_name => "libapache2-mod-php${php_version}",
    }
  } else {
    $mod = "php${php_version}"
    ::apache::mod { $mod:
      package => "libapache2-mod-php${php_version}",
      lib     => "libphp${php_version}.so",
      id      => 'php_module',
    }
    file { "${mod}.conf":
      ensure  => file,
      path    => "${::apache::mod_dir}/${mod}.conf",
      content => "<FilesMatch \".+\\.ph(ar|p|tml)\$\">\n    SetHandler application/x-httpd-php\n</FilesMatch>\n",
      require => Exec["mkdir ${::apache::mod_dir}"],
      before  => File[$::apache::mod_dir],
      notify  => Class['apache::service'],
    }
    include ::apache::mod::dir
  }

  ensure_packages(['php-mysql'])
}
