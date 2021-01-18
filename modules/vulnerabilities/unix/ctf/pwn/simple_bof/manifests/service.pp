class simple_bof::service {
  # If we've got a port supplied, host the binary over the network. Otherwise do nothing
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $port = 0 + $secgen_parameters['port'][0]



}