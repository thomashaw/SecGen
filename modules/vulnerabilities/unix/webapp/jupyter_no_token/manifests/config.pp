# Writes the Jupyter server config that disables authentication and binds to all
# interfaces, and leaks the flag into the notebook directory (the service user's
# home) so it is reachable through the contents API or a kernel.
class jupyter_no_token::config {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $port              = $secgen_parameters['port'][0]
  $strings_to_leak   = $secgen_parameters['strings_to_leak']
  $leaked_filenames  = $secgen_parameters['leaked_filenames']

  $user = 'jupyter'
  $home = '/home/jupyter'

  file { "${home}/.jupyter/jupyter_notebook_config.py":
    ensure  => file,
    owner   => $user,
    group   => $user,
    mode    => '0644',
    content => template('jupyter_no_token/jupyter_notebook_config.py.erb'),
    require => Class['jupyter_no_token::install'],
  }

  # A plausible working notebook so the server looks like one a data team uses,
  # rather than a directory holding only the flag. It never auto-runs; it just
  # makes the box realistic and the Jupyter UI obviously identifiable.
  file { "${home}/analysis.ipynb":
    ensure  => file,
    owner   => $user,
    group   => $user,
    mode    => '0644',
    content => template('jupyter_no_token/analysis.ipynb.erb'),
    require => Class['jupyter_no_token::install'],
  }

  # The flag sits in the notebook directory itself, so browsing the unauthenticated
  # server exposes it.
  ::secgen_functions::leak_files { 'jupyter_no_token_flag':
    storage_directory => $home,
    leaked_filenames  => $leaked_filenames,
    strings_to_leak   => $strings_to_leak,
    leaked_from       => 'jupyter_no_token',
    owner             => $user,
    group             => $user,
    mode              => '0644',
    require           => Class['jupyter_no_token::install'],
  }
}
