# Owns /etc/default/tftpd-hpa and the TFTP directory. --secure is always set.
class tftpd_hpa::config {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $address           = $secgen_parameters['address'][0]
  $directory         = $secgen_parameters['directory'][0]
  $allow_create      = downcase("${secgen_parameters['allow_create'][0]}") == 'true'

  # host:port, with the host optional (':69' means every interface)
  unless $address =~ /^[0-9.]*:[0-9]+$/ {
    fail("tftpd_hpa: address '${address}' must look like 127.0.0.1:69 or 0.0.0.0:69")
  }
  unless $directory =~ /^\/[A-Za-z0-9_.\/-]+$/ {
    fail("tftpd_hpa: directory '${directory}' must be an absolute path")
  }

  exec { 'create tftp directory':
    command => "/bin/mkdir -p ${directory}",
    creates => $directory,
    require => Class['tftpd_hpa::install'],
  }

  # owned by the tftp user so --create uploads can land; other modules add files here
  file { $directory:
    ensure  => directory,
    owner   => 'tftp',
    group   => 'tftp',
    mode    => '0755',
    require => Exec['create tftp directory'],
  }

  file { '/etc/default/tftpd-hpa':
    ensure  => file,
    owner   => 'root',
    group   => 'root',
    mode    => '0644',
    content => template('tftpd_hpa/tftpd-hpa.erb'),
    require => Class['tftpd_hpa::install'],
    notify  => Service['tftpd-hpa'],
  }
}
