class thunderbird::configure {
  $secgen_params = secgen_functions::get_parameters($::base64_inputs_file)
  $accounts = $secgen_params['accounts']
  $autostart = str2bool($secgen_params['autostart'][0])
  $start_page = $secgen_params['start_page'][0]

  # Setup TB for each user account
  unless $accounts == undef {
    $accounts.each |$raw_account| {
      $account = parsejson($raw_account)
      $username = $account['username']
      unless $username == 'root' {
        notice("configuring thunderbird for username: $username")
        $user_id = inline_template("<%= require 'securerandom'; SecureRandom.alphanumeric(8) -%>")
        notice("generated user_id: $user_id")


        # add user profile
        file { "/home/$username/.thunderbird/":
          ensure  => directory,
          recurse => true,
          mode    => '0600',
          owner   => $username,
          group   => $username,
          source  => 'puppet:///modules/thunderbird/.thunderbird',
        } ->

        exec { 'set directory userid':
          command => "/bin/mv /home/$username/.thunderbird/sbge8oh9.default-default /home/$username/.thunderbird/$user_id.default-default"
        }

        file { "/home/$username/.thunderbird/$user_id.default-default/Mail/localhost/Inbox.msf":
          ensure  => directory,
          recurse => true,
          mode    => '0600',
          owner   => $username,
          group   => $username,
          content  => template('thunderbird/user.default-default/Mail/localhost/Inbox.msf.erb'),
          require => Exec['set directory userid']
        }

        file { "/home/$username/.thunderbird/$user_id.default-default/folderTree.json":
          ensure  => directory,
          recurse => true,
          mode    => '0600',
          owner   => $username,
          group   => $username,
          content  => template('thunderbird/user.default-default/folderTree.json.erb'),
          require => Exec['set directory userid']
        }

        file { "/home/$username/.thunderbird/$user_id.default-default/panacea.dat":
          ensure  => directory,
          recurse => true,
          mode    => '0600',
          owner   => $username,
          group   => $username,
          content  => template('thunderbird/user.default-default/panacea.dat.erb'),
          require => Exec['set directory userid']
        }

        file { "/home/$username/.thunderbird/$user_id.default-default/pkcs11.txt":
          ensure  => directory,
          recurse => true,
          mode    => '0600',
          owner   => $username,
          group   => $username,
          content  => template('thunderbird/user.default-default/pkcs11.txt.erb'),
          require => Exec['set directory userid']
        }

        file { "/home/$username/.thunderbird/$user_id.default-default/prefs.js":
          ensure  => directory,
          recurse => true,
          mode    => '0600',
          owner   => $username,
          group   => $username,
          content  => template('thunderbird/user.default-default/prefs.js.erb'),
          require => Exec['set directory userid']
        }

        file { "/home/$username/.thunderbird/$user_id.default-default/session.json":
          ensure  => directory,
          recurse => true,
          mode    => '0600',
          owner   => $username,
          group   => $username,
          content  => template('thunderbird/user.default-default/session.json.erb'),
          require => Exec['set directory userid']
        }

        # autostart script
        if $autostart {
          file { ["/home/$username/.config/", "/home/$username/.config/autostart/"]:
            ensure => directory,
            owner  => $username,
            group  => $username,
          }

          file { "/home/$username/.config/autostart/thunderbird.desktop":
            ensure => file,
            source => 'puppet:///modules/thunderbird/thunderbird.desktop',
            owner  => $username,
            group  => $username,
          }
        }
      }
    }
  }
}
