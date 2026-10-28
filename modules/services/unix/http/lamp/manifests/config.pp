# Apache and MySQL are installed by the required modules (services/unix/http/apache
# and database/mysql on Debian, the *_kali_compatible pair on Kali). Enable PHP in
# that Apache and install the PHP MySQL extension so PHP scripts can talk to the
# database.
#
# SecGen applies each module in its own Puppet run, so the apache class (and its
# apache::mod resources and variables) isn't in this catalog, and declaring it here
# would rewrite the config the apache module set up. Debian's libapache2-mod-php
# metapackage pulls in mod_php for whatever PHP version the base ships (7.3 on
# buster, 8.2 on bookworm, Kali's current one) and its postinst switches Apache to
# the prefork MPM and enables the module; the exec makes sure it is enabled, then
# Apache is restarted to load it.
class lamp::config {
  ensure_packages(['libapache2-mod-php', 'php-mysql'])

  exec { 'lamp: enable mod_php':
    command => "/bin/sh -c 'a2dismod -q mpm_event mpm_worker; a2enmod -q mpm_prefork \$(basename -s .load /etc/apache2/mods-available/php*.load)'",
    path    => ['/usr/sbin', '/usr/bin', '/sbin', '/bin'],
    unless  => "/bin/sh -c 'ls /etc/apache2/mods-enabled/php*.load >/dev/null 2>&1'",
    require => Package['libapache2-mod-php'],
    notify  => Exec['lamp: restart apache'],
  }

  exec { 'lamp: restart apache':
    command     => 'systemctl restart apache2',
    path        => ['/usr/sbin', '/usr/bin', '/sbin', '/bin'],
    refreshonly => true,
    subscribe   => Package['libapache2-mod-php', 'php-mysql'],
  }
}
