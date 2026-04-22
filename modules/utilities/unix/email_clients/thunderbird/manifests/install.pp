class thunderbird::install{
  ensure_packages(['mailutils', 'libnotify-bin', 'notify-osd'])

  package { 'thunderbird':
    ensure => 'installed',
  }
}
