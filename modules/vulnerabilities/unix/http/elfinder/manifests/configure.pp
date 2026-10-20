class elfinder::configure {
	$secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
	$leaked_filenames   = $secgen_parameters['leaked_filenames']
	$strings_to_leak    = $secgen_parameters['strings_to_leak']
	$strings_to_pre_leak = $secgen_parameters['strings_to_pre_leak']
	$storage_directory  = $secgen_parameters['storage_directory'][0]
	$releasename = 'elFinder-2.1.58'
	$docroot = "/var/www/$releasename"

	Exec { path => ['/bin', '/usr/bin', '/usr/local/bin', '/sbin', '/usr/sbin'] }

	# world-writable public upload directory (reachable pre-exploit)
	file { "${docroot}/files/public":
		ensure => directory,
		mode   => '0777',
	}

	# visible pre-leak content inside the file manager (discoverable before exploitation)
	::secgen_functions::leak_files { 'elfinder-pre-leak':
		storage_directory => "${docroot}/files/public",
		leaked_filenames  => $leaked_filenames,
		strings_to_leak   => $strings_to_pre_leak,
		owner             => 'www-data',
		mode              => '0444',
		leaked_from       => 'elfinder-pre',
		require           => File["${docroot}/files/public"],
	}

	# hidden directory: only accessible as www-data (post-exploit)
	file { "${docroot}/files/.hidden":
		ensure => directory,
		owner  => 'www-data',
		mode   => '0700',
	}

	# leak the flag into the hidden directory
	::secgen_functions::leak_files { 'elfinder-flag-leak':
		storage_directory => $storage_directory,
		leaked_filenames  => $leaked_filenames,
		strings_to_leak   => $strings_to_leak,
		owner             => 'www-data',
		mode              => '0750',
		leaked_from       => 'elfinder',
	}
}
