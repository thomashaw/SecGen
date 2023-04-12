class apache_couchdb::configure {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $database = 'couchdb'
  $user = $secgen_parameters['leaked_username'][0]
  $password = $secgen_parameters['leaked_password'][0]
  $jsondb = 'sampledata' ##TODO secgen
  $strings_to_leak = $secgen_parameters['strings_to_leak'][0]
  $leaked_filenames = $secgen_parameters['leaked_filenames'][0]
  $user_home = "/home/${user}"

  Exec { path => ['/bin', '/usr/bin', '/usr/local/bin', '/sbin', '/usr/sbin'] }

  #create database 
  #exec { 'create-database':
  #	command => "curl -X PUT http://localhost:34023/${database} -u \"${username}:${password}\"",
  #	logoutput => true
  #}->
  #exec { 'import_data':
  #  cwd=> '/usr/bin/',
  #	command => "curl -d @${jsondb}.json -H \"Content-type: application/json\" -X POST http://127.0.0.1:34023/${database}/_bulk_docs -u \"${username}:${password}\"",
  #	logoutput => true
  #}

  ::secgen_functions::leak_files { 'couchdb-flag-leak':
    storage_directory => $user_home,
    leaked_filenames  => $leaked_filenames,
    strings_to_leak   => $strings_to_leak,
    owner             => $user,
    mode              => '0644',
    leaked_from       => 'apache_couchdb',
  }
}
