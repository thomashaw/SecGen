class analysis_alert_action_client {
  class { '::analysis_alert_action_client::install': }
  class { '::analysis_alert_action_client::config': }
}