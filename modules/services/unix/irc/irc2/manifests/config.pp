class irc2::config {
  $secgen_params = secgen_functions::get_parameters($::base64_inputs_file)
  $motd = $secgen_params['motd'][0]

  # Write the MOTD after the package so /etc/ircd/ exists; do not notify the
  # service — ircd races on rebinding 6667 if restarted during provisioning.
  file { '/etc/ircd/ircd.motd':
    ensure  => file,
    owner   => 'root',
    group   => 'irc',
    mode    => '0640',
    content => "${motd}\n",
    require => Package['ircd-irc2'],
  }
}
