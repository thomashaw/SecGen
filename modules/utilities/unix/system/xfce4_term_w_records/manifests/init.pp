class xfce4_term_w_records::init {

  if ($::osfamily == 'Debian' and $::lsbdistcodename == 'kali-rolling') or defined('xfce') {
    $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
    $accounts = $secgen_parameters['accounts']

    # If xfce is defined, we need to run AFTER the xfce4 and lightdm installation
    if defined('xfce') {
      augeas { "xfce4_term_w_records-root":
        path    => '/root/.config/xfce4/terminal/terminalrc',
        line    => "CommandUpdateRecords=TRUE",
        require => [Package['xfce4'], Package['lightdm']]
      }

      if $accounts {
        $accounts.each |$raw_account| {
          $account = parsejson($raw_account)
          $username = $account['username']
          unless $username == 'root' {
            auegas { "xfce4_term_w_records-$username":
              path    => "/home/$username/.config/xfce4/terminal/terminalrc",
              line    => "CommandUpdateRecords=TRUE",
              require => [Package['xfce4'], Package['lightdm'], Resource['parameterised_accounts::account']]
            }
          }
        }
      }
    } else {
      augeas { "xfce4_term_w_records-root":
        path => '/root/.config/xfce4/terminal/terminalrc',
        line => "CommandUpdateRecords=TRUE",
      }

      if $accounts {
        $accounts.each |$raw_account| {
          $account = parsejson($raw_account)
          $username = $account['username']
          unless $username == 'root' {
            auegas { "xfce4_term_w_records-$username":
              path => "/home/$username/.config/xfce4/terminal/terminalrc",
              line => "CommandUpdateRecords=TRUE",
              require => Resource['parameterised_accounts::account'],
            }
          }
        }
      }
    }
  }
}