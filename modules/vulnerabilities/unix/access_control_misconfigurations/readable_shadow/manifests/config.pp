class readable_shadow::config {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $shadow_flag = $strings_to_leak[0]

  file { '/etc/shadow':
    ensure  => present,
    mode    => '0644',
  }

  if $shadow_flag {
    exec{ 'append_flag_to_etc_shadow':
      command => "/bin/echo '$shadow_flag' >> /etc/shadow"
    }
  }
}
