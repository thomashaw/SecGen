# Writes snmpd.conf and creates the include directory other modules drop fragments into.
class snmpd::config {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $agent_address = $secgen_parameters['agent_address'][0]
  $ro_community  = $secgen_parameters['ro_community'][0]
  $sys_location  = $secgen_parameters['sys_location'][0]
  $sys_contact   = $secgen_parameters['sys_contact'][0]

  file { '/etc/snmp/snmpd.conf.d':
    ensure  => directory,
    owner   => 'root',
    group   => 'root',
    mode    => '0755',
    require => Class['snmpd::install'],
  }

  # Replaces Debian's sample file, which carries a 'public' community
  file { '/etc/snmp/snmpd.conf':
    ensure    => file,
    owner     => 'root',
    group     => 'root',
    mode      => '0600',
    content   => template('snmpd/snmpd.conf.erb'),
    show_diff => false,
    require   => File['/etc/snmp/snmpd.conf.d'],
    notify    => Service['snmpd'],
  }
}
