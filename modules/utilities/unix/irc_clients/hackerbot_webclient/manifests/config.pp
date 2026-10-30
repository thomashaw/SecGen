class hackerbot_webclient::config {
  require hackerbot_webclient::install

  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $username       = $secgen_parameters['username'][0]
  $irc_server_ip  = $secgen_parameters['irc_server_ip'][0]
  $hackerbot_nick = $secgen_parameters['hackerbot_nick'][0]

  file { '/opt/hackerbot_webclient/index.html':
    ensure  => file,
    source  => 'puppet:///modules/hackerbot_webclient/index.html',
    owner   => 'root',
    group   => 'root',
    mode    => '0644',
    require => File['/opt/hackerbot_webclient'],
  }

  file { '/opt/hackerbot_webclient/app.js':
    ensure  => file,
    source  => 'puppet:///modules/hackerbot_webclient/app.js',
    owner   => 'root',
    group   => 'root',
    mode    => '0644',
    require => File['/opt/hackerbot_webclient'],
  }

  # The only templated file: bakes in the per-lab nick/target/server values.
  # index.html and app.js are static and identical across every deployment.
  file { '/opt/hackerbot_webclient/config.js':
    ensure  => file,
    content => template('hackerbot_webclient/config.js.erb'),
    owner   => 'root',
    group   => 'root',
    mode    => '0644',
    require => File['/opt/hackerbot_webclient'],
  }

  # DEV ONLY (RSA lab manual test, branch rsa-lab-dev): serves the walkthrough, a copy of the lab sheet and the
  # automated tester at http://hackerbot:8080/. Never merge these resources or their files.
  file { '/opt/hackerbot_webclient/manual_test.html':
    ensure  => file,
    source  => 'puppet:///modules/hackerbot_webclient/manual_test.html',
    owner   => 'root',
    group   => 'root',
    mode    => '0644',
    require => File['/opt/hackerbot_webclient'],
  }

  file { '/opt/hackerbot_webclient/lab_sheet.html':
    ensure  => file,
    source  => 'puppet:///modules/hackerbot_webclient/lab_sheet.html',
    owner   => 'root',
    group   => 'root',
    mode    => '0644',
    require => File['/opt/hackerbot_webclient'],
  }

  file { '/opt/hackerbot_webclient/rsa_lab_test.py':
    ensure  => file,
    source  => 'puppet:///modules/hackerbot_webclient/rsa_lab_test.py',
    owner   => 'root',
    group   => 'root',
    mode    => '0644',
    require => File['/opt/hackerbot_webclient'],
  }

  file { '/opt/hackerbot_webclient/hackerbot_irc.py':
    ensure  => file,
    source  => 'puppet:///modules/hackerbot_webclient/hackerbot_irc.py',
    owner   => 'root',
    group   => 'root',
    mode    => '0644',
    require => File['/opt/hackerbot_webclient'],
  }

  file { '/opt/hackerbot_webclient/7_asymmetric_enc_rsa.md':
    ensure  => file,
    source  => 'puppet:///modules/hackerbot_webclient/7_asymmetric_enc_rsa.md',
    owner   => 'root',
    group   => 'root',
    mode    => '0644',
    require => File['/opt/hackerbot_webclient'],
  }
}
