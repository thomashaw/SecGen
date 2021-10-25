class logstash_7::install () {
  package { 'logstash':
    ensure => present,
  }

  file { '/etc/logstash/combined_path.rb':
    ensure => file,
    source => 'puppet:///modules/logstash_7/combined_path.rb',
    require => Package['logstash'],
  }
}
