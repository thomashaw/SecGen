define secgen_functions::leak_file (
  $file_path_to_leak = '',
  $leaked_filename   = '',
  $storage_directory = '',
  $strings_to_leak,
  $owner             = 'root',
  $group             = 'root',
  $mode              = '0660',
  $leaked_from       = ''
) {

  if ($file_path_to_leak != '' and ($leaked_filename != '' or $storage_directory != '')) {
    fail("ERROR: Either a file_path_to_leak OR (a leaked_filename and a storage directory), not both.")
  }

  if ($leaked_filename != '') {
    $path_to_leak = "$storage_directory/$leaked_filename"
  } else {
    $path_to_leak = $file_path_to_leak
  }
  # create the directory tree, incase the file name has extra layers of directories
  if $::osfamily == 'windows' {
    exec { "win_$leaked_from-$path_to_leak-mkdir":
      command => "cmd.exe /c md $storage_directory",
      path    => "C:\\Windows\\System32",
      returns => [1]
    } ->
    exec { "win_$leaked_from-$path_to_leak-takeown":
      command => "cmd.exe /c takeown /F $storage_directory /U $owner",
      path    => "C:\\Windows\\System32",
      returns => [1]
    }
    # assuming everything not windows has mkdir and chown
    } else {
      exec { "$leaked_from-$path_to_leak-mkdir":
        path     => ['/bin', '/usr/bin', '/usr/local/bin', '/sbin', '/usr/sbin'],
        command  => "mkdir -p `dirname $path_to_leak`;chown $owner:$owner `dirname $path_to_leak`",
        provider => shell,
      }

    }

  # If the file already exists append to it, otherwise create it.
  if $::osfamily == 'windows' {
    if (defined(File[$path_to_leak])) {
      notice("File with that name already defined, appending leaked strings instead...")
      file_line { "$leaked_from-$path_to_leak -append":
        path => $path_to_leak,
        line => "\n------\n$strings_to_leak",
      }
    } else {
      file { $path_to_leak:
        ensure  => present,
        owner   => $owner,
        group   => $group,
        content => template('secgen_functions/overshare.erb')
      }
    }
  }else { # If the file already exists append to it, otherwise create it.
    if (defined(File[$path_to_leak])) {
      notice("File with that name already defined, appending leaked strings instead...")
      file_line { "$leaked_from-$path_to_leak -append":
        path => $path_to_leak,
        line => "\n------\n$strings_to_leak",
      }
    } else {
      file { $path_to_leak:
        ensure  => present,
        owner   => $owner,
        group   => $group,
        mode    => $mode,
        content => template('secgen_functions/overshare.erb')
      }
    }
  }
}
