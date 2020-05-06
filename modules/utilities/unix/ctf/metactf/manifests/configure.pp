class metactf::configure {
  $secgen_params = secgen_functions::get_parameters($::base64_inputs_file)
  $install_dir = '/tmp/metactf'
  $challenge_list = $secgen_params['challenge_list']
  $include_scaffolding = str2bool($secgen_params['include_scaffolding'][0])

  $raw_account = $secgen_params['account'][0]
  $account = parsejson($raw_account)
  $username = $account['username']
  $storage_dir = "/home/$username/challenges"

  Exec { path => ['/bin', '/usr/bin', '/usr/local/bin', '/sbin', '/usr/sbin'] }

  # Move the challenges based on account name and challenge name.

  $challenge_list.each |$counter, $raw_challenge| {

    notice("raw_challenge: $raw_challenge")
    $challenge = parsejson($raw_challenge)

    $challenge_name = $challenge['challenge_name']
    $challenge_path = $challenge['challenge_path']
    $group = $challenge['group']
    $flag = $challenge['flag']

    $split_challenge = split($challenge_path, '/')
    $metactf_challenge_category = $split_challenge[0]
    $metactf_challenge_type = split($metactf_challenge_category, '_')[1]

    $binary_path = "$install_dir/$metactf_challenge_category/obj/secgen/$metactf_challenge_type/$challenge_name"
    if $metactf_challenge_category == 'src_angr' and $include_scaffolding {
      $challenge_number = split($challenge_name, '_')[0]
      $scaffold_filename = "scaffold$challenge_number.py"
      $scaffold_path = "$install_dir/$metactf_challenge_category/$challenge_name/$scaffold_filename"
      #
      file { "create-$challenge_name-$scaffold_filename":
        path   => "$storage_dir/$challenge_name/$scaffold_filename",
        ensure => file,
        source => $scaffold_path,
      }
    }

    ::secgen_functions::install_setgid_binary { "metactf_$challenge_name":
      source_module_name => $module_name,
      challenge_name     => $challenge_name,
      group              => $group,
      account            => $account,
      flag               => $flag,
      flag_name          => 'flag',
      binary_path        => $binary_path,
      storage_dir        => $storage_dir,
    }
  }
}