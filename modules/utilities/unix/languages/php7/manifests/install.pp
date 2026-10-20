# == Class: php7::install
#
# Installs PHP 7.4 from the deb.sury.org third-party apt repository and makes
# it the default `php` CLI binary, for modules whose exploit needs PHP 7
# behaviour that PHP 8 changed (e.g. Debian 12's stock PHP 8.2 rejects a
# trailing `?>` inside eval(), which PHP 7 tolerated).
#
# This is ADDITIONAL software, not a downgrade: php8.2 (and anything that
# depends on the 'php'/'libapache2-mod-php' metapackages) is never installed
# by this module and is left alone if something else installed it first.
# Making /usr/bin/php resolve to 7.4 system-wide is the significant,
# behaviour-changing part, which is why this module ships a <conflict> in its
# secgen_metadata.xml against every other module known (at the time of
# writing) to manage a PHP version on this host -- see that file for the
# full list and the residual-risk note repeated below.
#
# What this module does NOT do:
#   - It does not touch Apache. Enabling PHP 7.4 as Apache's active PHP
#     handler (`a2enmod php7.4`) is left to the consuming module, because
#     Apache is not guaranteed to be installed yet when this module
#     provisions (SecGen runs <requires> dependencies, i.e. this module,
#     before the module that requires it) -- apt will pull in apache2 as a
#     dependency of libapache2-mod-php7.4 if apache2 is already selected
#     to install later in the *same* module's catalog, but not across
#     different modules' separate catalogs/provision runs. The consumer
#     (e.g. gravcms) installs apache2 itself and runs a2enmod php7.4 once
#     apache2 exists.
#   - It does not catch a module that calls `ensure_packages(['php'])` (or
#     similar) directly, without declaring itself in this module's conflict
#     list and without declaring <software_name>php</software_name> in its
#     own metadata. SecGen has no single "this needs PHP 8" marker to key
#     off; the conflict list in secgen_metadata.xml is a best-effort,
#     point-in-time enumeration of the webapp/http vulnerability (and lamp
#     service) modules that currently install PHP, not a guarantee. A new
#     PHP-dependent module added later, or any module installing PHP from
#     files/ or a different package name this list doesn't anticipate, will
#     not be excluded and could end up composed onto the same system as this
#     module with PHP silently switched to 7.4 under it.
#
class php7::install {

  Exec { path => ['/bin', '/usr/bin', '/usr/local/bin', '/sbin', '/usr/sbin'] }

  ensure_packages(['ca-certificates', 'apt-transport-https', 'lsb-release'], { ensure => 'latest' })

  # deb.sury.org (Ondrej Sury's PHP PPA-equivalent for Debian/Ubuntu) ships
  # every still-maintained PHP branch for the current stable release. Keyed
  # by `lsb_release -sc` (the release codename), which is why this module's
  # secgen_metadata.xml <requires>s the debian_bookworm base specifically.
  exec { 'install php7 (sury) apt gpg key':
    command => '/usr/bin/wget --no-check-certificate -O /etc/apt/trusted.gpg.d/php7-sury.gpg https://packages.sury.org/php/apt.gpg',
    creates => '/etc/apt/trusted.gpg.d/php7-sury.gpg',
  } ->
  exec { 'add php7 (sury) apt repo':
    command => "/bin/echo \"deb ${secgen_functions::mirror_url('https://packages.sury.org/php/')} $(lsb_release -sc) main\" > /etc/apt/sources.list.d/php7-sury.list",
    creates => '/etc/apt/sources.list.d/php7-sury.list',
  } ->
  exec { 'apt update for php7 (sury) repo':
    command => '/usr/bin/apt-get update',
  } ->
  package { [
    'php7.4',
    'php7.4-cli',
    'php7.4-common',
    'php7.4-mbstring',
    'php7.4-curl',
    'php7.4-xml',
    'php7.4-zip',
    'php7.4-gd',
    'libapache2-mod-php7.4',
  ]:
    ensure => installed,
  } ->
  # This is the critical fix for e.g. the gravcms (CVE-2021-21425) exploit:
  # /usr/bin/php is what its implanted scheduler cron job runs, over
  # update-alternatives. Idempotent: only re-points the alternative if it
  # isn't already 7.4 (so repeat provisioning runs don't fight any later,
  # manual change).
  exec { 'make php7.4 the default php CLI (update-alternatives)':
    command => '/usr/bin/update-alternatives --set php /usr/bin/php7.4',
    unless  => '/usr/bin/test "$(readlink -f /usr/bin/php)" = "/usr/bin/php7.4"',
  }
}
