class hosts_legacy::config {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)

  $hosts = $secgen_parameters['hosts']
  $ip_addresses = $secgen_parameters['IP_addresses']
  if $hosts {
    $hosts.each |$index, $hostname| {
      host { $hostname:
        ip => $ip_addresses[$index],
      }
    }
  }
}
