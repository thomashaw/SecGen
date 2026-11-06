# Class: tomcat_ghostcat_ajp::configure
# Expose the AJP connector (Ghostcat, CVE-2020-1938) and plant a WEB-INF flag.
#
class tomcat_ghostcat_ajp::configure {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $strings_to_leak   = $secgen_parameters['strings_to_leak']
  $leaked_filenames  = $secgen_parameters['leaked_filenames']

  $catalina_base = '/opt/tomcat'
  $webinf_dir    = "${catalina_base}/webapps/ROOT/WEB-INF"

  # Replace server.xml with one whose AJP connector is bound to 0.0.0.0:8009,
  # requires no secret and allows arbitrary request attributes. This is the
  # pre-9.0.31 Ghostcat behaviour, restored on the installed (patched) release.
  # Augeas is avoided on purpose: installing ruby-augeas and using the augeas
  # provider in the same Puppet run is unreliable, so ship the whole file.
  file { "${catalina_base}/conf/server.xml":
    ensure  => file,
    owner   => 'tomcat',
    group   => 'tomcat',
    mode    => '0640',
    source  => 'puppet:///modules/tomcat_ghostcat_ajp/server.xml',
    require => Tomcat::Install[$catalina_base],
    notify  => Service['tomcat'],
  }

  # Plant the flag under WEB-INF: unreachable over HTTP, readable through the
  # Ghostcat AJP file read. leak_files creates the directory tree and chowns it
  # to the tomcat user so the service can read the file back.
  ::secgen_functions::leak_files { 'tomcat-ghostcat-webinf-leak':
    storage_directory => $webinf_dir,
    leaked_filenames  => $leaked_filenames,
    strings_to_leak   => $strings_to_leak,
    owner             => 'tomcat',
    group             => 'tomcat',
    mode              => '0640',
    leaked_from       => 'tomcat_ghostcat_ajp',
    require           => Tomcat::Install[$catalina_base],
    notify            => Service['tomcat'],
  }
}
