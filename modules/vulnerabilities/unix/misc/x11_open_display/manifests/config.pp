# Opens the X server to the network and auto-logs in a Plasma session, then
# leaks a flag into the desktop user's home. Applied by SDDM after the
# post-build reboot; the display manager is not restarted during provisioning.
class x11_open_display::config {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $desktop_user     = $secgen_parameters['desktop_user'][0]
  $strings_to_leak  = $secgen_parameters['strings_to_leak']
  $leaked_filenames = $secgen_parameters['leaked_filenames']

  Exec { path => ['/bin', '/usr/bin', '/usr/local/bin', '/sbin', '/usr/sbin'] }

  # SDDM drop-in (main sddm.conf is left untouched). -listen tcp makes Xorg
  # accept TCP clients on 6000; -ac disables access control so no cookie is
  # needed; -s 0 -dpms stop the display blanking or locking the session.
  # Autologin starts the X11 Plasma session as the desktop user at boot.
  file { '/etc/sddm.conf.d':
    ensure => directory,
    mode   => '0755',
  } ->
  file { '/etc/sddm.conf.d/zz-x11-open-display.conf':
    ensure  => file,
    mode    => '0644',
    content => template('x11_open_display/sddm-x11-open.conf.erb'),
  }

  # Session hygiene so the autologged-in session stays interactive and nothing
  # steals focus from KRunner: no screen lock, no wallet prompt, no welcome tour.
  file { ["/home/${desktop_user}/.config", "/home/${desktop_user}/.config/autostart"]:
    ensure  => directory,
    owner   => $desktop_user,
    group   => $desktop_user,
    mode    => '0755',
    require => User[$desktop_user],
  }

  file { "/home/${desktop_user}/.config/kscreenlockerrc":
    ensure  => file,
    owner   => $desktop_user,
    group   => $desktop_user,
    mode    => '0644',
    content => "[Daemon]\nAutolock=false\nLockOnResume=false\n",
    require => File["/home/${desktop_user}/.config"],
  }

  file { "/home/${desktop_user}/.config/kwalletrc":
    ensure  => file,
    owner   => $desktop_user,
    group   => $desktop_user,
    mode    => '0644',
    content => "[Wallet]\nEnabled=false\n",
    require => File["/home/${desktop_user}/.config"],
  }

  file { "/home/${desktop_user}/.config/autostart/plasma-welcome.desktop":
    ensure  => file,
    owner   => $desktop_user,
    group   => $desktop_user,
    mode    => '0644',
    content => "[Desktop Entry]\nType=Application\nName=plasma-welcome\nHidden=true\n",
    require => File["/home/${desktop_user}/.config/autostart"],
  }

  # The flag, readable by the desktop user whose session the attacker drives.
  ::secgen_functions::leak_files { 'x11_open_display-home-leak':
    storage_directory => "/home/${desktop_user}/",
    strings_to_leak   => $strings_to_leak,
    leaked_filenames  => $leaked_filenames,
    owner             => $desktop_user,
    group             => $desktop_user,
    mode              => '0600',
    leaked_from       => "x11_open_display_${desktop_user}",
    require           => User[$desktop_user],
  }
}
