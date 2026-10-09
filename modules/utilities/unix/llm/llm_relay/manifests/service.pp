class llm_relay::service {
  require llm_relay::config

  service { 'nginx':
    ensure => running,
    enable => true,
  }
}
