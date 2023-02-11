# Class: jenkins_cli::configure
# Configuration and extras for Jenkins cli
#
class jenkins_cli::configure {
  $leaked_filenames = ['flagtest'] ##$secgen_parameters['leaked_filenames']
  $strings_to_leak = ['this is a list of strings that are secrets / flags','another secret'] ##$secgen_parameters['strings_to_leak']

  Exec { path => ['/bin', '/usr/bin', '/usr/local/bin', '/sbin', '/usr/sbin'] }

  $user = 'yong'#$secgen_parameters['leaked_username'][0]
  $user_home = "/home/${user}"

  ::secgen_functions::leak_files { 'jenkins-flag-leak':
    storage_directory => $user_home,
    leaked_filenames  => $leaked_filenames,
    strings_to_leak   => $strings_to_leak,
    owner             => 'root',
    mode              => '0750',
    leaked_from       => 'jenkins_cli',
  }
}
