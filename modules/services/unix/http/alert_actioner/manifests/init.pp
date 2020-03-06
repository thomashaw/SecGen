class alert_actioner::init {
  class { '::alert_actioner::install': }
  class { '::alert_actioner::config': }
}