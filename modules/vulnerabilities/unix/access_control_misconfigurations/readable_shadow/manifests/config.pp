class readable_shadow::config {
  file { '/etc/shadow':
    ensure => present,
    mode   => '0644',
  }

  # Check if base64 inputs file is included as it's only generated when a string has been leaked from the scenario
  if $::base64_inputs_file {
    $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
    $strings_to_leak = $secgen_parameters['strings_to_leak']
    if $strings_to_leak and $strings_to_leak[0] {
      $shadow_flag = $strings_to_leak[0]
      exec { 'append_flag_to_etc_shadow':
        command => "/bin/echo '$shadow_flag' >> /etc/shadow"
      }
    }
  }
}