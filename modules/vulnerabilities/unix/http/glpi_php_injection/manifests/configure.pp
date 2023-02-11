# Class: glpi_php_injection::configure
# Configuration for glpi/ Secgen
#
class glpi_php_injection::configure {
  $leaked_filenames = ['flagtest']
  $strings_to_leak = ['this is a list of strings that are secrets / flags','another secret'] ##$secgen_parameters['strings_to_leak']
  $known_username = 'admin' ##$secgen_parameters['known_username'][0]
  $known_password = 'password' ##$secgen_parameters['known_password'][0]
  $strings_to_pre_leak =  ['The username is admin', 'The password is password'] ##$secgen_parameters['strings_to_pre_leak']
  $web_pre_leak_filename = 'TODO' ##$secgen_parameters['web_pre_leak_filename'][0]

  ::secgen_functions::leak_files { 'glpi-flag-leak':
    storage_directory => '/var/www/html/glpi/',
    leaked_filenames  => $leaked_filenames,
    strings_to_leak   => $strings_to_leak,
    owner             => 'www-data',
    mode              => '0750',
    leaked_from       => 'glpi_php_injection',
  }
}
