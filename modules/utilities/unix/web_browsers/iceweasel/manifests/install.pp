class iceweasel::install{
  # Debian renamed Iceweasel back to Firefox (ESR) in stretch (9), and current Debian
  # and Kali releases no longer have the transitional iceweasel package
  if $facts['os']['name'] == 'Debian' and versioncmp($facts['os']['release']['major'], '9') < 0 {
    $browser_package = 'iceweasel'
  } else {
    $browser_package = 'firefox-esr'
  }
  package { $browser_package:
    ensure => 'installed',
  }
}
