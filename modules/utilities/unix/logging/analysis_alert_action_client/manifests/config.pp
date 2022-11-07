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

  ::secgen_functions::create_directory { "create_/home/challenger/.config/":
    path => '/home/challenger/.config/',
    notify => File["/home/challenger/.config/konsole.notifyrc"],
  }

  file { "/home/challenger/.config/konsole.notifyrc":
    ensure  => file,
    source  => 'puppet:///modules/analysis_alert_action_client/kde_config/konsole.notifyrc',
    owner   => 'challenger',
    group   => 'challenger',
    notify => File["/home/challenger/.config/kwrited.notifyrc"],
  }

  file { "/home/challenger/.config/kwrited.notifyrc":
    ensure  => file,
    source  => 'puppet:///modules/analysis_alert_action_client/kde_config/kwrited.notifyrc',
    owner   => 'challenger',
    group   => 'challenger',
  }

}