class php::install {

  ensure_packages('ca-certificates', { ensure => 'latest'})
  ensure_packages('apt-transport-https', { ensure => 'latest'})

  exec { 'install php5 gpg key':
    command => '/usr/bin/wget --no-check-certificate -O /etc/apt/trusted.gpg.d/php.gpg https://packages.sury.org/php/apt.gpg',
  } ->

  exec { 'add repo to sources':
    command =>
      '/bin/echo "deb http://172.33.0.44/packages.sury.org/php/ $(lsb_release -sc) main" > /etc/apt/sources.list.d/php.list',
  } ->

  exec { 'apt update':
    command => '/usr/bin/apt-get update',
  } ->

  package { ['php5.6', 'php5.6-mysql','php5.6-cli', 'php5.6-common', 'php5.6-curl', 'php5.6-mbstring','php5.6-xml']:
    ensure => installed,
  } ->

  package { 'php7.0-common':
    ensure => 'absent',
  }
  # ->
  #
  # exec { 'disable php7':
  #   command => '/usr/sbin/a2dismod php7.0',
  #   require => [Exec['install php5 gpg key'], Exec['add repo to sources']],
  # }
}
