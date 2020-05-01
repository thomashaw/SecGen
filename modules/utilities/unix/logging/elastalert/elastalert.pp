unless defined('analysis_alert_action_server') {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $elasticsearch_ip = $secgen_parameters['elasticsearch_ip'][0]
  $elasticsearch_port = 0 + $secgen_parameters['elasticsearch_port'][0]

  class { 'elastalert::install':
    elasticsearch_ip => $elasticsearch_ip,
    elasticsearch_port => $elasticsearch_port,
  } ~>
  class {'elastalert::config':
    elasticsearch_ip => $elasticsearch_ip,
    elasticsearch_port => $elasticsearch_port,
  }~>
  class {'elastalert::service':
    elasticsearch_ip => $elasticsearch_ip,
    elasticsearch_port => $elasticsearch_port,
  }
}