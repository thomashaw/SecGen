class llm_relay::config {
  require llm_relay::install

  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $upstream_url     = regsubst($secgen_parameters['upstream_url'][0], '/+$', '')
  # secgen.rb --llm-api-key arrives as a fact (never written to the project); it wins over the api_key input
  if $::llm_api_key and $::llm_api_key != '' {
    $api_key = $::llm_api_key
  } else {
    $api_key = $secgen_parameters['api_key'][0]
  }
  $listen_port      = $secgen_parameters['listen_port'][0]
  $allowed_networks = $secgen_parameters['allowed_networks'].filter |$n| { $n != '' }

  # holds the API key: readable by root and nginx's master process only
  file { '/etc/nginx/conf.d/secgen_llm_relay.conf':
    ensure  => file,
    content => template('llm_relay/secgen_llm_relay.conf.erb'),
    mode    => '0600',
    owner   => 'root',
    group   => 'root',
    notify  => Service['nginx'],
  }
}
