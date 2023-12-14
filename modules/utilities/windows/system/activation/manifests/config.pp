class activation::config {
  # command to activate Windows during provisioning
  exec { 'activate_windows':
    command     => 'C:\\Windows\\System32\\cscript.exe //b C:\\Windows\\System32\\slmgr.vbs /ato',
    refreshonly => true,
  } ->

  # registry entry for RunOnce to rearm activation on first reboot (after network isolation)
  registry_value { 'run_once_slmgr_rearm':
    ensure => present,
    path    => 'HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Windows\CurrentVersion\RunOnce',
    type   => string,
    data   => 'C:\\Windows\\System32\\cscript.exe //b C:\\Windows\\System32\\slmgr.vbs /rearm',
  }
}
