class ldap_server::install (
  String $domain = 'example.com',
  String $organization = 'Example Organization',
  String $admin_password = 'temp123',
) {
  # Pre-seed debconf values to make slapd installation non-interactive
  # This prevents prompts during package installation
  exec { 'preseed-slapd':
    command => "/bin/echo \"slapd slapd/internal/generated_adminpw password ${admin_password}\" | debconf-set-selections && \
                /bin/echo \"slapd slapd/internal/adminpw password ${admin_password}\" | debconf-set-selections && \
                /bin/echo \"slapd slapd/password2 password ${admin_password}\" | debconf-set-selections && \
                /bin/echo \"slapd slapd/password1 password ${admin_password}\" | debconf-set-selections && \
                /bin/echo \"slapd slapd/domain string ${domain}\" | debconf-set-selections && \
                /bin/echo \"slapd shared/organization string ${organization}\" | debconf-set-selections && \
                /bin/echo 'slapd slapd/backend string MDB' | debconf-set-selections && \
                /bin/echo 'slapd slapd/purge_database boolean true' | debconf-set-selections && \
                /bin/echo 'slapd slapd/move_old_database boolean true' | debconf-set-selections && \
                /bin/echo 'slapd slapd/allow_ldap_v2 boolean false' | debconf-set-selections && \
                /bin/echo 'slapd slapd/no_configuration boolean false' | debconf-set-selections",
    unless  => '/usr/bin/dpkg -l | grep -q "^ii  slapd"',
    path    => ['/bin', '/usr/bin'],
  } ->
  # OpenLDAP Server and Utilities
  # Standalone LDAP daemon for serving directory information
  package { 'slapd':
    ensure  => installed,
  } ->
  # Ensure slapd service is running
  service { 'slapd':
    ensure  => running,
    enable  => true,
  }

  # LDAP command-line utilities for server management
  # Provides ldapsearch, ldapadd, ldapmodify, ldapdelete, etc.
  ensure_packages(['ldap-utils'])

  # phpLDAPadmin - Web-based LDAP administration interface
  # Provides a GUI for managing LDAP directory via Apache
  # This will automatically pull in php, php-ldap, php-xml, and libapache2-mod-php
  package { 'phpldapadmin':
    ensure => installed,
  }
  ->
  # Enable PHP module in Apache (version-agnostic)
  # Uses find to locate the installed PHP module and enables it
  exec { 'enable-php-module':
    command => '/bin/sh -c "/usr/bin/find /etc/apache2/mods-available -name php*.load -exec basename {} .load \; | /usr/bin/head -1 | /usr/bin/xargs /usr/sbin/a2enmod"',
    path    => ['/bin', '/usr/bin', '/usr/sbin'],
  }
  ->
  # Enable phpldapadmin Apache configuration
  exec { 'enable-phpldapadmin-conf':
    command => '/usr/sbin/a2enconf phpldapadmin',
    path    => ['/bin', '/usr/bin', '/usr/sbin'],
  }
  ->
  # Restart Apache to apply configuration changes
  exec { 'restart-apache2-for-phpldapadmin':
    command => '/usr/sbin/service apache2 restart',
    path    => ['/bin', '/usr/bin', '/usr/sbin'],
  }
}
