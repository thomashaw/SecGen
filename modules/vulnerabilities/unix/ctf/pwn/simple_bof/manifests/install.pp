class simple_bof::install {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $buffer_size = inline_template("<%= (3..8).sample ^ 2 %>")   # randomised buffer size
  $challenge_name = $secgen_parameters['challenge_name'][0]
  $storage_dir = $secgen_parameters['storage_directory'][0]


  $install_dir = '/tmp/'

  # Generate the C file (either in the home directory or the supplied storage_directory)


  # Compile the binary
  ## ... we will need to add compiler parameters to the install_setgid_binary and install_setuid_binary modules
  ##   Determine if want ASLR:
          # OFF: echo 0 > /proc/sys/kernel/randomize_va_space
          # ON:  echo 2 > /proc/sys/kernel/randomize_va_space

  ## Determine if we want executable stack or not:
        # Pass "-z execstack" to gcc to disable

  ## Determine if we want stack canaries or not:
      # Pass "-fno-stack-protector" to gcc to disable canaries


  ## Determine if we want debugging symbols included or not
    # Pass "-g" to gcc to generate debugging symbols

  # SetGID binary requires: Makefile and any .c files within it's <module_name>/files directory
  # Can we dynamically generate these before we call the install_setgid_binary function?

  ::secgen_functions::install_setgid_binary { "simple_bof_$challenge_name":
    source_module_name => 'simple_bof',
    challenge_name     => $challenge_name,
    group              => $group,
    account            => $account,
    flag               => $flag,
    flag_name          => 'flag',
    storage_dir        => $storage_dir,
  }

}
