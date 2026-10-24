# == Class: php2shell_rce::service
# Start the built-in PHP server serving the practice target.
class php2shell_rce::service {
  require php2shell_rce::install

  service { 'php2shell-target':
    ensure => running,
    enable => true,
  }
}
