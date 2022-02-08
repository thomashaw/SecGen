class hidden_file::install{
  $secgen_params = secgen_functions::get_parameters($::base64_inputs_file)
  $file_path_to_leak = $secgen_params['file_path_to_leak'][0]
  $strings_to_leak = $secgen_params['strings_to_leak']

  # Drop the hidden file at the file_path_to_leak
  ::secgen_functions::leak_file { "hidden_file-$file_path_to_leak":
      file_path_to_leak => $file_path_to_leak,
      strings_to_leak   => $strings_to_leak,
      leaked_from       => "hidden_file-$file_path_to_leak",
      mode              => '0644',
    }

}
