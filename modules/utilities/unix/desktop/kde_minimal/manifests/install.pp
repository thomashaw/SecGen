class kde_minimal::install{
  case $operatingsystem {
    'Debian': {
      # ksnapshot was removed after Debian 10 (buster); spectacle is its KDE Plasma 5 replacement
      if versioncmp($facts['os']['release']['major'], '11') >= 0 {
        $screenshot_tool = 'spectacle'
      } else {
        $screenshot_tool = 'ksnapshot'
      }
      package { ['kde-plasma-desktop', 'kate', $screenshot_tool, 'qtcurve']:
        ensure => 'installed',
      }
    }
  }
}
