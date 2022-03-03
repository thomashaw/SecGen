class {'suid_root_view::change_uid_permissions':
  file_input => {
    '/usr/bin/view' => '4777',
    '/etc/alternatives/view' => '4777'
  }
}
