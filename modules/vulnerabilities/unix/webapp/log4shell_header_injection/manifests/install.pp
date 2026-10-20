# Class: log4shell_header_injection::install
#
# Installs Apache Solr 8.11.0 (bundles the vulnerable Log4j 2.14.1), run as a
# dedicated service account.
#
# Solr 8.11.0's start script only validates against Java 8/11; Debian
# bookworm's default-jre is OpenJDK 17, which the bundled start script may
# refuse. A pinned Temurin 11 JRE is downloaded alongside the system JDK
# (same approach as hadoop_yarn_rce) rather than relying on apt's default-jre.
class log4shell_header_injection::install {
  $solr_version  = '8.11.0'
  $solr_url      = "https://archive.apache.org/dist/solr/solr/${solr_version}/solr-${solr_version}.tgz"
  $solr_src      = "/usr/local/src/solr-${solr_version}.tgz"
  $solr_extract  = "/opt/solr-${solr_version}"
  $solr_home     = '/opt/solr'

  $jre_url  = 'https://github.com/adoptium/temurin11-binaries/releases/download/jdk-11.0.32.1%2B1/OpenJDK11U-jre_x64_linux_hotspot_11.0.32.1_1.tar.gz'
  $jre_src  = '/usr/local/src/temurin-jre11.tar.gz'
  $jre_home = '/usr/local/jre11'

  Exec { path => ['/bin', '/usr/bin', '/usr/local/bin', '/sbin', '/usr/sbin'] }

  ensure_packages(['wget'])

  file { '/usr/local/src':
    ensure => directory,
  }

  # Pinned Temurin 11 JRE, downloaded as data and placed by Puppet.
  exec { 'download-jre11':
    command => "wget -q --tries=3 ${jre_url} -O ${jre_src}",
    creates => $jre_src,
    timeout => 900,
    require => [Package['wget'], File['/usr/local/src']],
  }
  ->
  file { $jre_home:
    ensure => directory,
  }
  ->
  exec { 'extract-jre11':
    command => "tar -xzf ${jre_src} -C ${jre_home} --strip-components=1",
    creates => "${jre_home}/bin/java",
    timeout => 900,
  }

  File['/usr/local/src']
  ->
  exec { 'download-solr':
    command => "wget -q --tries=3 ${solr_url} -O ${solr_src}",
    creates => $solr_src,
    timeout => 1800,
    require => Package['wget'],
  }
  ->
  exec { 'extract-solr':
    command => "tar -xzf ${solr_src} -C /opt",
    creates => $solr_extract,
    timeout => 900,
  }
  ->
  file { $solr_home:
    ensure => link,
    target => $solr_extract,
  }

  # Dedicated service account to run Solr as (not root, no student login
  # needed, so this is a plain Puppet account, not the SecGen accounts
  # module). The flag is leaked into its home, reachable only after RCE.
  group { 'solr':
    ensure => present,
  }
  ->
  user { 'solr':
    ensure     => present,
    gid        => 'solr',
    home       => '/home/solr',
    managehome => true,
    shell      => '/bin/bash',
  }
  ->
  exec { 'chown-solr-install':
    command => "chown -R solr:solr ${solr_extract}",
    unless  => "test \"\$(stat -c '%U' ${solr_extract})\" = 'solr'",
    require => Exec['extract-solr'],
  }

  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  ::secgen_functions::leak_files { 'log4shell_header_injection-file-leak':
    storage_directory => '/home/solr',
    leaked_filenames   => $secgen_parameters['leaked_filenames'],
    strings_to_leak    => $secgen_parameters['strings_to_leak'],
    leaked_from        => 'log4shell_header_injection',
    owner              => 'solr',
    group              => 'solr',
    mode               => '0600',
    require            => User['solr'],
  }
}
