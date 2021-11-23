class readable_shadow::config {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $strings_to_leak = $secgen_parameters['strings_to_leak']

  file { '/etc/shadow':
    ensure  => present,
    mode    => '0644',
  }

  if $strings_to_leak and $strings_to_leak[0] {
    $shadow_flag = $strings_to_leak[0]
    exec{ 'append_flag_to_etc_shadow':
      command => "/bin/echo '$shadow_flag' >> /etc/shadow"
    }
  }
}
