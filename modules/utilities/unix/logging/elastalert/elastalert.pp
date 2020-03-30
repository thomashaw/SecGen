unless defined('analysis_alert_action_server') {
  include elastalert::install
  include elastalert::config
  include elastalert::service
}