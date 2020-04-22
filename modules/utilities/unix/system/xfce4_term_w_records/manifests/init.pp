class xfce4_term_w_records::init {

  if ($::osfamily == 'Debian' and $::lsbdistcodename == 'kali-rolling') or defined('xfce') {
    $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
    $accounts = $secgen_parameters['accounts']

    # TODO: refactor this module into something like 'system mail utils'
    # TODO: move me somewhere else into another class (xfce4_term_w_records::install)
    ensure_packages(['mutt', 'procmail'])

    # If xfce is defined, we need to run AFTER the xfce4 and lightdm installation
    if defined('xfce') {
      augeas { "xfce4_term_w_records-root":
        context    => '/root/.config/xfce4/terminal/terminalrc',
        changes => ["CommandUpdateRecords=TRUE",],
        require => [Package['xfce4'], Package['lightdm']]
      }

      if $accounts and defined('parameterised_accounts') {
        $accounts.each |$raw_account| {
          $account = parsejson($raw_account)
          $username = $account['username']
          unless $username == 'root' {
            augeas { "xfce4_term_w_records-$username":
              context    => "/home/$username/.config/xfce4/terminal/terminalrc",
              changes => ["CommandUpdateRecords=TRUE",],
              require => [Package['xfce4'], Package['lightdm'], Resource['parameterised_accounts::account']]
            }
          }
        }
      }
    } elsif $::osfamily == 'Debian' and $::lsbdistcodename == 'kali-rolling' {
      augeas { "xfce4_term_w_records-root":
        context => '/root/.config/xfce4/terminal/terminalrc',
        changes => ["CommandUpdateRecords=TRUE",],
      }

      if $accounts and defined('parameterised_accounts'){
        $accounts.each |$raw_account| {
          $account = parsejson($raw_account)
          $username = $account['username']
          unless $username == 'root' {
            augeas { "xfce4_term_w_records-$username":
              context => "/home/$username/.config/xfce4/terminal/terminalrc",
              changes => ["CommandUpdateRecords=TRUE",],
              require => Resource['parameterised_accounts::account'],
            }
          }
        }
      }
    }
  }
}