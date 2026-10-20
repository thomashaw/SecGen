# Class: log4shell_header_injection::service
#
# Runs Solr in the foreground under systemd, listening on 8983.
class log4shell_header_injection::service {
  ensure_packages(['lsof'])

  file { '/etc/systemd/system/solr.service':
    content => template('log4shell_header_injection/solr.service.erb'),
    owner   => 'root',
    mode    => '0644',
  }

  service { 'solr':
    ensure  => 'running',
    enable  => true,
    require => [File['/etc/systemd/system/solr.service'], Package['lsof']],
  }
}
