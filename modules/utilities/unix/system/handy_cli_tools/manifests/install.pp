class handy_cli_tools::install{
  package { ['vim.tiny', 'vim', 'rsync', 'psmisc', 'curl', 'sudo', 'libnotify-bin']:
    ensure => 'installed',
  }
}
