class analysis_alert_action_client::config {
  augeas { "sshd_permit_root":
    context => "/files/etc/ssh/sshd_config",
    changes => [
      "set PermitRootLogin yes",
    ],
  }
}