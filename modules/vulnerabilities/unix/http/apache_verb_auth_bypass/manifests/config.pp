# Serves a Basic-Auth-protected directory whose access control is written with
# <Limit GET POST> rather than <LimitExcept GET POST>. A <Limit> block only
# covers the verbs it names (and the HEAD that Apache folds into GET), so any
# other method reaches the resource unauthenticated. apache is declared here
# (each SecGen module is its own Puppet catalog, so the required apache service
# module's resources are not visible from this run).
class apache_verb_auth_bypass::config {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $strings_to_leak   = $secgen_parameters['strings_to_leak']
  $leaked_filenames  = $secgen_parameters['leaked_filenames']
  $username          = $secgen_parameters['unix_username'][0]
  $password          = $secgen_parameters['used_password'][0]

  $docroot       = '/var/www/html'
  $protected_dir = "${docroot}/private"
  $htpasswd      = '/etc/apache2/.htpasswd'
  $auth_realm    = 'Restricted'
  $secret_file   = "${protected_dir}/${leaked_filenames[0]}"

  Exec { path => ['/bin', '/usr/bin', '/sbin', '/usr/sbin'] }

  # trace_enable => 'On' (the module default, set explicitly) is what makes the
  # bypass observable: TRACE is not in <Limit GET POST>, so it reaches the
  # resource with a 200 where GET is challenged with a 401.
  class { '::apache':
    default_vhost => false,
    trace_enable  => 'On',
  }

  # priority 05 keeps this vhost ahead of any default vhost a co-selected apache
  # module registers at priority 15 (see the Apache module's secgen_test notes).
  apache::vhost { 'verb_auth_bypass':
    priority => '05',
    port     => 80,
    docroot  => $docroot,
  }

  file { $protected_dir:
    ensure  => directory,
    owner   => 'www-data',
    group   => 'www-data',
    mode    => '0755',
    require => Package['httpd'],
  }

  # The secret only meant to be reachable with valid Basic Auth credentials.
  file { $secret_file:
    ensure  => file,
    content => $strings_to_leak[0],
    owner   => 'www-data',
    group   => 'www-data',
    mode    => '0644',
    require => File[$protected_dir],
  }

  # htpasswd lives in apache2-utils (an apache2 recommends, not a hard dep).
  ensure_packages(['apache2-utils'])

  # Basic Auth credentials. The password is passed through an environment
  # variable so shell metacharacters in it cannot break the command.
  exec { 'verb_auth_bypass-htpasswd':
    command     => "htpasswd -bc ${htpasswd} ${username} \"\$VAB_PW\"",
    environment => ["VAB_PW=${password}"],
    provider    => shell,
    creates     => $htpasswd,
    logoutput   => false,
    require     => [Package['httpd'], Package['apache2-utils']],
  }

  file { $htpasswd:
    ensure  => file,
    owner   => 'root',
    group   => 'www-data',
    mode    => '0640',
    require => Exec['verb_auth_bypass-htpasswd'],
  }

  # The misconfigured access control. <Limit GET POST> authenticates only those
  # verbs; everything else (TRACE, arbitrary methods) is served unchallenged.
  apache::custom_config { 'verb_auth_bypass':
    content => template('apache_verb_auth_bypass/verb_auth_bypass.conf.erb'),
    require => [File[$protected_dir], File[$htpasswd], File[$secret_file]],
  }
}
