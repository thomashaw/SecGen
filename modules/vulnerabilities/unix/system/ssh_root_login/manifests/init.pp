class ssh_root_login::init {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $strings_to_leak = $secgen_parameters['strings_to_leak']
  $leaked_filenames = $secgen_parameters['leaked_filenames']
  $root_password = $secgen_parameters['root_password'][0]

  package { "openssh-server":
    ensure => "installed",
  }

  package { "sshpass":
    ensure => "installed",
  }

  service { "ssh":
    ensure    => running,
    hasstatus => true,
    require   => Package["openssh-server"],
  }

  file_line { 'sshd_config_permitrootlogin':
    path               => '/etc/ssh/sshd_config',
    line               => 'PermitRootLogin yes',
    match              => '^PermitRootLogin',
    replace            => true,
    append_on_no_match => true,
  }

  user { 'root':
    ensure => present,
    password => pw_hash($root_password, 'SHA-512', 'mysalt'),
  }

  ::secgen_functions::leak_files { "root-file-leak":
    storage_directory => "/root/",
    leaked_filenames  => $leaked_filenames,
    strings_to_leak   => $strings_to_leak,
    owner             => root,
    group             => root,
    mode              => '0600',
    leaked_from       => "accounts_root",
  }
}
