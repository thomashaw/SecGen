# Creates the non-privileged desktop user that is auto-logged in, and installs
# xterm so a network attacker can open a terminal in the session via KRunner.
class x11_open_display::install {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $desktop_user     = $secgen_parameters['desktop_user'][0]
  $desktop_password = $secgen_parameters['desktop_password'][0]

  # xterm is what exploit/unix/x11/x11_keyboard_exec launches (TARGET 0); the KDE
  # base ships konsole but not xterm.
  ensure_packages(['xterm'])

  # Non-privileged, non-sudoer desktop account. managehome creates /home/$user.
  user { $desktop_user:
    ensure     => present,
    shell      => '/bin/bash',
    groups     => ['audio', 'video'],
    password   => pw_hash($desktop_password, 'SHA-512', 'secgenx11'),
    managehome => true,
    home       => "/home/${desktop_user}",
  }
}
