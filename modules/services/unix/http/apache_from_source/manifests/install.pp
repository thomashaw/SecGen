class apache_from_source::install {

  Exec { path => ['/bin', '/usr/bin', '/usr/local/bin', '/sbin', '/usr/sbin'], }

  $httpd = 'httpd-2.4.23'
  $archive = "$httpd.tar.gz"
  $apr_archive = 'apr-1.5.2.tar.gz'
  $apr_util_archive = 'apr-util-1.5.4.tar.gz'

  file { "/usr/local/src/$archive":
    ensure => present,
    source => "puppet:///modules/apache_from_source/$archive",
  }

  exec { 'unpack-apache':
    cwd     => '/usr/local/src/',
    command => "tar -xzf $archive",
  }

  file { "/usr/local/src/$apr_archive":
    ensure => present,
    source => "puppet:///modules/apache_from_source/$apr_archive",
  }

  exec { 'unpack-apr':
    cwd     => '/usr/local/src/',
    command => "tar -xzf $apr_archive -C $httpd/srclib/; mv $httpd/srclib/apr-1.5.2 $httpd/srclib/apr",
  }

  file { "/usr/local/src/$apr_util_archive":
    ensure => present,
    source => "puppet:///modules/apache_from_source/$apr_util_archive",
  }

  exec { 'unpack-apr-util':
    cwd     => '/usr/local/src/',
    command => "tar -xzf $apr_util_archive -C $httpd/srclib/; mv $httpd/srclib/apr-util-1.5.4 $httpd/srclib/apr-util",
  }

  package { ['libpcre3','libpcre3-dev', 'zlib1g-dev', 'libldap2-dev']:
    ensure => installed,
  }

  exec { 'configure-apache':
    cwd     => "/usr/local/src/$httpd",
    command => 'bash configure --with-pcre=/usr \
                --enable-mpms-shared=all \
                --enable-unixd=static \
                --enable-layout=Debian --enable-so \
                --with-program-name=apache2  \
                --with-ldap=yes --with-ldap-include=/usr/include \
                --with-ldap-lib=/usr/lib \
                --with-suexec-caller=www-data \
                --with-suexec-bin=/usr/lib/apache2/suexec \
                --with-suexec-docroot=/var/www \
                --with-suexec-userdir=public_html \
                --with-suexec-logfile=/var/log/apache2/suexec.log \
                --with-suexec-uidmin=100 \
                --enable-suexec=shared \
                --enable-log-config=static --enable-logio=static \
                --enable-version=static \
                --with-pcre=yes \
                --enable-pie \
                --enable-authn-alias=shared --enable-authnz-ldap=shared  \
                --enable-disk-cache=shared --enable-cache=shared \
                --enable-mem-cache=shared --enable-file-cache=shared \
                --enable-cern-meta=shared --enable-dumpio=shared --enable-ext-filter=shared \
                --enable-charset-lite=shared --enable-cgi=shared \
                --enable-dav-lock=shared --enable-log-forensic=shared \
                --enable-ldap=shared --enable-proxy=shared \
                --enable-proxy-connect=shared --enable-proxy-ftp=shared \
                --enable-proxy-http=shared --enable-proxy-ajp=shared \
                --enable-proxy-scgi=shared \
                --enable-proxy-balancer=shared --enable-ssl=shared \
                --enable-authn-dbm=shared --enable-authn-anon=shared \
                --enable-authn-dbd=shared --enable-authn-file=shared \
                --enable-authn-default=shared --enable-authz-host=shared \
                --enable-authz-groupfile=shared --enable-authz-user=shared \
                --enable-authz-dbm=shared --enable-authz-owner=shared \
                --enable-authnz-ldap=shared --enable-authz-default=shared \
                --enable-auth-basic=shared --enable-auth-digest=shared \
                --enable-dbd=shared --enable-deflate=shared \
                --enable-include=shared --enable-filter=shared \
                --enable-env=shared --enable-mime-magic=shared \
                --enable-expires=shared --enable-headers=shared \
                --enable-ident=shared --enable-usertrack=shared \
                --enable-unique-id=shared --enable-setenvif=shared \
                --enable-status=shared \
                --enable-autoindex=shared --enable-asis=shared \
                --enable-info=shared --enable-cgid=shared \
                --enable-dav=shared --enable-dav-fs=shared \
                --enable-vhost-alias=shared --enable-negotiation=shared \
                --enable-dir=shared --enable-imagemap=shared \
                --enable-actions=shared --enable-speling=shared \
                --enable-userdir=shared --enable-alias=shared \
                --enable-rewrite=shared --enable-mime=shared \
                --enable-substitute=shared  --enable-reqtimeout=shared',
  }
  #
  # exec { 'make-apache':
  #   cwd     => "/usr/local/src/$httpd",
  #   command => 'make',
  # }
  #
  # exec { 'make-install-apache':
  #   cwd     => "/usr/local/src/$httpd",
  #   command => 'make install',
  # }


  file { '/tmp/httpd_2.4.23-1_i386.deb':
    source => 'puppet:///modules/apache_from_source/httpd_2.4.23-1_i386.deb',
    ensure => file,
  }

  package { 'httpd_2.4.23-1_i386.deb':
    ensure => installed,
    provider => dpkg,
    source => '/tmp/httpd_2.4.23-1_i386.deb',
  }

  file { ['/etc/apache2/','/etc/apache2/mods-enabled/','/etc/apache2/mods-available/','/etc/apache2/sites-enabled/', '/etc/apache2/sites-available/','/etc/apache2/conf.d/']:
    ensure => directory,
  }

  file { ['/etc/apache2/mods-enabled/dummy.conf','/etc/apache2/mods-enabled/dummy.load','/etc/apache2/ports.conf','/etc/apache2/conf.d/dummy.conf','/etc/apache2/conf.d/dummy.load']:
    ensure  => present,
    content => "",
  }

  file { '/tmp/apache2.conf':
    ensure => file,
    source => 'puppet:///modules/apache_from_source/apache2.conf',
  }

  file { '/etc/apache2/envvars':
    ensure => file,
    source => 'puppet:///modules/apache_from_source/envvars',
  }

  file { '/etc/init.d/apache2':
    ensure  => file,
    owner   => root,
    group   => root,
    mode    => '0755',
    source  => 'puppet:///modules/apache_from_source/apache2.init.d',
  }

}
