# Installs the LAMP stack and unpacks the bundled Pandora FMS 7.0NG.742 tarballs.
# The tarballs are treated as data: Puppet copies and untars them, nothing in
# them is executed at install time.
class pandora_fms_rce::install {
  Exec { path => ['/bin', '/usr/bin', '/usr/local/bin', '/sbin', '/usr/sbin'] }

  # php-xmlrpc is not packaged for PHP 8 (Debian 12); the console does not need it to serve pages.
  ensure_packages(['apache2', 'libapache2-mod-php', 'php', 'php-cli', 'php-common', 'php-mysql', 'php-gd',
    'php-curl', 'php-mbstring', 'php-xml', 'php-zip', 'php-bcmath', 'php-gmp', 'php-snmp', 'php-ldap',
    'php-sqlite3', 'php-pear', 'mariadb-server', 'graphviz', 'unzip', 'snmp'])

  $console_package = 'pandorafms_console-7.0NG.742.tar.gz'
  $server_package  = 'pandorafms_server-7.0NG.742.tar.gz'
  $agent_package   = 'pandorafms_agent_unix-7.0NG.742.tar.gz'

  file { "/usr/local/src/${console_package}":
    ensure => file,
    source => "puppet:///modules/pandora_fms_rce/${console_package}",
  }
  file { "/usr/local/src/${server_package}":
    ensure => file,
    source => "puppet:///modules/pandora_fms_rce/${server_package}",
  }
  file { "/usr/local/src/${agent_package}":
    ensure => file,
    source => "puppet:///modules/pandora_fms_rce/${agent_package}",
  }

  # Console goes to /var/www/html/pandora_console (the Metasploit module's default TARGETURI)
  exec { 'unpack-pandora-console':
    command => "tar -xzf /usr/local/src/${console_package} -C /var/www/html/",
    creates => '/var/www/html/pandora_console/index.php',
    timeout => 600,
    require => [Package['apache2'], File["/usr/local/src/${console_package}"]],
  }

  # Pandora 7.0NG.742 (2019) targets PHP 7; Debian 12 ships PHP 8.2, which removed
  # PHP4-style constructors (a method named after its class) entirely. The console's
  # bundled PHP-gettext library (include/streams.php, include/gettext.php - used by
  # every page via set_user_language()) uses that pattern in exactly 4 places, with no
  # other instances anywhere else in the console codebase (confirmed by a full-tree
  # scan). Patched here rather than downgrading system PHP, which would affect every
  # other PHP module composed into the same VM.
  exec { 'patch-pandora-php8-gettext-ctors-streams':
    command  => 'sed -i -E "s/function (StringReader|FileReader|CachedFileReader)\(/function __construct(/; s/parent::StringReader\(\)/parent::__construct()/" /var/www/html/pandora_console/include/streams.php',
    provider => shell,
    unless   => 'grep -q "function __construct" /var/www/html/pandora_console/include/streams.php',
    require  => Exec['unpack-pandora-console'],
  }
  exec { 'patch-pandora-php8-gettext-ctors-gettext':
    command  => 'sed -i "s/function gettext_reader(/function __construct(/" /var/www/html/pandora_console/include/gettext.php',
    provider => shell,
    unless   => 'grep -q "function __construct" /var/www/html/pandora_console/include/gettext.php',
    require  => Exec['unpack-pandora-console'],
  }

  # Server and agent are unpacked for completeness but not installed: only the
  # console (the vulnerable component) is needed to serve the application.
  file { '/opt/pandorafms':
    ensure => directory,
  }
  exec { 'unpack-pandora-server':
    command => "tar -xzf /usr/local/src/${server_package} -C /opt/pandorafms/",
    creates => '/opt/pandorafms/pandora_server',
    require => [File['/opt/pandorafms'], File["/usr/local/src/${server_package}"]],
  }
  exec { 'unpack-pandora-agent':
    command => "tar -xzf /usr/local/src/${agent_package} -C /opt/pandorafms/",
    creates => '/opt/pandorafms/pandora_agent',
    require => [File['/opt/pandorafms'], File["/usr/local/src/${agent_package}"]],
  }
}
