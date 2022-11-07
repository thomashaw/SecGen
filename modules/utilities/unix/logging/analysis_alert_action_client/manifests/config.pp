class analysis_alert_action_client::config {
  augeas { "sshd_permit_root":
    context => "/files/etc/ssh/sshd_config",
    changes => [
      "set PermitRootLogin yes",
    ],
  }


  # TODO:

  # For each user, add the dot files disabling double notifications:

  ## Pull the user data from secgen_params (from the usernames of recipients?)
  ## Determine the home directory
  ## Move the file from files -> ~/.config/<filename>
  ## Make sure to set the owner and permissions, username:username with -rw-------


  # WIP:

  # Testing hard-coded, then parameterise

  exec  { "create_directory_chall_conf":
    path => '/bin:/sbin:/usr/bin:/usr/sbin',
    command => "mkdir -p challenger:challenger /home/challenger/.config/"
  }

  file { "/home/challenger/.config/konsole.notifyrc":
    ensure  => file,
    source  => 'puppet:///modules/analysis_alert_action_client/kde_config/konsole.notifyrc',
    owner   => 'challenger',
    group   => 'challenger',
    require => Exec['create_directory_chall_conf']
  }

  file { "/home/challenger/.config/kwrited.notifyrc":
    ensure  => file,
    source  => 'puppet:///modules/analysis_alert_action_client/kde_config/kwrited.notifyrc',
    owner   => 'challenger',
    group   => 'challenger',
    require => Exec['create_directory_chall_conf']
  }

}