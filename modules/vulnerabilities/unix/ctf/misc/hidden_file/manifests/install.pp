class hidden_file::install{
  $secgen_params = secgen_functions::get_parameters($::base64_inputs_file)
  $leaked_filename = $secgen_params['leaked_filenames'][0]
  $strings_to_leak = $secgen_params['strings_to_leak']
  $default_path = '/'

  # Populate account and storage_directory
  if $secgen_params['account'] and $secgen_params['account'][0]{
    $account = parsejson($secgen_params['account'][0])
  } else{
    $account = undef
  }

  if $secgen_params['storage_directory'] and $secgen_params['storage_directory'][0] {
    $storage_directory = $secgen_params['storage_directory'][0]
  } else {
    $storage_directory = undef
  }

  # Determine if storage_dir is used, if not use the account information
  if $storage_directory and $account {
    fail('ERROR: provide either a storage_directory or account, but not both.')
  }

  if $storage_directory {
    $directory = $storage_directory
    $username = 'root'
  } elsif $account {
    $username = $account['username']
    $directory = "/home/$username"
  } else {
    $directory = '/'
    $username = undef
  }

  # Drop the hidden file in the challenge directory
  ::secgen_functions::leak_files { "hidden_file-$directory.$leaked_filename":
    leaked_filenames  => [".$leaked_filename"],
    storage_directory => $directory,
    strings_to_leak   => $strings_to_leak,
    owner             => $username,
    group             => $username,
    leaked_from       => "hidden_file-$directory.$leaked_filename",
    mode              => '0644'
  }

}
