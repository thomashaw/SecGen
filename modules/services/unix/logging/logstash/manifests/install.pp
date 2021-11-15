class logstash::install () {
  package { 'logstash':
    ensure => present,
  }

  file { '/etc/logstash/combined_path.rb':
    ensure => file,
    source => 'puppet:///modules/logstash/combined_path.rb',
    require => Package['logstash'],
  }
}
