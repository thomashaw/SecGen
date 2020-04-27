class xfce4_term_w_records::init {

  if ($::osfamily == 'Debian' and $::lsbdistcodename == 'kali-rolling') or defined('xfce') {
    $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
    $accounts = $secgen_parameters['accounts']

    # If xfce is defined, we need to run AFTER the xfce4 and lightdm installation
    if defined('xfce') or ($::osfamily == 'Debian' and $::lsbdistcodename == 'kali-rolling') {
      file { '/root/.config/xfce4/terminal/terminalrc':
        ensure => present,
        source => 'puppet:///modules/xfce4_term_w_records/terminalrc',
        user   => 'root',
        group  => 'root',
      }

      if $accounts and defined('parameterised_accounts') {
        $accounts.each |$raw_account| {
          $account = parsejson($raw_account)
          $username = $account['username']
          unless $username == 'root' {
            file { "/home/$username/.config/xfce4/terminal/terminalrc":
              ensure  => present,
              source  => 'puppet:///modules/xfce4_term_w_records/terminalrc',
              user    => 'root',
              group   => 'root',
              require => Resource['parameterised_accounts::account'],
            }
          }
        }
      }
    }
  }
}
