class metactf::configure {
  $secgen_params = secgen_functions::get_parameters($::base64_inputs_file)
  $install_dir = '/tmp/metactf'
  $challenge_list = $secgen_params['challenge_list']
  $groups = $secgen_params['groups']
  $include_chapters = str2bool($secgen_params['include_chapters'][0])
  $include_scaffolding = str2bool($secgen_params['include_scaffolding'][0])

  $raw_account = $secgen_params['account'][0]
  $account = parsejson($raw_account)
  $username = $account['username']
  $storage_dir = "/home/$username/challenges"

  Exec { path => ['/bin', '/usr/bin', '/usr/local/bin', '/sbin', '/usr/sbin'] }

  # Move the challenges based on account name and challenge name.

  $challenge_list.each |$counter, $raw_challenge| {
    $challenge = parsejson($raw_challenge)

    $challenge_name = $challenge['challenge_name']
    $challenge_path = $challenge['challenge_path']
    $group = $challenge['group']
    $flag = $challenge['flag']

    $split_challenge = split($challenge_name, '/')
    $metactf_challenge_category = $split_challenge[0]
    $metactf_challenge_type = split($metactf_challenge_category, '_')[1]

    # "/tmp/metactf/src_angr/obj/secgen/src_angr"
    # "/tmp/metactf/src_malware/Ch15-16/Ch15AntiDis_FakeCond/obj/secgen/$challenge_name"

    $binary_path = "$install_dir/$metactf_challenge_category/obj/secgen/$metactf_challenge_type/$challenge_name"
    # $binary_path = "$install_dir/$metactf_challenge_category/$challenge_outer_dir/$challenge_name/obj/secgen/$challenge_name"

    # if $metactf_challenge_category == 'src_angr' and $include_scaffolding {
    #
    #   $challenge_number = split($challenge_name, '_')[0]
    #   $scaffold_filename = "scaffold$challenge_number.py"
    #
    #   $scaffold_path = "$install_dir/$metactf_challenge_category/$challenge_name/$scaffold_filename"
    #
    #   file { "create-$challenge_name-$scaffold_filename":
    #       path => "$storage_dir/$challenge_name/$scaffold_filename",
    #       ensure => file,
    #       source => $scaffold_path,
    #     }
    #
    # } else {
    #   $challenge_outer_dir = $split_challenge[1]
      if !$include_chapters {
        $split_challenge_name = split($challenge_name,'_')
        $target_challenge_name = $split_challenge_name[-1]
      } else {
        $target_challenge_name = $challenge_name
      }
    }

    ::secgen_functions::install_setgid_binary { "metactf_$challenge_name":
      source_module_name => $module_name,
      challenge_name     => $target_challenge_name,
      group              => $group,
      account            => $account,
      flag               => $flag,
      flag_name          => 'flag',
      binary_path        => $binary_path,
      storage_dir        => $storage_dir,
    }

  }

}