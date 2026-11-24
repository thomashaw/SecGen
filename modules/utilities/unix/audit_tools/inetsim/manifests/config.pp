# Binds every INetSim service to all interfaces (so traffic redirected to this host is answered)
# and points the fake DNS answers at dns_default_ip.
#
# Where the installed Net::DNS refuses to run in INetSim's forked service processes (Kali),
# INetSim's DNS service is switched off and inetsim-dns.service answers DNS with dnsmasq
# instead, giving the same answers and logging each query to /var/log/inetsim/dns.log.
class inetsim::config {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $dns_default_ip = $secgen_parameters['dns_default_ip'][0]
  $conf = '/etc/inetsim/inetsim.conf'
  $needs_dnsmasq = '/usr/local/sbin/inetsim-dns-needs-dnsmasq'

  Exec { path => ['/usr/local/sbin', '/usr/local/bin', '/usr/sbin', '/usr/bin', '/sbin', '/bin'] }

  # The shipped config has these settings commented out ('#service_bind_address 10.10.10.1').
  file_line { 'inetsim service_bind_address':
    path    => $conf,
    line    => 'service_bind_address 0.0.0.0',
    match   => '^#?service_bind_address\s',
    require => Class['inetsim::install'],
  }

  file_line { 'inetsim dns_default_ip':
    path    => $conf,
    line    => "dns_default_ip ${dns_default_ip}",
    match   => '^#?dns_default_ip\s',
    require => Class['inetsim::install'],
  }

  # dnsmasq stand-in for INetSim's DNS service
  file { $needs_dnsmasq:
    ensure => file,
    mode   => '0755',
    source => 'puppet:///modules/inetsim/inetsim-dns-needs-dnsmasq',
  }

  file { '/etc/inetsim/dnsmasq.conf':
    ensure  => file,
    mode    => '0644',
    content => template('inetsim/dnsmasq.conf.erb'),
    require => Class['inetsim::install'],
  }

  file { '/etc/systemd/system/inetsim-dns.service':
    ensure => file,
    mode   => '0644',
    source => 'puppet:///modules/inetsim/inetsim-dns.service',
    notify => Exec['inetsim systemd daemon-reload'],
  }

  file { '/etc/systemd/system/inetsim.service.d':
    ensure => directory,
  }

  file { '/etc/systemd/system/inetsim.service.d/dns.conf':
    ensure => file,
    mode   => '0644',
    source => 'puppet:///modules/inetsim/inetsim-dns.conf',
    notify => Exec['inetsim systemd daemon-reload'],
  }

  exec { 'inetsim systemd daemon-reload':
    command     => 'systemctl daemon-reload',
    refreshonly => true,
  }

  # Only where dnsmasq takes over; elsewhere INetSim keeps its own DNS service.
  exec { 'inetsim: leave DNS to dnsmasq':
    command => "sed -i 's/^start_service dns\$/#start_service dns (answered by inetsim-dns.service)/' ${conf}",
    onlyif  => "${needs_dnsmasq} && grep -q '^start_service dns\$' ${conf}",
    require => [Class['inetsim::install'], File[$needs_dnsmasq]],
  }
}
