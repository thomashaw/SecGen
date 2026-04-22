class xfce4_term_w_records::init {

  if ($::osfamily == 'Debian' and $::lsbdistcodename == 'kali-rolling') or defined('xfce') {
    $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
    $accounts = $secgen_parameters['accounts']

    ensure_packages(['mailutils', 'libnotify-bin', 'notify-osd'])

    file { ['/root/.config/xfce4/', '/root/.config/xfce4/terminal/']:
      ensure => directory,
    }

    file { '/root/.config/xfce4/terminal/terminalrc':
      ensure  => present,
      source  => 'puppet:///modules/xfce4_term_w_records/terminalrc',
      owner   => 'root',
      group   => 'root',
      require => [File['/root/.config/xfce4/'], File['/root/.config/xfce4/terminal/'], ],
    }

    if $accounts and defined('parameterised_accounts') {
      $accounts.each |$raw_account| {
        $account = parsejson($raw_account)
        $username = $account['username']
        unless $username == 'root' {
          file { "/home/$username/.config/xfce4/terminal/terminalrc":
            ensure  => present,
            source  => 'puppet:///modules/xfce4_term_w_records/terminalrc',
            owner   => $username,
            group   => $username,
            require => [
              File['/root/.config/xfce4/'],
              File['/root/.config/xfce4/terminal/'],
              Resource['parameterised_accounts::account']
            ],
          }
        }
      }
    }
  }
}
