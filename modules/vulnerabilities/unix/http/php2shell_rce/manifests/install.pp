# == Class: php2shell_rce::install
#
# Serves a trivial PHP page from the unpatched PHP 7.4 (php7_vulnerable) using
# PHP's built-in server, and leaks a flag to capture after code execution. The
# page is deliberately minimal: the vulnerability is in the interpreter, not the
# page.

class php2shell_rce::install {

  Exec { path => ['/bin', '/usr/bin', '/usr/local/bin', '/sbin', '/usr/sbin'] }

  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $port             = $secgen_parameters['port'][0]
  $strings_to_leak  = $secgen_parameters['strings_to_leak']
  $leaked_filenames = $secgen_parameters['leaked_filenames']

  $docroot = '/var/www/html'
  $php_bin = '/opt/php-7.4-vulnerable/bin/php'

  file { ['/var/www', $docroot]:
    ensure => directory,
    mode   => '0755',
  }
  -> file { "${docroot}/index.php":
    ensure => file,
    source => 'puppet:///modules/php2shell_rce/index.php',
    mode   => '0644',
  }

  # A flag to capture once code execution is achieved.
  ::secgen_functions::leak_files { 'php2shell-flag-leak':
    storage_directory => '/root',
    leaked_filenames  => $leaked_filenames,
    strings_to_leak   => $strings_to_leak,
    owner             => 'root',
    group             => 'root',
    mode              => '0600',
    leaked_from       => 'php2shell_rce',
  }

  # Minimal web server: PHP's built-in server on the vulnerable interpreter.
  file { '/etc/systemd/system/php2shell-target.service':
    ensure  => file,
    mode    => '0644',
    content => "[Unit]\nDescription=php2shell practice target\nAfter=network.target\n\n[Service]\nExecStart=${php_bin} -S 0.0.0.0:${port} -t ${docroot}\nRestart=always\n\n[Install]\nWantedBy=multi-user.target\n",
    require => File["${docroot}/index.php"],
  }
  ~> exec { 'php2shell-target systemd daemon-reload':
    command     => '/bin/systemctl daemon-reload',
    refreshonly => true,
  }
}
